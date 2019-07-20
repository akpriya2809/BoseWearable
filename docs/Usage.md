# Usage

This document describes how to use the Bose Wearable SDK in your app.

- [Copy AARs to your project](#copy-aars-to-your-project)
- [Configuring and Initializing the Library](#configuring-and-initializing-the-library)
- [Scanning and connecting to a device](#scanning-and-connecting-to-a-device)
- [Listening for Sensor Data](#listening-for-sensor-data)
- [Listening for Gestures](#listening-for-gestures)

## Copy AARs to your project

### Import AARs to your project

Copy the AAR files from `aar/` directory to your project:

```shell
# Assuming you are at the root of your own project

$ mkdir app/aars
$ cp $BoseWearable-Android/aar/blecore-release.aar app/aars
$ cp $BoseWearable-Android/aar/bosewearable-release.aar app/aars/
$ # Optional, if you want to use the device scan/search Fragment
$ cp $BoseWearable-Android/aar/bosewearableui-release.aar app/aars/
```

Edit `app/build.gradle` and add the following repository source and dependencies:

```text
...
repositories {
    flatDir {
        dirs "aars/"
    }
}

dependencies {
...
    implementation 'com.google.android.gms:play-services-tasks:16.0.1'
    implementation 'com.bose.wearable:blecore:release@aar'
    implementation 'com.bose.wearable:bosewearable:release@aar'

    // optionally bosewearableui with its dependencies (recommended)
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'androidx.recyclerview:recyclerview:1.0.0'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.0.0'
    implementation 'com.google.android.material:material:1.1.0-alpha06'
    implementation 'com.bose.wearable:bosewearableui:release@aar'
}
...
```

Then re-sync the project.

## Configuring and Initializing the Library

Before using the Bose Wearable library, you will need to call `BoseWearable.configure(Context appContext)`. We recommend doing so in your `Application` subclass.

```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BoseWearable.configure(this);
    }
}
```

The `BoseWearable.configure()` method takes an optional RSSI cut off configuration parameter. See the documentation for more details.

## Scanning and connecting to a device

In order to connect to a device, it needs to be scanned first. There are two ways to do it:

### With `bosewearableui`

This is the recommended way of selecting and connecting to a remote device. From your `Activity` or
`Fragment`, start the `DeviceConnectorActivity`. The results are reported back using `onActivityResult()` class method.
Here's an example of the whole process:

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

Once you receive an open Session, install a listener to detect errors and lost connection:

```java
session.callback(new SessionDelegate() {
    @Override
    public void sessionConnected(@NonNull Session session) {
        // Not needed, the session is already open
    }

    @Override
    public void sessionError(@NonNull DeviceException exception) {
        // Not needed, the session is already open
    }

    @Override
    public void sessionClosed(int statusCode) {
        // Session closed. The status codes are coming from Android Bluetooth stack. 
        // statusCode == 0 indicates the session closing was initiated by the application. 
        // Other values are error conditions to indicate lost connection. 
    }
});
```

### With custom UI dialog

In some cases, it might be desirable to implement scanning and device connection using a custom UI.
In order to do so, there are quite a few steps that need to be taken:
* Check that Bluetooth hardware exists and is enabled
* Check that the required permissions are granted
* Scan for Bluetooth devices
* When a device is selected from scan results, create and open a `Session`
* Detect and handle devices which require firmware update in order to use Bose AR
* Handle bonding (also known as secure pairing)

Here is an example how to programmatically scan for a device:  

```java
// Get the instance of BoseWearable
BoseWearable boseWearable = BoseWearable.getInstance();

// Define a listener for receiving scan events
ScanListener listener = new ScanListener() {
    @Override
    public void onDeviceFound(@NonNull DiscoveredDevice device) {
        // A device was found.
    }

    @Override
    public void onDeviceRemoved(@NonNull DiscoveredDevice device) {
        // The device has not been seen in scan results for the specified timeout.
    }

    @Override
    public void onError(@NonNull ScanError error) {
        // An error occurred and the scanning has been stopped.
    }
};

// Start Scanning
boseWearable.bluetoothManager().startScanning(15, TimeUnit.SECONDS, listener);

// ...
// When the device is found
boseWearable.bluetoothManager().stopScanning();
```

Once a `DiscoveredDevice` instance is selected, a session can be opened to it:

```java
Session session = BoseWearable.getInstance()
    .bluetoothManager()
    .session(device);

// Set a delegate to get notified when the Session is opened and closed
session.callback(new SessionDelegate() {
    @Override
    public void sessionConnected(@NonNull Session session) {
        // Session opened successfully. You can now start listening for sensor data.
        // Make sure to .close() the Session when it is no longer needed.
        WearableDevice wearableDevice = (WearableDevice) session.device();
    }

    @Override
    public void sessionError(@NonNull DeviceException exception) {
        // The session failed to open. Present the error to the user

        // When the exception is an instance of FirmwareUpdateRequiredException, it means the remote
        // device' firmware must be upgraded in order to use it for Bose AR.
        
        // exception.code() == DeviceException.UNSUPPORTED_DEVICE means that the remote device is
        // not supported by Bose AR
    }

    @Override
    public void sessionClosed(int statusCode) {
        // Session closed.
    }
});

// Set another delegate to get notified when bonding is required
session.bondingDelegate(new SessionBondingDelegate() {
    @Override
    public void bondingRequired(@NonNull Session session) {
        // Show some UI to the user requesting them to put the remote device to pairing mode.
    }

    @Override
    public void bondingInProgress(@NonNull final Session session) {
        // Notifies that the remote device was put to pairing mode and the pairing mode request
        // dialog should be dismissed.   
    }
});

// Finally, open the Session
session.open();
```

## Listening for Sensor Data

All sensors are disabled by default.

```java
// Add a listener for sensor events
WearableDeviceListener listener = new BaseWearableDeviceListener() {
    @Override
    public void onSensorConfigurationRead(@NonNull SensorConfiguration sensorConfiguration) {
        // Sensor configuration has been updated.
    }

    @Override
    public void onSensorConfigurationChanged(@NonNull SensorConfiguration sensorConfiguration) {
        // Sensor configuration change was accepted.
    }

    @Override
    public void onSensorConfigurationError(@NonNull BoseWearableException wearableException) {
        // Sensor configuration change was rejected with the specified exception.
    }

    @Override
    public void onSensorDataRead(@NonNull SensorValue sensorData) {
        switch (sensorData.sensorType()) {
            case ACCELEROMETER:
                // Handle accelerometer reading
                break;
            case GYROSCOPE:
                // Handle gyroscope reading
                break;
        }
    }
};

wearableDevice.addListener(listener);

// Enable accelerometer and gyroscope
SamplePeriod samplePeriod = SamplePeriod._40_MS;
SensorConfiguration configuration = wearableDevice.sensorConfiguration()
    .disableAll()
    .enableSensor(SensorType.ACCELEROMETER, samplePeriod)
    .enableSensor(SensorType.GYROSCOPE, samplePeriod);
wearableDevice.changeSensorConfiguration(configuration);
```

## Listening for Gestures

All gestures are disabled by default.

```java

// Add a listener for gesture events
WearableDeviceListener listener = new BaseWearableDeviceListener() {
    @Override
    public void onGestureConfigurationRead(@NonNull GestureConfiguration gestureConfiguration) {
        // Gesture configuration has been updated,
    }

    @Override
    public void onGestureConfigurationChanged(@NonNull GestureConfiguration gestureConfiguration) {
        // Gesture configuration change was accepted.
    }

    @Override
    public void onGestureConfigurationError(@NonNull BoseWearableException wearableException) {
        // Gesture configuration change was rejected with the specified exception.
    }

    @Override
    public void onGestureDataRead(@NonNull GestureData gestureData) {
        // Gesture received.
    }
};

wearableDevice.addListener(listener);

// Enable double tap gesture
GestureConfiguration config = wearableDevice.gestureConfiguration()
   .disableAll()
   .gestureEnabled(GestureType.DOUBLE_TAP, true);
wearableDevice.changeGestureConfiguration(config);
```
