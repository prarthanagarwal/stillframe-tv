#!/usr/bin/env python3
"""Install the separate official AirPlay receiver; uses only Python's standard library and adb."""
import argparse
import hashlib
import os
from pathlib import Path
import shlex
import subprocess
import tempfile
import urllib.request

VERSION = "0.0.33"
PACKAGE = "io.github.jqssun.airplay"
URL = f"https://github.com/jqssun/android-airplay-server/releases/download/v{VERSION}/app-release.apk"
SHA256 = "2203dcc76ad884265e6e01c2147c917f100e93f37b91bf728bb3eed466396b32"

def verify(path, expected=SHA256):
    digest = hashlib.sha256(Path(path).read_bytes()).hexdigest()
    if digest != expected:
        raise ValueError("AirPlay APK checksum mismatch; nothing was installed")

def remote_command(args):
    # adb shell joins arguments before sending them: quote for the device's shell, not the host shell.
    return shlex.join([str(arg) for arg in args])

class Device:
    def __init__(self, serial):
        self.base = ["adb", "-s", serial]
    def run(self, *args):
        result = subprocess.run(self.base + list(args), check=True, text=True, capture_output=True, timeout=180)
        if result.stdout.strip(): print(result.stdout.strip())
        return result.stdout.strip()
    def shell(self, *args):
        return self.run("shell", remote_command(args))

def install(serial, name):
    if not name.strip() or len(name) > 64 or any(ord(c) < 32 for c in name):
        raise ValueError("Receiver name must be 1–64 printable characters")
    device = Device(serial)
    if device.run("get-state") != "device":
        raise RuntimeError("Connect and authorize this TV over adb first")
    sdk = int(device.shell("getprop", "ro.build.version.sdk"))
    if sdk < 24: raise RuntimeError("The receiver requires Android 7 / SDK 24 or newer")
    abis = device.shell("getprop", "ro.product.cpu.abilist").split(",")
    if not set(abis).intersection({"armeabi-v7a", "arm64-v8a", "x86_64"}):
        raise RuntimeError("The official receiver APK does not support this CPU architecture")
    with tempfile.TemporaryDirectory(prefix="stillframe-airplay-") as folder:
        apk = Path(folder) / "airplay.apk"
        print(f"Downloading official AirPlay Server {VERSION}…")
        with urllib.request.urlopen(URL, timeout=60) as response, apk.open("wb") as out:
            total = 0
            while True:
                chunk = response.read(65536)
                if not chunk: break
                total += len(chunk)
                if total > 128 * 1024 * 1024: raise ValueError("Unexpected APK size")
                out.write(chunk)
        verify(apk)
        installed = device.run("install", "-r", str(apk))
        if "Success" not in installed: raise RuntimeError("APK installation was not confirmed")
    for key, value in [("server_name", name), ("boot_auto_start", "true")]:
        result = device.shell("am", "broadcast", "-a", f"{PACKAGE}.SET_SETTING", "-n", f"{PACKAGE}/.SettingsReceiver", "--es", "key", key, "--es", "value", value)
        if "result=-1" not in result: raise RuntimeError(f"Receiver did not accept {key}; inspect the app before continuing")
    device.shell("dumpsys", "deviceidle", "whitelist", f"+{PACKAGE}")
    device.shell("cmd", "appops", "set", PACKAGE, "RUN_IN_BACKGROUND", "allow")
    device.shell("cmd", "appops", "set", PACKAGE, "SYSTEM_ALERT_WINDOW", "allow")
    if sdk >= 33:
        device.shell("pm", "grant", PACKAGE, "android.permission.POST_NOTIFICATIONS")
    started = device.shell("am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")
    if "Status: ok" not in started or "Error:" in started:
        raise RuntimeError("Receiver launch was not confirmed; open AirPlay Server on the TV")
    print(f'Installed. Select "{name}" in Screen Mirroring on your Apple device.')
    print("Use Home Settings → Receivers → AirPlay settings for PIN and startup controls.")
    print("FairPlay/DRM-protected video is unsupported; use the installed streaming apps.")

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", default=os.environ.get("TV_SERIAL"), help="ADB device serial, usually TV_IP:5555")
    parser.add_argument("--name", default="Mi TV AirPlay", help="Receiver display name")
    args = parser.parse_args()
    if not args.serial: parser.error("Pass --serial TV_IP:5555 or set TV_SERIAL")
    try: install(args.serial, args.name)
    except (ValueError, RuntimeError, OSError, subprocess.SubprocessError) as error:
        parser.exit(1, f"AirPlay setup stopped: {error}\n")

if __name__ == "__main__": main()
