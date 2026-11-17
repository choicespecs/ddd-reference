#!/usr/bin/env python3
"""
Builds the documentation site.

    python3 docs/build.py        # from the repo root (or anywhere)

Source:  docs/_chapters/<slug>.html   (HTML fragments; ```lang fenced code blocks are allowed)
Output:  docs/<slug>.html           (full pages with nav, search, on-page TOC, prev/next)

Each fragment starts with a front-matter block:

    ---
    title: Tactical patterns
    lede: One sentence that appears under the heading.
    ---

To add a chapter: create the fragment, add it to CHAPTERS below, re-run this script.
Only the standard library is used.
"""
from __future__ import annotations

import html
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "_chapters"

SITE_TITLE = "DDD Spring + Vue Guide"

# (group, slug) in reading order. Titles come from each fragment's front matter.
CHAPTERS: list[tuple[str, str]] = [
    ("Start", "index"),
    ("Start", "data-flow"),
    ("Concepts", "ddd-fundamentals"),
    ("Concepts", "strategic-design"),
    ("Concepts", "tactical-patterns"),
    ("Architecture", "architecture-styles"),
    ("Architecture", "project-structure"),
    ("Backend", "persistence"),
    ("Backend", "application-layer"),
    ("Backend", "domain-events"),
    ("Backend", "rest-api"),
    ("Frontend", "vue-frontend"),
    ("Quality & ops", "testing"),
    ("Quality & ops", "security-config-deploy"),
    ("Decisions", "decisions"),
    ("Decisions", "make-it-yours"),
    ("Reference", "sample-walkthrough"),
    ("Reference", "glossary"),
]

FENCE = re.compile(r"^```(\w+)?[^\n]*\n(.*?)\n```[ \t]*$", re.S | re.M)
HEADING = re.compile(r"<h([23])\s+id=\"([^\"]+)\"[^>]*>(.*?)</h\1>", re.S)
TAG = re.compile(r"<[^>]+>")


def parse(slug: str) -> dict:
    raw = (SRC / f"{slug}.html").read_text(encoding="utf-8")
    meta: dict[str, str] = {}
    if raw.startswith("---"):
        _, front, raw = raw.split("---", 2)
        for line in front.strip().splitlines():
            key, _, value = line.partition(":")
            meta[key.strip()] = value.strip()
    return {"slug": slug, "title": meta.get("title", slug), "lede": meta.get("lede", ""), "body": raw.strip()}


def render_fences(body: str) -> str:
    def repl(m: re.Match) -> str:
        lang = (m.group(1) or "plaintext").lower()
        code = m.group(2)
        if lang == "mermaid":
            return f'<pre class="mermaid">\n{html.escape(code, quote=False)}\n</pre>'
        label = {"java": "Java", "ts": "TypeScript", "typescript": "TypeScript", "vue": "Vue SFC",
                 "yaml": "YAML", "sql": "SQL", "bash": "Shell", "xml": "XML", "json": "JSON",
                 "http": "HTTP", "text": "Text", "plaintext": "Text", "properties": "Properties"}.get(lang, lang)
        hl = {"vue": "xml", "ts": "typescript", "text": "plaintext"}.get(lang, lang)
        return (f'<div class="code"><div class="code-head"><span>{label}</span>'
                f'<button class="copy" type="button">Copy</button></div>'
                f'<pre><code class="language-{hl}">{html.escape(code, quote=False)}</code></pre></div>')
    return FENCE.sub(repl, body)


def nav_html(chapters: list[dict], current: str) -> str:
    out, group = [], None
    for c in chapters:
        if c["group"] != group:
            if group is not None:
                out.append("</ol>")
            group = c["group"]
            out.append(f'<div class="nav-group">{html.escape(group)}</div><ol>')
        cur = ' aria-current="page" class="on"' if c["slug"] == current else ""
        out.append(f'<li><a href="{c["slug"]}.html"{cur}>{html.escape(c["title"])}</a></li>')
    out.append("</ol>")
    return "\n".join(out)


PAGE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="{lede_attr}">
<title>{title} — {site}</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;600&display=swap">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css" id="hl-light">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css" id="hl-dark" disabled>
<link rel="stylesheet" href="assets/style.css">
<script>try{{var t=localStorage.getItem('theme');if(t)document.documentElement.dataset.theme=t;}}catch(e){{}}</script>
</head>
<body>
<a class="skip" href="#content">Skip to content</a>
<div class="shell">
  <nav class="rail" id="rail" aria-label="Chapters">
    <a class="brand" href="index.html">{site}</a>
    <div class="search">
      <input id="search" type="search" placeholder="Search headings…  ( / )" autocomplete="off" aria-label="Search">
      <ol id="search-results" hidden></ol>
    </div>
    {nav}
  </nav>
  <div class="main-wrap">
    <header class="topbar">
      <button id="menu" class="icon-btn" aria-label="Open chapters" aria-controls="rail">☰</button>
      <span class="crumbs"><a href="index.html">Guide</a> / {group}</span>
      <button id="theme" class="icon-btn" aria-label="Toggle dark mode">◐</button>
    </header>
    <main id="content">
      <article>
        <header class="chapter-head">
          <span class="eyebrow">{group}</span>
          <h1>{title}</h1>
          {lede_html}
        </header>
        {body}
        <nav class="pager" aria-label="Previous and next">{prev}{next}</nav>
      </article>
      <aside class="toc" aria-label="On this page"><div class="eyebrow">On this page</div><ol id="toc"></ol></aside>
    </main>
  </div>
</div>
<script src="assets/search-index.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/java.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/yaml.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/sql.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/typescript.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/http.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.1/dist/mermaid.min.js"></script>
<script src="assets/site.js"></script>
</body>
</html>
"""


def main() -> None:
    chapters = []
    for group, slug in CHAPTERS:
        c = parse(slug)
        c["group"] = group
        chapters.append(c)

    index = []
    for i, c in enumerate(chapters):
        body = render_fences(c["body"])
        prev_c = chapters[i - 1] if i > 0 else None
        next_c = chapters[i + 1] if i + 1 < len(chapters) else None
        prev = (f'<a class="prev" href="{prev_c["slug"]}.html"><small>← Previous</small>'
                f'{html.escape(prev_c["title"])}</a>') if prev_c else "<span></span>"
        nxt = (f'<a class="next" href="{next_c["slug"]}.html"><small>Next →</small>'
               f'{html.escape(next_c["title"])}</a>') if next_c else "<span></span>"
        page = PAGE.format(
            site=SITE_TITLE,
            title=html.escape(c["title"]),
            group=html.escape(c["group"]),
            lede_attr=html.escape(c["lede"]),
            lede_html=f'<p class="lede">{c["lede"]}</p>' if c["lede"] else "",
            nav=nav_html(chapters, c["slug"]),
            body=body,
            prev=prev,
            next=nxt,
        )
        (ROOT / f'{c["slug"]}.html').write_text(page, encoding="utf-8")

        index.append({"t": c["title"], "u": f'{c["slug"]}.html', "p": c["title"], "g": c["group"]})
        for level, anchor, text in HEADING.findall(body):
            label = html.unescape(TAG.sub("", text)).strip()
            index.append({"t": label, "u": f'{c["slug"]}.html#{anchor}', "p": c["title"], "g": c["group"]})

    (ROOT / "assets" / "search-index.js").write_text(
        "window.SEARCH_INDEX=" + json.dumps(index, ensure_ascii=False) + ";\n", encoding="utf-8")
    print(f"Built {len(chapters)} pages, {len(index)} search entries → {ROOT}")


if __name__ == "__main__":
    main()
