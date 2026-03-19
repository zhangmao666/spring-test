---
name: latest-domestic-news
description: Fetch the latest China domestic news from free official RSS feeds with no API key. Use when users ask for latest domestic headlines, current China news summaries, quick news scans, or categorized mainland news updates. Do not use for deep fact-checking, historical archives, or international-only coverage.
---

# Latest Domestic News

Fetch near-real-time domestic headlines from official free RSS feeds published by China News Service.
Use the bundled script to get structured results, then answer in concise Chinese.

## Workflow

1. Identify whether the user wants:
   - general domestic headlines
   - a category such as `domestic`, `breaking`, `society`, or `finance`
   - a specific number of headlines
2. Run:
   `python "<skill_dir>/scripts/get-latest-domestic-news.py" --category all --limit 10`
3. If the user requests a category, replace `all` with one of:
   - `domestic`
   - `breaking`
   - `society`
   - `finance`
4. Read the JSON output:
   - `source`
   - `fetched_at`
   - `category`
   - `articles[]`
5. Summarize the freshest items in Chinese, keeping titles accurate and links intact.

## Commands

### Default latest domestic news

```bash
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category all --limit 10
```

### Category-specific queries

```bash
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category domestic --limit 8
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category breaking --limit 8
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category society --limit 8
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category finance --limit 8
```

### Plain text output

```bash
python "<skill_dir>/scripts/get-latest-domestic-news.py" --category all --limit 6 --format text
```

## Output Guidance

When replying:

1. State the feed scope briefly, for example "merged from domestic, breaking, and society feeds".
2. List the newest headlines first.
3. Include:
   - title
   - publish time
   - category
   - link
4. If the user asks for a summary, paraphrase the headline and short description instead of inventing details.
5. If the user asks for "latest", prefer the first 5 to 10 items sorted by publish time.

## Error Handling

If the script returns:

- `{"error":"category must be one of ..."}`: rerun with a valid category.
- `{"error":"all feeds failed" ...}`: explain briefly that the free RSS source is temporarily unavailable.
- empty `articles`: tell the user no fresh items were returned for that category and retry with `all` or `breaking`.

## Sources

Read [references/feeds.md](references/feeds.md) when you need the feed list or want to explain why this skill does not require an API key.
