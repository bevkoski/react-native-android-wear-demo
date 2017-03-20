# React Native-Android Wear Communication Demo

Showcase of a two-way detection of running state between a React Native app and an Android Wear app using the [CapabilityApi](https://developers.google.com/android/reference/com/google/android/gms/wearable/CapabilityApi).

When both apps are running, "Increase phone/wear counter" button is  shown. When one of them isn't started, a "Launch phone/wear app" button is shown. This button launches the app on the mentioned device.

## How it works
Instead of just sending a message to the nodes in the network, blindly believing that it will be handled by some node, we are using the [CapabilityApi](https://developers.google.com/android/reference/com/google/android/gms/wearable/CapabilityApi) to check if there is a capable node that can handle the message we send.

When there are no capable nodes, a message is sent to the connected nodes in order to launch an app that will handle future messages.

#### Registering capability in the native module of the React Native app
In order to register that the React Native app is capable of handling counter increase messages, the [CapabilityApi.addLocalCapability](https://developers.google.com/android/reference/com/google/android/gms/wearable/CapabilityApi.html#addLocalCapability) method is invoked.

```java
@Override
public void onConnected(@Nullable Bundle bundle) {
  Wearable.CapabilityApi.addLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);
}
```

When the application is closed, we make sure that this capability is removed.

```java
@Override
public void onHostDestroy() {
  Wearable.CapabilityApi.removeLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);
  googleApiClient.disconnect();
}
```

#### Listening for capability from the React Native app
Similar to registering capability, we register a listener when the `GoogleApiClient` is connected, and unregister it once the application is closed.

```java
@Override
public void onConnected(@Nullable Bundle bundle) {
  // Listen for capability changes on the network
  Wearable.CapabilityApi.addCapabilityListener(googleApiClient, capabilityListener, WEAR_COUNTER_CAPABILITY);
}
```

When capability has been registered or unregistered in the network, `onCapabilityChanged(CapabilityInfo)` will be triggered. It can then be handled from the native module or delegated to the JavaScript thread.

Before the app is closed, we unregister it via `removeCapabilityListener`.
```java
@Override
public void onHostDestroy() {
  Wearable.CapabilityApi.removeCapabilityListener(googleApiClient, this, WEAR_COUNTER_CAPABILITY);
  googleApiClient.disconnect();
}
```

#### Starting the React Native app from the wearable device
We create a service that extends `WearableListenerService` and in the same fashion listen for messages from the MessageApi. Instead of increasing the counter, a new activity is started.
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

The `LaunchPhoneAppListenerService` needs to be registered in the `AndroidManifest.xml` of the phone app module. It is also required to declare that this service will listen to events coming from the MessageApi.
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

#### How is it implemented in the wear app
It is implemented exactly the same as in the phone app. Ideally, most of this implementation would be implemented in a common module that would be a dependency of all the other modules that require such functionality.
