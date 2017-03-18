# React Native-Android Wear Communication Demo

Showcase of an established two-way communication between a React Native app and an Android Wear app using the [MessageAPI](https://developers.google.com/android/reference/com/google/android/gms/wearable/MessageApi).

## Running from Android Studio

#### Clone the repository

`git clone https://github.com/bevkoski/react-native-android-wear-demo.git`

`cd react-native-android-wear-demo`

#### Install dependencies

`yarn` or `npm install`

#### Start the packager

`react-native start`

#### Open the project in Android Studio

1. Start Android Studio
2. Choose "Open an existing Android Studio project"
3. Select the `/react-native-android-wear-demo/android` folder

#### Run the mobile app

1. Connect your Android phone via USB
2. Select the `app` module as a run configuration
3. Run the `app` module
4. Select your phone from the available connected devices

If you get one of the following error messages:

*Could not connect to development server.*

*Could not get BatchedBridge, make sure your bundle is packaged properly.*

Try executing `adb reverse tcp:8081 tcp:8081` from the command line and reloading the app.

#### Run the watch app

1. Connect your Android watch via USB
2. Select the `wear` module as a run configuration
3. Run the `wear` module
4. Select your watch from the available connected devices

## How it works

### React Native to Android Wear communication

In the `index.android.js` file, an increase of the counter located on the watch is triggered via a native module.

```javascript
increaseWearCounter = () => {
  NativeModules.AndroidWearCommunication.increaseWearCounter();
};
```

The native module exposes a `@ReactMethod` named `increaseWearCounter`, in which an `/increase_wear_counter` message is sent to the watch via the [MessageAPI](https://developers.google.com/android/reference/com/google/android/gms/wearable/MessageApi).

```java
@ReactMethod
public void increaseWearCounter() {
  final List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
  if (nodes.size() > 0) {
    for (Node node : nodes) {
      Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/increase_wear_counter", null);
    }
  } else {
    Toast.makeText(getReactApplicationContext(), "No connected nodes found", Toast.LENGTH_LONG).show();
  }
}
```

The watch app overrides the `onMessageReceived` method and when an `/increase_wear_counter` message is received, it updates the `tvCounter` TextView.

```java
@Override
public void onMessageReceived(MessageEvent messageEvent) {
  if (messageEvent.getPath().equals("/increase_wear_counter")) {
    tvCounter.setText(Integer.toString(++count));
  }
}
```

### Android Wear to React Native communication

An increase of the counter located on the phone is triggered via the button displayed in the watch app. When tapped, it sends an `/increase_phone_counter` message using the [MessageAPI](https://developers.google.com/android/reference/com/google/android/gms/wearable/MessageApi).

```
private final View.OnClickListener clickListener = new View.OnClickListener() {
  @Override
  public void onClick(View v) {
    // Send a message to the found node to increase its counter
    Wearable.MessageApi.sendMessage(client, node, "/increase_phone_counter", null);
  }
};
```

The native module overrides the `onMessageReceived` method and when an `/increase_phone_counter` message is received, it emits an `increaseCounter` event to the JavaScript thread.

```java
@Override
public void onMessageReceived(MessageEvent messageEvent) {
  if (messageEvent.getPath().equals("/increase_phone_counter")) {
    sendEvent(getReactApplicationContext(), "increaseCounter", null);
  }
}

private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
  reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
    .emit(eventName, params);
}
```

In the `index.android.js` file, the event is handled via listener.

```javascript
componentWillMount() {
  DeviceEventEmitter.addListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
};

increaseLocalCounter = () => {
  const currentValue = this.state.counter;
  this.setState({
    counter: currentValue + 1
  });
};
```

## Thanks

Special thanks to @toteto for the implementation of the watch app and the extensive contribution to the native parts of the mobile app.
