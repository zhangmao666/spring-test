#!/usr/bin/env python3
"""
Fetch the latest China domestic news from free official RSS feeds.

Usage:
    python get-latest-domestic-news.py --category all --limit 10
    python get-latest-domestic-news.py --category domestic --limit 8 --format text
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from html import unescape


USER_AGENT = "latest-domestic-news-skill/1.0"
TIMEOUT_SECONDS = 15

FEEDS: dict[str, str] = {
    "domestic": "https://www.chinanews.com.cn/rss/china.xml",
    "breaking": "https://www.chinanews.com.cn/rss/scroll-news.xml",
    "society": "https://www.chinanews.com.cn/rss/society.xml",
    "finance": "https://www.chinanews.com.cn/rss/finance.xml",
}


def http_get(url: str) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
        return response.read()


def strip_html(text: str) -> str:
    text = re.sub(r"<[^>]+>", " ", text or "")
    text = unescape(text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def parse_pub_date(raw: str) -> tuple[str | None, float]:
    if not raw:
        return None, 0.0
    raw = raw.strip()
    try:
        dt = parsedate_to_datetime(raw)
    except Exception:
        dt = None

    if dt is None:
        for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%d"):
            try:
                dt = datetime.strptime(raw, fmt)
                dt = dt.replace(tzinfo=timezone.utc)
                break
            except ValueError:
                continue

    if dt is None:
        return raw, 0.0

    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)

    iso_value = dt.astimezone().isoformat(timespec="seconds")
    return iso_value, dt.timestamp()


def parse_feed(category: str, url: str) -> tuple[list[dict], str]:
    xml_bytes = http_get(url)
    root = ET.fromstring(xml_bytes)
    channel = root.find("./channel")
    channel_title = channel.findtext("title", default="China News Service") if channel is not None else "China News Service"
    articles: list[dict] = []

    for item in root.findall("./channel/item"):
        title = (item.findtext("title") or "").strip()
        link = (item.findtext("link") or "").strip()
        description = strip_html(item.findtext("description") or "")
        pub_raw = item.findtext("pubDate") or ""
        published_at, published_ts = parse_pub_date(pub_raw)

        if not title or not link:
            continue

        articles.append(
            {
                "title": title,
                "link": link,
                "description": description,
                "published_at": published_at,
                "_published_ts": published_ts,
                "category": category,
                "source_feed": channel_title.strip(),
            }
        )

    return articles, channel_title.strip()


def collect_articles(category: str) -> dict:
    selected_categories = list(FEEDS.keys()) if category == "all" else [category]
    merged: list[dict] = []
    seen: set[str] = set()
    feed_titles: list[str] = []
    errors: list[dict] = []

    for current_category in selected_categories:
        url = FEEDS[current_category]
        try:
            articles, feed_title = parse_feed(current_category, url)
            feed_titles.append(feed_title)
            for article in articles:
                dedupe_key = article["link"] or article["title"]
                if dedupe_key in seen:
                    continue
                seen.add(dedupe_key)
                merged.append(article)
        except Exception as exc:  # noqa: BLE001
            errors.append(
                {
                    "category": current_category,
                    "url": url,
                    "error": str(exc),
                }
            )

    if not merged and errors:
        return {
            "error": "all feeds failed",
            "category": category,
            "details": errors,
        }

    merged.sort(key=lambda item: item.get("_published_ts", 0.0), reverse=True)
    for article in merged:
        article.pop("_published_ts", None)

    return {
        "source": "China News Service RSS",
        "fetched_at": datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z"),
        "category": category,
        "feeds": feed_titles,
        "articles": merged,
        "warnings": errors,
    }


def to_text(payload: dict, limit: int) -> str:
    if payload.get("error"):
        return json.dumps(payload, ensure_ascii=False)

    lines = [
        f"source: {payload['source']}",
        f"category: {payload['category']}",
        f"fetched_at: {payload['fetched_at']}",
        "",
    ]

    for index, article in enumerate(payload.get("articles", [])[:limit], start=1):
        lines.append(f"{index}. {article['title']}")
        lines.append(f"   time: {article.get('published_at') or 'unknown'}")
        lines.append(f"   category: {article['category']}")
        lines.append(f"   link: {article['link']}")
        if article.get("description"):
            lines.append(f"   summary: {article['description']}")
        lines.append("")

    return "\n".join(lines).rstrip()


def main() -> int:
    parser = argparse.ArgumentParser(description="Fetch latest China domestic news from free official RSS feeds.")
    parser.add_argument("--category", default="all", help="all, domestic, breaking, society, finance")
    parser.add_argument("--limit", type=int, default=10, help="Maximum number of articles to output")
    parser.add_argument("--format", choices=("json", "text"), default="json", help="Output format")
    args = parser.parse_args()

    category = args.category.strip().lower()
    if category not in {"all", *FEEDS.keys()}:
        print(
            json.dumps(
                {
                    "error": "category must be one of all, domestic, breaking, society, finance",
                    "received": category,
                },
                ensure_ascii=False,
            )
        )
        return 2

    limit = max(1, min(args.limit, 30))
    payload = collect_articles(category)

    if not payload.get("error"):
        payload["articles"] = payload.get("articles", [])[:limit]

    if args.format == "text":
        print(to_text(payload, limit))
    else:
        print(json.dumps(payload, ensure_ascii=False))

    return 1 if payload.get("error") else 0


if __name__ == "__main__":
    sys.exit(main())
