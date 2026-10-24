# Device setup retained from this customization

The launcher source and defaults contain the Home settings requested in this conversation. Android system package changes and the separate AirPlay receiver are device setup, recorded here rather than requiring the launcher to gain system-level permissions.

## MiTV-AXSO1 cleanup

`mi-tv-axso1-disabled.txt` records the exact 14 packages disabled on this TV, including Google Play Movies & TV and Xiaomi TV+. All other original packages were retained. Preserve Chromecast, voice search, the Play Store/Services, keyboard, Bluetooth/remote services, firmware updates and TV/HDMI input components.

Before changing any other device, inspect its actual packages, save enabled/disabled packages, properties and settings, and prepare an undo. Use the guarded tools from https://github.com/Gathin23/cleanyourtv rather than applying this list blindly. System packages are disabled for user 0, not uninstalled.

This TV's Google launcher has higher HOME intent priority. During the original setup it was disabled only after Personal Home was installed, visually verified and tested. Keep an ADB recovery connection and the prior Home component before a first-time handover. Installing a launcher does not by itself prove the firmware selected it.

## DNS

The owner chose AdGuard Private DNS, hostname `dns.adguard-dns.com`, mode `hostname`. The original DNS values were unset. Undo restores the original values with `settings delete global private_dns_mode` and `settings delete global private_dns_specifier`. DNS filtering cannot remove every advertisement inside streaming apps. Do not enable this over a router's existing DNS/domain-routing rules without checking their effect.

## AirPlay

Separate receiver: https://github.com/jqssun/android-airplay-server/releases/tag/v0.0.33 . Its official APK's SHA256 is `2203dcc76ad884265e6e01c2147c917f100e93f37b91bf728bb3eed466396b32`; package `io.github.jqssun.airplay`, GPL-3. No GPL receiver code is mixed into this MIT launcher.

Receiver name is **Mi TV AirPlay**. Auto-start at boot is enabled. Background operation and overlay permission were configured, and its foreground service and Bonjour advertisement were verified. Its own app controls name, PIN and startup. Screen/audio mirroring must be checked from the owner's Apple device; FairPlay-protected streaming video is unsupported. Use the installed streaming apps for that video.

ADB remains enabled at the owner's request for further customization. Disable debugging and revoke authorizations when finished.
