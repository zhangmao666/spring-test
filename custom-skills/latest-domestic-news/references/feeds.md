# Free Feed Sources

This skill uses free official RSS feeds from China News Service.

Official RSS directory:

- `https://www.chinanews.com.cn/rss/`

Feeds used by the script:

- `domestic`: `https://www.chinanews.com.cn/rss/china.xml`
- `breaking`: `https://www.chinanews.com.cn/rss/scroll-news.xml`
- `society`: `https://www.chinanews.com.cn/rss/society.xml`
- `finance`: `https://www.chinanews.com.cn/rss/finance.xml`

Why this source:

- Official publisher feed
- Free to access
- No API key required
- Standard RSS 2.0, easy to parse with Python standard library
- Provides publish time, title, description, and link

Notes:

- `breaking` can include mixed topics, but it is often the freshest source.
- `all` mode in the script merges multiple feeds, deduplicates by link or title, and sorts by publish time descending.
- This skill is optimized for latest domestic headlines, not deep reporting or historical archives.
