# Stillframe contributor instructions

This is prarthanagarwal's Stillframe fork of Gathin23/cleanyourtv. Read README.md, launcher/README.md and launcher/DESIGN.md first. The upstream README is retained as README.upstream.md; upstream model lists and guarded device tools remain available.

- The maintained launcher is `launcher/`, package `com.gathin.tvhome`, version 1.0. Preserve its identity, signing compatibility, native TV inputs, remote navigation and stored choices.
- Keep dock sizing independent from normal app-grid sizing. No recommendation preview or analytics. Weather is optional, cached and fetched only while Home is visible. Glass blur is a cached wallpaper sample, never a per-frame filter.
- Home Settings owns app, dock, background, weather and receiver controls. Keep its TV Settings shortcut at the top right and all controls usable by D-pad.
- `airplay/` installs a separate GPL-3 receiver from its official pinned release with checksum verification. Do not mix GPL receiver code into the MIT launcher. Do not commit receiver APKs, keys, credentials or device backups.
- Preserve the original guarded debloat/restore tools. Disable rather than uninstall. Never disable core Android, remote/Bluetooth, keyboard, Play Store/Services, or TV/HDMI input components. Never launch factory/test packages.
- Back up and prepare recovery before device changes. Stock Home is handed over only after the new Home actually starts and is visually verified. Do not root, flash, bypass signature protection, or silently alter casting/voice/update services.
- Real-device operations require the owner's task authorization. Capture and inspect actual screenshots for UI changes; honor a request that the owner perform physical checks themselves. Device ADB closure follows the owner's current instruction.
- Keep Wi-Fi identifiers, account data, device addresses/serials, credentials and private backups out of public commits. The README screenshots were explicitly requested for publication by the owner.
- Launcher validation: in launcher/, `./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease` with JDK17 and Android SDK34. Lint errors must be zero. Our APK includes three local photographs, so the upstream 512 KB limit does not apply to this fork.
- Installer validation: `python3 -m unittest discover -s airplay/tests -v`. Do not run it against a real TV as a test.
- Do not enable upstream maintainer bots or send comments/messages to other users unless explicitly authorized. CI for this fork builds/tests the launcher and tests the AirPlay installer.
