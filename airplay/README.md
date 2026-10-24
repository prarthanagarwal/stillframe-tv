# AirPlay companion setup

Personal Home links to a separate **AirPlay Server** app by [jqssun](https://github.com/jqssun/android-airplay-server), using UxPlay. The receiver is GPL-3; this directory contains our MIT installer, not the receiver's code. The companion setup was inspired by [nguyenduchoai's AirPlay branch](https://github.com/nguyenduchoai/tv-vibehack/tree/feat/airplay-receiver).

The installer downloads the official **v0.0.33** universal APK and verifies GitHub's recorded SHA256 before installing anything:

```text
2203dcc76ad884265e6e01c2147c917f100e93f37b91bf728bb3eed466396b32
```

Connect and authorize the TV, then run from the repository root:

```sh
python3 airplay/install.py --serial TV_IP:5555 --name "Mi TV AirPlay"
```

It configures the name, start at boot, background operation and the overlay permission needed to open mirroring. On Android 13+ it also grants the receiver's notification permission. Only the Android 9 MiTV-AXSO1 setup has been checked on real hardware. The installer leaves the receiver app on screen so you can check its status and finish any device-specific prompts.

Open **Home Settings → Receivers → AirPlay settings** for the receiver's own PIN and startup controls. Select **Mi TV AirPlay** from Screen Mirroring on an iPhone, iPad or Mac on the same network. Chromecast remains installed independently.

The receiver's foreground service and Bonjour advertisement were verified on our TV. End-to-end mirroring/audio testing remains a physical-device check. FairPlay-protected video from Netflix, Apple TV+ or Disney+ cannot be decrypted by this receiver; use those TV apps instead.

To stop using it, stop the receiver and switch off start at boot in its app. You can also disable this separate package over ADB:

```sh
adb -s TV_IP:5555 shell pm disable-user --user 0 io.github.jqssun.airplay
# Undo:
adb -s TV_IP:5555 shell pm enable --user 0 io.github.jqssun.airplay
```

Python smoke tests: `python3 -m unittest discover -s airplay/tests -v`. These tests do not contact or change a TV.
