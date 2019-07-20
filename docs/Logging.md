# Bose Wearable SDK Logging

BoseWearable-Android is using the standard Android logging
[framework](https://developer.android.com/reference/android/util/Log.html). To see the output of
Android logs, `adb logcat` command can be used from terminal. Android logging framework uses
following log levels:

```
VERBOSE
DEBUG
*INFO
*WARN
*ERROR
```

* indicates that the log level is enabled by default.

## Locate `adb` tool

The default location on MacOS `~/Library/Android/sdk/platform-tools/adb`. If it's not there, you can
find the Android SDK installation directory from Android Studio. Open Android Studio Preferences and
have a look at:

Appearance & Behavior -> System Settings -> Android SDK -> Android SDK Location

The `adb` binary will be in `$ANDROID_SDK_LOCATION/platform-tools/adb`.

## Enable debug logging

BoseWearable-Android is using the following logging "topics", each of which can be controlled
independently using the `adb` tool. The topics are:

```
BoseWearableDiscovery
BoseWearableTraffic
BoseWearableDevice
BoseWearableService
BoseWearableSession
BoseWearableSensor
```

Hopefully the meaning of each topic is self explanatory.
To see debug log message for device discovery and Bluetooth traffic for an example, the following
commands can be used:

```shell
$ adb shell setprop log.tag.BoseWearableDiscovery DEBUG
$ adb shell setprop log.tag.BoseWearableTraffic DEBUG
```

## Monitor logs

To see log messages, use `adb` command:

```shell
$ adb logcat | grep BoseWearable
```
