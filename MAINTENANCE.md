# Vesper maintenance fork

This fork focuses on dependable Android-to-Flipper device control. Upstream: [elder-plinius/V3SP3R](https://github.com/elder-plinius/V3SP3R), base commit `53146f0ce0420a5924fb8dc83aa3bd9810eaeb85`.

## Status and scope

The previous local repair (1.0.0-local.1, version code 2) was installed on a Pixel with preferences preserved. Its Bluetooth acceptance test has not passed. This branch adds ordered GATT setup and an explicit hardware test; it is a development build, not a verified release. The connected-device test remains pending. Do not infer device reliability from JVM tests alone.

Changes include firmware-specific official catalog downloads, ELF/checksum validation, upload readback before activation, recursive app inventory, queue-acquisition deadlines, RPC-only Bluetooth probes, tool-capable model filtering, and rejection of invented model tools. BLE startup now waits for MTU completion, service discovery, RX notifications, flow-control notifications and initial capacity before announcing readiness. Failed setup is closed with a specific error; missing callbacks time out. Generated build outputs and machine-local configuration are no longer tracked.

Existing same-name destination apps are preserved. Bulk resource installation and automatic upgrades are not implemented. Installed apps may need additional hardware or manual menu interaction. This fork does not promise universal AI control.

## Build and regression tests

Use JDK 21 or newer and Android SDK platform 35. Set `ANDROID_HOME` or a private `local.properties` containing `sdk.dir`. Keep existing debug signing credentials if updating an installed debug build; a different signer cannot update it in place.

```sh
./gradlew assembleDebug assembleDebugAndroidTest testDebugUnitTest \
  --tests '*FlipperAppCatalogTest' --tests '*CommandLockTest' \
  --tests '*BleRpcTransportTest' --tests '*BleSetupSequenceTest'
```

Set `VESPER_LIVE_CATALOG=1` to include the optional live catalog download test. Otherwise that test is skipped. These are targeted regression tests, not the entire upstream suite. The Android test APK is built but not run by this command.

## Hardware acceptance

Use an unlocked, paired Flipper. Disconnect other companion apps. Verify Android Bluetooth permissions and select the correct current device identity, especially after changing firmware. Install the two locally built APKs with adb (application update using `-r`; test APK using `-r -t`). Never clear app data merely to fix a signing conflict.

Run explicitly against the chosen device:

```sh
adb shell am instrument -w \
  -e class com.vesper.flipper.diagnostics.DeviceAcceptanceTest \
  -e flipperAddress YOUR_PAIRED_DEVICE_ADDRESS \
  -e writeReadback true \
  com.vesper.flipper.test/androidx.test.runner.AndroidJUnitRunner
```

This test uses the real app's services without an AI call. It reads device info and `/ext/apps`, optionally writes and reads approximately 9 KiB of known text to a new UUID filename under `/ext`, then disconnects, reconnects and repeats the reads. The scratch file is retained. It does not install arbitrary apps, transmit saved signals, or run keyboard scripts. Without a device address it skips; without `writeReadback=true` it performs only reads and connection changes.

Hardware evidence is still required for catalog installation/opening of a selected compatible app. Record device model, firmware/API, app version, test results and failure stage without publishing personal files, device identifiers or credentials.

## Next milestones

1. Pass device information, file listing, readback and reconnect on hardware.
2. Validate catalog installation and required resources for selected apps.
3. Report actual command results and capability limits clearly in chat.
4. Add an in-app capability guide with practical examples and hardware requirements.

Do not expand the feature set before milestone 1 passes. Reuse established official Flipper implementations where they improve reliability. Preserve upstream attribution and license terms.
