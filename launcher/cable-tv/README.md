# Cable TV — HDMI 2

An optional, permission-free shortcut for the configured BPL/Changhong AI PONT hall TV. Its banner says **Cable TV / HDMI 2** so it can be placed first in Stillframe's dock for the parents who use this set.

[Download Cable TV 1.0](https://github.com/prarthanagarwal/stillframe-tv/releases/tag/cable-tv-v1.0). The main Stillframe launcher remains version 1.0 build 13.

This selects the set-top box's physical **HDMI 2** input. It sends `ACTION_VIEW` with the passthrough channel URI for `com.mediatek.tvinput/.hdmi.HDMIInputService/HW5`, explicitly to `com.mediatek.wwtv.tvcenter`. The registered hardware map on this device identifies HW5 as HDMI port 2. Launching the native Live TV app without an input URI would only restore its remembered source; this shortcut always requests HDMI 2.

The package is `com.stillframe.cabletv`, version 1.0. It contains one transient activity, has no permissions, services or boot receivers, and finishes immediately after forwarding the request. It does not change the TV's startup source or the Stillframe Home assignment.

## Build and install

From `launcher/`, with the same JDK 17 / Android SDK 34 setup as the main app:

```sh
./gradlew :cable-tv:assembleRelease
adb -s TV_IP:5555 install -r cable-tv/build/outputs/apk/release/cable-tv-release.apk
```

In **Home Settings → Apps → Choose dock apps**, add Cable TV. Up to seven dock items are supported. The hall setup puts it first, followed by the existing Netflix, Prime Video, YouTube, JioHotstar, Stremio and Spotify choices. The same long-press arrangement controls work for it as for other apps.

This is a device-specific shortcut. Other models may map HDMI 2 to a different input ID or use another player; inspect their actual input map before adapting the constants. Remove it from the dock before disabling or uninstalling the companion package.

The banner is a native vector drawable, with text outlines from the repository's Outfit font under the [SIL Open Font License](../FONT-LICENSE-OFL.txt). Its checked-in source can be regenerated on macOS with `generate-banner.swift`; Android builds do not need Swift or the font generator.

This addition was built and installed without running tests or rebooting, at the owner's request. HDMI switching is left for the owner's check.
