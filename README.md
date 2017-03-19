# React Native-Android Wear Communication Demo

Showcase of how to detect the running state of the counter increment demo apps on both phone an wear device. 

If both apps are running, "Increase phone/wear counter" button will be shown. If one of them isn't started, an "Launch phone/wear app" button will be shown. This button will launch the app on the mentioned device.

## How it works
Instead of just sending message to the nodes on the network, blindly believing that it will be handled by the node, we are using [CapabilityApi](https://developers.google.com/android/reference/com/google/android/gms/wearable/CapabilityApi) to check if there is a capable connected node that can handle the messages we send.

Eventually, if there are no capable nodes, an message is send to connected nodes to launch an app that will handle the event.

### Register a capability on the native module of the React Native app
To register that the React Native app is capable of handling counter increase messages, it is required to register a local capability with call to 
```java
  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Wearable.CapabilityApi.addLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);
  }
```

And when the application is closed, make sure this capability is removed.
```java
@Override
  public void onHostDestroy() {
    Wearable.CapabilityApi.removeLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);
    googleApiClient.disconnect();
  }
```

### Listening for capability from the React Native app
Similar to registering a capability, we register a capability listener when the GoogleApiClient is connected, and unregister it once the application is closed.

```java
@Override
  public void onConnected(@Nullable Bundle bundle) {
    // Listen for capability changes on the network
    Wearable.CapabilityApi.addCapabilityListener(googleApiClient, capabilityListener, WEAR_COUNTER_CAPABILITY);
  }
```

Now when a capability has been registered or unregister on the network, `onCapabilityChanged(CapabilityInfo)` will be triggered. It can be now handled on the native module, or be delegated to the JS module.

And once the app is closing, unregister it:
```java
@Override
  public void onHostDestroy() {
    Wearable.CapabilityApi.removeCapabilityListener(googleApiClient, this, WEAR_COUNTER_CAPABILITY);
    googleApiClient.disconnect();
  }
```

### Starting the React Native app from the wear device
Create service that extends `WearableListenerService` and in the same fashion listen for messages from the MessageApi as in the counter incrementer example, but instead of increasing a counter it starts a new activity.
```java
public class LaunchPhoneAppListenerService extends WearableListenerService {
  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/launch_phone_app")) {
      final Intent intent = new Intent(this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    } else {
      super.onMessageReceived(messageEvent);
    }
  }
}
```

After `LaunchPhoneAppListenerService` is created, it needs to be added in the `AndroidManifest.xml` from the phone app module. It is also required to register it that this service wants to listen to events coming from MessageApi.
```xml
<service android:name=".LaunchPhoneAppListenerService">
  <intent-filter>
    <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED"/>
    <data
      android:host="*"
      android:scheme="wear"/>
  </intent-filter>
</service>
```

### How it is implemented on the wear app
It is exactly the same as on the phone app, so there is no need for duplication in this readme.

Ideally most of this implementation will be implemented one time in a common module that will be a dependency on the other modules that require such functionality.
