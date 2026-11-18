# DDD Reference

A learning reference for **domain-driven design**. It explains the strategic ideas that shape a system, the tactical patterns that shape the code, and the trade-offs in between. One fictional ticketing business is used as the running example throughout.

**Read it online: https://choicespecs.github.io/ddd-reference/**

This is a reference to read, not a template or framework to install.

## Contents

| Section | Chapters |
|---|---|
| Start | What DDD is · Why DDD (and when not to) · The running example |
| Strategic design | Ubiquitous language · Subdomains · Discovering the domain (Event Storming, Domain Storytelling, Example Mapping) · Bounded contexts · Context mapping |
| Tactical design | Entities & value objects · Aggregates · Domain events · Repositories & factories · Domain & application services |
| Architecture | Layered / hexagonal / clean · Integrating contexts (outbox, idempotency, sagas) · CQRS & event sourcing |
| Practice | Testing the model · Anti-patterns · DDD in legacy systems · Design checklist |
| Reference | Glossary · Further reading |

Code examples use modern Java because it reads clearly, but the ideas apply in any language.

## Editing

Chapter sources are HTML fragments in `docs/_chapters/` (with ```` ```lang ```` code fences and ```` ```mermaid ```` diagrams). After editing, rebuild the pages:

```bash
python3 docs/build.py   # standard library only; writes docs/*.html and the search index
```

To add a chapter, create `docs/_chapters/<slug>.html` with `title:` / `lede:` front matter, add it to `CHAPTERS` in `docs/build.py`, and rebuild. GitHub Pages serves the `docs/` folder from `main`.

Open `docs/index.html` directly in a browser to preview the site.
