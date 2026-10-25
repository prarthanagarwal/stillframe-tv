# Stillframe

Your independent, editable Android TV launcher fork, based on CleanYourTV at commit eb0131a32fd8567086f39002f1782f7c1b078ca6. Tested device: Xiaomi MiTV-AXSO1, Android 9 / SDK 28, firmware PI.6157.

Version: **1.0** (version code 13). It updates the existing `com.gathin.tvhome` identity to preserve your Home assignment and preferences. Original licences and credits are retained. Published project: https://github.com/prarthanagarwal/stillframe-tv . The root README contains the real TV screenshots, releases and companion AirPlay setup. Also tested on BPL/Changhong AI PONT, Android 11 / SDK 30, firmware RTO2.230323.001; its MediaTek Live TV player is selected automatically when the standard player is absent.

## Included controls

- A thin dock and separate, normal-proportion app-grid tiles.
- Default dock: Netflix, Prime Video, YouTube, JioHotstar, Stremio and Spotify.
- Hold OK: add/replace a dock app, move an app, or open app information.
- Home Settings: Dock, Apps, Background, Weather, Receivers and Developer, with TV Settings fixed at the top right.
- Remote-friendly sliders: dock vertical position, dock thickness, glass opacity and blur, and wallpaper dimming.
- A dock layout preview, reset for dock appearance and focus-animation control.
- Dock selection/order and Home shortcut visibility.
- Local landscape choices, optional picture import and rotation.
- Surat weather by default, city search, Celsius/Fahrenheit and a weather toggle.
- AirPlay settings shortcut; the receiver is a separate GPL-3 application.
- Developer page: confirmed ADB switch, Android Developer options and native TV settings. The switch needs the optional `WRITE_SECURE_SETTINGS` grant.
- Startup: Stillframe remains the Android Home app and launches immediately when Android selects Home. The Xiaomi firmware may still auto-play a TV input independently.
- No recommendation preview or analytics.

Changes save on the TV. Press Left/Right on a slider; return Home to apply layout/material changes. Dock position runs from Bottom to Top within the space below the header. Thickness is 40–140% of normal tile height; the app grid stays at its original proportions.

The frosted dock uses a small cached wallpaper sample, blurred off the main thread only when the picture changes or Home rebuilds. App navigation does not run a blur filter. With a plain background there is no wallpaper detail to blur. Blur/opacity 0 means clear; 100 is the strongest setting. Landscape photos retain their own terms in WALLPAPER-CREDITS.json.

## Build

Use JDK 17, Android SDK platform 34, build-tools 34.0.0 and platform-tools. Set `JAVA_HOME` and `ANDROID_HOME`, then run:

```sh
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

The signed APK is `app/build/outputs/apk/release/app-release.apk`. Keep the Android signing key used for the first installation; later updates require that same key. Version code 13 updates earlier Stillframe builds in place; the displayed version remains 1.0.

With the TV connected and authorized over ADB:

```sh
GRANT_ADB_CONTROL=1 TV_SERIAL=TV_IP:5555 ./build-and-install.sh
```

`WRITE_SECURE_SETTINGS` is powerful and is granted only when `GRANT_ADB_CONTROL=1` is set. Revoke with `adb -s TV_IP:5555 shell pm revoke com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS`; the system-settings shortcuts still work without it. It does not disable another launcher or run a package cleanup. First-time setup notes are in setup/README.md. On the owner's already-configured TV, it is an in-place update.

## Source map

- `LauncherSettingsActivity.java`: settings navigation, sliders, app and wallpaper controls.
- `AppAdapter.java`, `DockLayout.java`: separate dock/grid sizing and dock positioning.
- `DockDecoration.java`, `PixelBlur.java`: cached frosted dock material.
- `HomePreferences.java`, `Config.java`: saved choices and shipped defaults.
- `WeatherLine.java`: cached Open-Meteo header weather, active only while Home is visible.
- `MainActivity.java`: Home, remote navigation, inputs and app launch integration.
- `app/src/main/res/values/config.xml`: default dock, native layout and enabled controls.

Source files are under `app/src/main/java/com/gathin/tvhome/`. Tests are under `app/src/test/`. The retained package identity is intentional; the app label is Stillframe.
