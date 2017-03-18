package com.reactnativeandroidweardemo;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.List;

public class MainActivity extends WearableActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
  MessageApi.MessageListener {
  private static final String LOG_TAG = MainActivity.class.getSimpleName();
  private static final String PHONE_COUNTER_CAPABILITY = "phone_counter_capability";
  private final static String WEAR_COUNTER_CAPABILITY = "wear_counter_capability";


  /** Counter that stores the current count of the wear module. */
  private int count = 0;

  /**
   * Button used to increase counter on the mobile module. The status of this button is handled by the result of {@link
   * MainActivity.InitNodesTask}.
   */
  private Button btnIncreaseCounter;
  private Button btnLaunchPhoneApp;
  private TextView tvCounter;

  private GoogleApiClient client = null;
  private String node = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnIncreaseCounter = (Button) findViewById(R.id.btnWearIncreaseCounter);
    btnIncreaseCounter.getBackground().setColorFilter(0xFF1194F7, PorterDuff.Mode.MULTIPLY);
    tvCounter = (TextView) findViewById(R.id.tvCounter);
    tvCounter.setText(Integer.toString(count));
    btnLaunchPhoneApp = (Button) findViewById(R.id.btnWearLaunchPhoneApp);

    client = new GoogleApiClient.Builder(this).addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .build();

    btnIncreaseCounter.setOnClickListener(clickListener);
    btnLaunchPhoneApp.setOnClickListener(clickListener);
  }

  private final View.OnClickListener clickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v == btnIncreaseCounter) {
        // Send a message to the found node to increase its counter
        Wearable.MessageApi.sendMessage(client, node, "/increase_phone_counter", null);
      } else if (v == btnLaunchPhoneApp) {
        // Try to launch the phone app
        new LaunchAppTask().execute(client);
      }
    }
  };

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(LOG_TAG, "onConnected: GoogleApiClient successfully connected.");
    Wearable.MessageApi.addListener(client, this);
    // Advertise the capability to handle counter increases
    Wearable.CapabilityApi.addLocalCapability(client, WEAR_COUNTER_CAPABILITY);

    // Register capability listener to know when a capable node is connected/disconnected form the network
    Wearable.CapabilityApi.addCapabilityListener(client, capabilityListener, PHONE_COUNTER_CAPABILITY);
    new InitNodesTask().execute(client);
  }

  /** Handle capability node changes on the network. */
  private final CapabilityApi.CapabilityListener capabilityListener = new CapabilityApi.CapabilityListener() {
    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
      final Collection<Node> capableNodes = capabilityInfo.getNodes();
      if (capableNodes.isEmpty()) {
        enablePhoneAppLaunchControls();
      } else {
        // Enable the phone counter with the first node in the capableNodes received from this listener
        enableCountControls(capableNodes.iterator().next().getId());
      }
    }
  };

  /**
   * This async task will get all the nodes that advertise {@link MainActivity#PHONE_COUNTER_CAPABILITY} and get the
   * first one that is nearby. Further we will communicate only to that node. Additionally will call the appropriate
   * method to {@link MainActivity#enableCountControls(String)} or {@link MainActivity#enablePhoneAppLaunchControls()}.
   */
  private class InitNodesTask extends AsyncTask<GoogleApiClient, Void, String> {
    @Override
    protected String doInBackground(GoogleApiClient... params) {
      final Collection<Node> connectedNodes = Wearable.CapabilityApi.getCapability(params[0], PHONE_COUNTER_CAPABILITY,
        CapabilityApi.FILTER_REACHABLE).await().getCapability().getNodes();
      for (Node connectedNode : connectedNodes) {
        if (connectedNode.isNearby()) {
          return connectedNode.getId();
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(String resultNode) {
      super.onPostExecute(resultNode);
      // Because this runs on the main thread it is safe to change the state of the UI.
      if (resultNode != null) {
        enableCountControls(resultNode);
      } else {
        enablePhoneAppLaunchControls();
      }
    }
  }

  /** Async task that will send messages to every connected node to try to launch the phone application. */
  private class LaunchAppTask extends AsyncTask<GoogleApiClient, Void, List<Node>> {
    @Override
    protected List<Node> doInBackground(GoogleApiClient... params) {
      return Wearable.NodeApi.getConnectedNodes(params[0]).await().getNodes();
    }

    @Override
    protected void onPostExecute(List<Node> nodes) {
      for (Node connectedNode : nodes) {
        if (connectedNode.isNearby()) {
          Wearable.MessageApi.sendMessage(client, connectedNode.getId(), "/launch_phone_app", null);
        }
      }
      super.onPostExecute(nodes);
    }
  }

  /** Enables the controls that will be used to increment the counter on the phone. */
  private void enableCountControls(@NonNull String nodeId) {
    node = nodeId;
    btnIncreaseCounter.setVisibility(View.VISIBLE);
    btnLaunchPhoneApp.setVisibility(View.GONE);
  }

  /** Enables the controls that will be used to launch the counting app on the phone. */
  private void enablePhoneAppLaunchControls() {
    node = null;
    btnIncreaseCounter.setVisibility(View.GONE);
    btnLaunchPhoneApp.setVisibility(View.VISIBLE);
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/increase_wear_counter")) {
      tvCounter.setText(Integer.toString(++count));
    }
  }

  /**
   * Used to disconnect the {@link MainActivity#client} and reset the other fields like the
   * {@link MainActivity#node} and disable the {@link MainActivity#btnIncreaseCounter}.
   */
  private void disconnectGoogleApiClient() {
    if (client != null && client.isConnected()) {
      // Remove the advertisement that this node is capable of handling counter increases
      Wearable.CapabilityApi.removeLocalCapability(client, WEAR_COUNTER_CAPABILITY);
      Wearable.MessageApi.removeListener(client, this);
      client.disconnect();
    }
    node = null;
  }

  @Override
  protected void onStart() {
    super.onStart();
    client.connect();
  }

  @Override
  protected void onStop() {
    disconnectGoogleApiClient();
    super.onStop();
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.e(LOG_TAG, "onConnectionSuspended: " + (i == CAUSE_NETWORK_LOST ? "NETWORK LOST" : "SERVICE_DISCONNECTED"));
    disconnectGoogleApiClient();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e(LOG_TAG, "Connection to GoogleApiClient failed." + connectionResult.getErrorMessage());
    disconnectGoogleApiClient();
  }
}
