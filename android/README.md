# Galeria poz AI — Android (WebView)

Opakowanie statycznej galerii PL/EN w WebView.

- **package:** `pl.nozownik.posegallery`
- **minSdk:** 24 · **targetSdk:** 34
- UI + `data/pose-en.json` z assetów (offline)
- Obrazy z CDN `https://ai-pose-gallery.pages.dev/` (wymaga sieci)

## Build

```bash
export JAVA_HOME=…   # JDK 17+
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Instalacja

```bash
adb install -r GaleriaPozAI.apk
```

Albo: skopiuj APK na telefon → otwórz → zezwól na instalację z nieznanych źródeł.
