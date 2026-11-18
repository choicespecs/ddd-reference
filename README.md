# Domain-Driven UI

A learning reference for building applications where **domain objects drive the UI**. You write plain domain objects (POJOs) with their data, rules and behaviour, and the forms, tables, buttons, validation and REST API are generated from them. The objects are described either with **Java annotations** or with **JSON metadata**. This approach is known as the *Naked Objects* pattern, and it builds on domain-driven design.

**Read it online: https://choicespecs.github.io/ddd-reference/**

This is a reference to read, not a framework to install. One fictional ticketing business is used as the running example throughout.

## Contents

| Section | Chapters |
|---|---|
| Start | Domain objects drive the UI · Naked Objects & DDD · The running example |
| Modelling the objects | Names become the UI · Entities & value objects · Aggregates · Behaviour as actions |
| Describing the objects | The metamodel · Java annotations · JSON metadata · Annotations vs. JSON |
| From metadata to UI | Rendering the UI · The generic REST API · Rules once, enforced everywhere |
| Practice | Customisation · Trade-offs (fit, security, performance) |
| Reference | Glossary · Further reading |

Server examples are plain Java (records, Jakarta Bean Validation). Client examples are TypeScript and Vue.

## Editing

Chapter sources are HTML fragments in `docs/_chapters/` (with ```` ```lang ```` code fences and ```` ```mermaid ```` diagrams). After editing, rebuild the pages:

```bash
python3 docs/build.py   # standard library only; writes docs/*.html and the search index
```

To add a chapter, create `docs/_chapters/<slug>.html` with `title:` / `lede:` front matter, add it to `CHAPTERS` in `docs/build.py`, and rebuild. GitHub Pages serves the `docs/` folder from `main`.

Open `docs/index.html` directly in a browser to preview the site.
