# BPL / Changhong AI PONT

Tested hardware: AI PONT, device `samseong`, MStar/MediaTek `m7322`, Android TV 11 / SDK 30, firmware `RTO2.230323.001`, 32-bit ARM, 1080p, 1,382,068 KiB usable RAM.

## Home and cable

Stillframe 1.0 build 13 owns Android Home. FLauncher was removed through the TV's uninstaller after Stillframe was installed and visually checked. The firmware's cable startup behavior is retained. Stillframe uses `com.mediatek.wwtv.tvcenter` for its Live TV entry when `com.android.tv` is absent.

The settings APK's `power_boot_resume_slice_uri` is empty on this firmware. Its generic Power-on behavior page therefore has no configured provider. Do not copy Xiaomi's auto-play flags to this board or add a delayed Home launch. Native power-on choice is left to the TV firmware.

## Cleanup

The initial inventory contained 126 packages, including ten already disabled. Four more packages were disabled with the guarded tools: the AI PONT promotional portal, Play Movies & TV, calendar syncing and print spooling. The exact additions are in [bpl-ai-pont-disabled.txt](bpl-ai-pont-disabled.txt). There was no Anoki or FAST-channel package on this older firmware.

Preserved: all other streaming apps, Chromecast, Assistant/voice search, both vendor screen-sharing apps, keyboard, Bluetooth, phone remote, firmware and vendor app updaters, `com.homwee.synckey`, MediaTek TV services and inputs, and `com.google.android.leanbacklauncher.partnercustomizer`. The partner customizer provides `tvlauncher.widget`; removing it can crash MediaTek's TV service. Factory and picture-test packages that were disabled before this session were not opened or changed.

Disposable caches were trimmed with `pm trim-caches 1G internal`; app data and sign-ins were retained. Free data storage increased from about 628 MiB to about 1 GiB. Temporary caches will grow again as apps are used.

## Responsiveness

FLauncher used roughly 22% of one CPU core in repeated short samples while behind Android Settings. After replacement, short stationary samples showed total CPU use near 3%, compared with roughly 17–20% before replacement. These samples describe idle load, not a controlled physical-remote latency benchmark. The owner reported that the physical remote felt better.

The owner chose **Dock → Animate focus → Off** for immediate focus changes. Android's window, transition and animator duration scales are 0.5. No accessibility key filter, process-limit tweak, forced GPU setting or Bluetooth reset was added. The existing 400 ms repeat timeout / 50 ms repeat delay were retained.

Build 12's first Home after reboot was reported black for 5–10 seconds. Lifecycle logs showed expensive work in both activity creation and resume. Build 13 moves the first wallpaper decode and initial app artwork scan off the UI thread; the header can draw before either job finishes. Empty grids after a memory trim use the same asynchronous reload, with stale results cancelled when the activity is destroyed or trimmed again. Foreground navigation remains on the UI thread, and data publication keeps the current tile order and user focus.

On-device checks after this change measured a 1,042 ms first frame from a fresh Home process opened from cable, and a 216 ms return after an explicit Android COMPLETE memory trim. The dock and selected wallpaper were visually checked after recovery. These are first-frame measurements after setup; artwork may finish loading afterwards and cold-boot performance also depends on firmware startup load.

After a final real reboot, the owner confirmed that cable still appeared first and the first Home press now displayed Stillframe promptly. Streaming playback, ordinary D-pad controls, volume hold and voice search had also passed the owner's checks.

## DNS, AirPlay and recovery

AdGuard Private DNS uses `dns.adguard-dns.com` in hostname mode, with validation confirmed. The original DNS entries were absent; undo with `settings delete global private_dns_mode` and `settings delete global private_dns_specifier`.

The separately licensed official AirPlay Server v0.0.33 is named **Hall TV AirPlay**, starts at boot and can run in the background. It listens on port 7001 so it can coexist with the retained vendor receiver. Chromecast and vendor casting remain installed. Apple-device mirroring and protected streaming playback require owner checks.

This firmware delivered the receiver's boot broadcast later than the cable player's startup. Its own boot receiver subsequently started the foreground service and registered Bonjour successfully; no launcher timer is used to start it.

Private backups, the complete package inventory and a session-specific recovery script are stored outside this repository. The script re-enables only the four new cleanup targets and restores changed DNS/animation settings. It verifies a recovery Home before disabling Stillframe. If the removed FLauncher is absent, it enables stock Google Home as the recovery destination. The original disabled-package state is otherwise preserved.
