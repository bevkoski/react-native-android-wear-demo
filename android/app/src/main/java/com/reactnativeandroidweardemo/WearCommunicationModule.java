package com.reactnativeandroidweardemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.Charset;
import java.util.List;

public class WearCommunicationModule extends ReactContextBaseJavaModule
    implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

  private final GoogleApiClient googleApiClient;

  public WearCommunicationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    googleApiClient = new GoogleApiClient.Builder(getReactApplicationContext())
      .addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .build();
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

  @Override
  public String getName() {
    return "AndroidWearCommunication";
  }

  @ReactMethod
  public void increaseWatchCounter() {
    Toast.makeText(getReactApplicationContext(), "Increase watch counter!", Toast.LENGTH_SHORT).show();
    final List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
    for (Node node : nodes) {
      Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/increase_wear_counter", null);
    }
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/increase_phone_counter")) {
      Toast.makeText(getReactApplicationContext(), "Increase phone counter!", Toast.LENGTH_SHORT).show();
      sendEvent(getReactApplicationContext(), "increaseCounter", null);
    }
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

}