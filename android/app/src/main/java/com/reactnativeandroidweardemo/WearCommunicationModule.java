package com.reactnativeandroidweardemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WearCommunicationModule extends ReactContextBaseJavaModule
  implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener, LifecycleEventListener {

  private final GoogleApiClient googleApiClient;

  public WearCommunicationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addLifecycleEventListener(this);
    googleApiClient = new GoogleApiClient.Builder(getReactApplicationContext()).addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .build();
  }

  @Override
  public void onHostResume() {
    googleApiClient.connect();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Wearable.MessageApi.addListener(googleApiClient, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    Wearable.MessageApi.removeListener(googleApiClient, this);
  }


  /** Increase the wear counter on every node that is connected to this device. */
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

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/increase_phone_counter")) {
      sendEvent(getReactApplicationContext(), "increaseCounter", null);
    }
  }

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  @Override
  public void onHostPause() {

  }

  @Override
  public void onHostDestroy() {
    Wearable.MessageApi.removeListener(googleApiClient, this);
    googleApiClient.disconnect();
  }

  @Override
  public String getName() {
    return "AndroidWearCommunication";
  }
}