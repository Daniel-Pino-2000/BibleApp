# TreasuredWord app icon — Android resources

"Light at the Seam": a closed Bible with a gold cross, and a soft warm glow
escaping from where the pages meet the cover.

## How to install

Copy everything inside this folder into your project's `app/src/main/res/`
directory, merging with what's already there (Android Studio will ask before
overwriting — say yes, or rename your existing `ic_launcher*` files first if
you want to keep them as backups).

```
res/
├── drawable/
│   └── ic_launcher_background.xml     ← the gradient background (vector, scales perfectly)
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml                ← adaptive icon definition (Android 8.0+)
│   └── ic_launcher_round.xml          ← same, for round-icon launchers
├── mipmap-mdpi/
│   ├── ic_launcher.png                ← legacy icon, pre-Android-8 fallback
│   ├── ic_launcher_round.png
│   └── ic_launcher_foreground.png     ← adaptive icon foreground layer
├── mipmap-hdpi/        (same three files, larger)
├── mipmap-xhdpi/       (same three files, larger)
├── mipmap-xxhdpi/      (same three files, larger)
└── mipmap-xxxhdpi/     (same three files, larger)
```

`playstore-icon-512.png` is **not** part of the app itself — upload it
separately in the Play Console under Store presence → Main store listing →
App icon.

## Why it's built this way

- **Android 8.0+ (API 26+)** reads `mipmap-anydpi-v26/ic_launcher.xml`, which
  layers `ic_launcher_background.xml` (a resolution-independent gradient —
  no PNG needed) under `ic_launcher_foreground.png` (the book, cross, and
  glow). The system applies its own mask shape (circle, squircle, rounded
  square, etc. depending on the device), so the icon looks native everywhere
  without you doing anything extra.
- **Older Android versions** ignore the adaptive files and use
  `ic_launcher.png` / `ic_launcher_round.png` instead, which already have the
  shape baked in.

## If you want to double-check it in Android Studio

Right-click `res` → New → Image Asset → Launcher Icons (Adaptive and
Legacy) will let you preview how the foreground/background combo looks
under different mask shapes before you ship. If you ever want to nudge the
glow or the cross, the original editable SVG is included below the PNGs are
generated from — ask and I can hand that over too, or generate the icon
again at a different scale.
