# Permissions Guide

BLE scanning on Android requires access to user's location: https://developer.android.com/guide/topics/connectivity/bluetooth-le#permissions

An anonymous StackOverflow post explains the reasons quite well:

> BLE beacons can be used to get location information using nothing more than the BLE broadcast UUID data and an internet connection (e.g. iBeacon, AltBeacon etc.) Since this is possible and the data can be acquired via scan, a permission for location is required. In reality, ACCESS_COARSE_LOCATION is the required level in order to get NetworkProvider level of permission. By using, ACCESS_FINE_LOCATION you get NetworkProvider as well as GPS.

Applications using Bose Wearable SDK will need to ask for the location permission at runtime. [bosewearableui module](Usage.md#with-bosewearableui) does it for you, but you'll need to handle it yourself when building a custom device discovery dialog.
