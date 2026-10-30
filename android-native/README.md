# Native Android — Galeria poz AI (Compose)

Package: `pl.nozownik.posegallery`  
Buildable project: sibling `ai-pose-gallery-pl-app`

## v2.3.0
New bottom tab **Ubrania / Outfits**: 1407 garment-only outfit references from COSTUME-DB-01 衣装集 by ギア (@giaaibusi), https://ai-costume-gallery.pages.dev/ .
8 categories (Wiosna, Lato, Jesień, Zima, Bielizna, Impreza, Stroje kąpielowe, Seksowne) + ♥ Ulubione, search by ID / EN keyword / common Polish words (czarny, sukienka…).
Detail: copy EN outfit description, copy image, save original PNG, open source site, outfit prompts (Nano Banana / ChatGPT / Grok Imagine / outfit + pose), swipe prev/next.
Only metadata is bundled (`assets/data/costumes.json`); images load on demand from the source site (author forbids redistribution and bulk downloading), so they are excluded from offline caching. Credit added to About.

## v2.2.1
About / O aplikacji in Settings: version (BuildConfig), APK repo link, ZH fork credit (anhao), pose CDN, image author (@giaaibusi), MIT note. Links open in browser.

## v2.2.0
Keeps 2.1.1: larger thumbs, copy image bitmap, style prompts only on custom create, category re-tap keeps filter.

1. Swipe gallery L/R → adjacent category
2. Custom categories CRUD + chip row
3. Clipboard history (tags/images) re-copy
4. Compare 2 poses (split / overlay)
5. Export custom pack ZIP + share
6. Random pose + pin home shortcut (no full widget)
7. Detail swipe: short = angle, long = next group
8. Offline cache thumbs for current category filter
10. Batch select → stacked EN tags to clipboard

Skipped: #9 seed/LoRA

versionName 2.3.0 / versionCode 7
