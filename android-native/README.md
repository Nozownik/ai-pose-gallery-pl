# Native Android — Galeria poz AI (Compose)

Package: `pl.nozownik.posegallery`  
Buildable project: sibling `ai-pose-gallery-pl-app`

## v2.4.2

- Ekran „Dodaj swój ciuch”: zdjęcie + nazwa/opis i ramka **PRO TIP** z gotowym promptem (kopiuj) do przerobienia zdjęcia (np. osoby w ciuchu) na sam strój w stylu Custom: ghost mannequin, bez człowieka, jasnoszare tło studyjne, 2:3.
- Usunięte karty promptów (Nano Banana / ChatGPT / Grok Imagine / Strój + poza) ze wszystkich ekranów strojów. Edycja i usuwanie własnych strojów zostają.

## v2.4.1

- **Ubrania → „Dodaj swój ciuch” (+)**: dodaj własny strój ze zdjęcia z telefonu (systemowy wybór zdjęć, bez nowych uprawnień), z nazwą i opcjonalnym opisem/słowami kluczowymi. Zapisywany tylko lokalnie w pamięci aplikacji, kategoria **Moje** (z licznikiem), wyszukiwanie, ulubione, edycja i usuwanie.
- Karty **Prompty stroju** (Nano Banana, ChatGPT, Grok Imagine, Strój + poza) pokazują się tylko przy dodawaniu własnego stroju i na jego ekranie — zniknęły z ekranów strojów Custom i COSTUME-DB-01 (prompty generowania zostają w lokalnym, niepublikowanym `custom.json`).

## v2.4.0
Ubrania: new first category **Custom** — 50 own AI-generated garment-only images (ghost mannequin / flat lay, no people), bundled offline in the APK (img 768×1152 webp + 300×450 thumbs). Neither the images nor `assets/custom/custom.json` (names, keywords, prompts) are published here; they are kept local (gitignored) and bundled at build time. Detail shows the generation prompt plus the usual prompt variants; save writes the WEBP; "open source site" hidden for Custom. Search matches PL/EN names and keywords. Favourites work as for other outfits.

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

versionName 2.4.2 / versionCode 10
