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
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.List;

public class WearCommunicationModule extends ReactContextBaseJavaModule
  implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener, LifecycleEventListener,
  CapabilityApi.CapabilityListener {
  private final static String PHONE_COUNTER_CAPABILITY = "phone_counter_capability";
  private final static String WEAR_COUNTER_CAPABILITY = "wear_counter_capability";

  private final GoogleApiClient googleApiClient;
  private String capableNode = null;

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
    // Listen for capability changes on the network
    Wearable.CapabilityApi.addCapabilityListener(googleApiClient, this, WEAR_COUNTER_CAPABILITY);

    // Advertise the PHONE_COUNTER_CAPABILITY on the network
    Wearable.CapabilityApi.addLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);

    // Get capable node that is already on the network with advertised WEAR_COUNTER_CAPABILITY.
    final Collection<Node> capableNodes = Wearable.CapabilityApi.getCapability(googleApiClient, WEAR_COUNTER_CAPABILITY,
      CapabilityApi.FILTER_REACHABLE).await().getCapability().getNodes();
    handleCapableNodes(capableNodes);
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

  /** Tries to launch the wear app. */
  @ReactMethod
  public void launchWearApp() {
    final List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
    for (Node connectedNode : connectedNodes) {
      Wearable.MessageApi.sendMessage(googleApiClient, connectedNode.getId(), "/launch_wear_app", null);
    }
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/increase_phone_counter")) {
      sendEvent(getReactApplicationContext(), "increaseCounter", null);
    }
  }

  @Override
  public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
    handleCapableNodes(capabilityInfo.getNodes());
  }

  private void handleCapableNodes(Collection<Node> capableNodes) {
    if (capableNodes.isEmpty()) {
      sendEvent(getReactApplicationContext(), "enableWearAppLaunchControls", null);
    } else {
      sendEvent(getReactApplicationContext(), "enableCountControls", null);
      capableNode = capableNodes.iterator().next().getId();
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
    Wearable.CapabilityApi.removeLocalCapability(googleApiClient, PHONE_COUNTER_CAPABILITY);
    googleApiClient.disconnect();
  }

  @Override
  public String getName() {
    return "AndroidWearCommunication";
  }
}