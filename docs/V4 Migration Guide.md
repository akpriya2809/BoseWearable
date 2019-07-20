# Migrating to SDK v4 from v3

This document explains how to migrate from v3 of the Bose Wearable SDK to v4.

## Breaking changes

BoseWearable-Android now requires minimum Android SDK version (minSdkVersion) 22 (LOLLIPOP_MR1). v3 required at least API 21 (LOLLIPOP).

BLEcore (com.bose.wearable:blecore) now requires a new dependency, Tasks API from Google Play services. Please update your application's dependencies and add the following dependency:
```text
implementation 'com.google.android.gms:play-services-tasks:16.0.1'
```

Bosewearable-UI (com.bose.wearable:bosewearableui) now requires new dependencies. Please update your application's dependencies and add the following dependencies:
```text
implementation 'androidx.lifecycle:lifecycle-extensions:2.0.0'
implementation 'com.google.android.material:material:1.1.0-alpha06'
```

If your application implements `WearableDeviceListener`, the following changes are necessary:
```java
// Add the 'SensorsSuspensionReason' argument
void onSensorsSuspended(@NonNull SensorsSuspensionReason suspensionReason) {
}

// Add a new listener method
void onDevicePropertiesRead(@NonNull DeviceProperties deviceProperties) {
}
```

## User Interface

Bose provided the `DeviceSearchFragment`. It would perform a device search and present UI to the user allowing the selection of a device. As a result, it would return the address of a `DiscoveredDevice`. It was the responsibility of the caller to open the session.

In the v4 SDK, Bose has provided `DeviceConnectorActivity` and deprecated `DeviceSearchFragment`. This `Activity` presents an entirely new user interface for device search. It also guides the user through the secure pairing process, if required. Finally, it checks to see whether a firmware update is available and directs the user to the appropriate app to perform the firmware update. The return value is still the address of a `DiscoveredDevice`, but now there is an open `Session` for the device, eliminating the requirement to open the session from the client app.

### Continuing to use the v3 API

We encourage all developers to switch over to the new `DeviceConnectorActivity` API. The old `DeviceSearchFragment` API will be removed from a future release of the SDK. If you want to continue using the old `DeviceSearchFragment` API, you need to handle secure pairing when connecting to devices that require a bonded connection.

To do so, you must register a `SessionBondingDelegate` instance with the `Session` before calling `open()`. Failing to do so will result in a fatal error.

When `SessionBondingDelegate.bondingRequired(Session)` is called, you must present user interface prompting the user to place the device into pairing mode. The process of opening the `Session` will wait indefinitely for the user to place the device into pairing mode.

When `SessionBondingDelegate.bondingInProgress(Session)` is called, you must dismiss the user interface prompt provided above.

Using `DeviceConnectorActivity` in the v4 API handles all of this for you.

### Migrating to the v4 API

Replace your call to create and present `DeviceSearchFragment` with a call to start `DeviceConnectorActivity`. You no longer need to open the session for the returned `DiscoveredDevice` when the operation is successful. You do not need to register a `SessionBondingDelegate` with the `Session`.

For example:

```java
public class MainActivity extends AppCompatActivity {
    private static final int AUTO_CONNECT_TIMEOUT = 5; // In seconds, use 0 to disable automatic reconnection
    private static final int REQUEST_CODE_CONNECTOR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.connectButton)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect();
                }
            });
    }

    private void connect() {
        // Tell the Connector UI which sensors and gestures are required by the app
        Set<SensorType> sensorTypes = new ArraySet<>(
            Arrays.asList(SensorType.ACCELEROMETER,
            SensorType.ROTATION_VECTOR)
        );
        SensorIntent sensorIntent = new SensorIntent(sensorTypes, Collections.singleton(SamplePeriod._20_MS));

        GestureIntent gestureIntent = new GestureIntent(Collections.singleton(GestureType.INPUT));

        // Start the connector Activity
        Intent intent = DeviceConnectorActivity.newIntent(this, AUTO_CONNECT_TIMEOUT,
            sensorIntent, gestureIntent);

        startActivityForResult(intent, REQUEST_CODE_CONNECTOR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONNECTOR) {
            if (resultCode == Activity.RESULT_OK) {
                String deviceAddress = data.getStringExtra(DeviceConnectorActivity.CONNECTED_DEVICE);
                BluetoothManager btManager = BoseWearable.getInstance().bluetoothManager();
                Session session = btManager.session(btManager.deviceByAddress(deviceAddress));
                WearableDevice wearableDevice = (WearableDevice) session.device();

                // session is opened at this point and ready to use.
                // It is up to the application to close the session when it is no longer needed.
            } else if (resultCode == DeviceConnectorActivity.RESULT_SCAN_ERROR) {
                ScanError scanError = (ScanError) data.getSerializableExtra(DeviceConnectorActivity.FAILURE_REASON);
                // An error occurred when searching for a device.
                // Present an error to the user.
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user cancelled the search operation.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
```

## Euler Angle Conversion

The `Quaternion` type in the SDK provides `pitch`, `roll`, and `yaw` properties that are now deprecated. These return the right-handed rotation around the X, Y, and Z axis, respectively, as defined in the Bose Wearable Coordinate System. However, these only work for the raw Quaternion coming from the device. If calibrating or mapping to a different coordinate system, derive your pitch, roll, and yaw in the new coordinate system from right-handed `Quaternion.xRotation()`, `Quaternion.yRotation()`, and `Quaternion.zRotation()`.

For example:

```java
static final Quaternion TRANSLATION_Q = new Quaternion(1, 0, 0, 0);

Quaternion quaternion = Quaternion.multiply(sensorValue.quaternion(), TRANSLATION_Q);

double pitch = quaternion.xRotation();
double roll = -quaternion.yRotation();
double yaw = -quaternion.zRotation();
```

## Device Properties

The Bose Wearable SDK now provides functionality to control the Product name, Active Noise Reduction (ANR) and Controllable Noise Cancellation (CNC) settings on the device. Note that not all devices support all these functions.

See `WearableDevice.deviceProperties()` to get the properties.

See `WearableDevice.changeName(String)`, `WearableDevice.changeAnr(AnrMode)` and `WearableDevice.changeCnc()` to change the properties.

The DataExample app has been updated to demonstrate these features. They are available under Device > Device Properties.

## Firmware Version

During connection establishment, a check is performed to see whether updated firmware is available. The result of this check is automatically presented to the user when using the `DeviceConnectorActivity` API. It is also available via the `FirmwareUpgrade.check(WearableDevice)` function.

## Suspension

As before, devices may suspend the wearable sensor service. On certain devices, certain user-initiated activities cause the wearable sensor service to be suspended due to bandwidth or processing restrictions.

When the service is suspended, you will continue to be sent a `WearableDeviceListener.onSensorsSuspended()`. In the v4 SDK, this event includes an associated `SensorsSuspensionReason` which you can use to inform the user why sensor data is no longer being received.

As before, you will continue to be sent a `WearableDeviceListener.onSensorResumed()` event when the service resumes.

## Intents

An app may specify a set of sensor and gesture intents that describe which sensors, sample periods, and gestures are required. The SDK provides mechanisms to validate these intents to determine whether a device is compatible with the requirements of your app.

You can provide `SensorIntent` and `GestureIntent` objects to the `DeviceConnectorActivity.newIntent()` method to have the intents validated upon connection. If the intents are not met, the connection fails.

Alternatively, you can call `validateIntents(SensorIntent, GestureIntent)` after a connection to validate your intents manually.
