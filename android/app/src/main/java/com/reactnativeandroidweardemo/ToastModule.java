package com.reactnativeandroidweardemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToastModule extends ReactContextBaseJavaModule
  implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

  private final GoogleApiClient googleApiClient;
  private static Callback loggingCallback;

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  public ToastModule(ReactApplicationContext reactContext) {
    super(reactContext);
    googleApiClient = new GoogleApiClient.Builder(getReactApplicationContext()).addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .build();
    googleApiClient.connect();
  }

  @Override
  public String getName() {
    return "NativeToastAndroid";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  @ReactMethod
  public void sendMessageToWear(String message) {
    final List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
    for (Node node : nodes) {
      Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/react_native_message", message.getBytes());
    }
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
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/counter")) {
      loggingCallback.invoke(new String(messageEvent.getData(), Charset.defaultCharset()));
    }
  }

  @ReactMethod
  public void registerLoggingCallback(Callback errorCallback, Callback loggingCallback) {
    try {
      ToastModule.loggingCallback = loggingCallback;
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          ToastModule.loggingCallback.invoke("Hello from the native side!");
        }
      }, 2000);
    } catch (IllegalViewOperationException e) {
      errorCallback.invoke(e.getMessage());
    }
  }

}