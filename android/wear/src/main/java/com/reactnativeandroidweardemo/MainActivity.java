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
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends WearableActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
  MessageApi.MessageListener {
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  /** Counter that stores the current count of the wear module. */
  private int count = 0;

  /**
   * Button used to increase counter on the mobile module. The status of this button is handled by the result of {@link
   * MainActivity.InitNodesTask}.
   */
  private Button btnIncreaseCounter;
  private TextView tvMessage;

  private GoogleApiClient client = null;
  private String node = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnIncreaseCounter = (Button) findViewById(R.id.btnWearIncreaseCounter);
    btnIncreaseCounter.getBackground().setColorFilter(0xFF1194F7, PorterDuff.Mode.MULTIPLY);
    tvMessage = (TextView) findViewById(R.id.tvMessage);
    tvMessage.setText(Integer.toString(count));

    client = new GoogleApiClient.Builder(this).addApi(Wearable.API)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .build();

    btnIncreaseCounter.setOnClickListener(clickListener);
  }

  private final View.OnClickListener clickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      // Send a message to the found node to increase its counter
      Wearable.MessageApi.sendMessage(client, node, "/increase_phone_counter", new byte[0]);
    }
  };

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(LOG_TAG, "onConnected: GoogleApiClient successfully connected.");
    Wearable.MessageApi.addListener(client, this);
    new InitNodesTask().execute(client);
  }

  /**
   * This async task will get all the connected nodes and get the first one that is nearby. Further we will communicate
   * only to that node. Additionally it will enable/disable the {@link MainActivity#btnIncreaseCounter} based on the
   * result.
   */
  private class InitNodesTask extends AsyncTask<GoogleApiClient, Void, String> {
    @Override
    protected String doInBackground(GoogleApiClient... params) {
      final List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(client).await().getNodes();
      for (Node connectedNode : connectedNodes) {
        if (connectedNode.isNearby()) {
          return connectedNode.getId();
        }
      }
      return null;
    }

    @Override
    /** Because this runs on the main thread it is safe to change the state of the UI.*/
    protected void onPostExecute(String resultNode) {
      super.onPostExecute(resultNode);
      node = resultNode;
      btnIncreaseCounter.setEnabled(resultNode != null);
    }
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/increase_wear_counter")) {
      tvMessage.setText(Integer.toString(++count));
    }
  }

  /**
   * Used to disconnect the {@link MainActivity#client} and reset the other fields like the
   * {@link MainActivity#node} and disable the {@link MainActivity#btnIncreaseCounter}.
   */
  private void disconnectGoogleApiClient() {
    if (client != null && client.isConnected()) {
      Wearable.MessageApi.removeListener(client, this);
      client.disconnect();
    }
    btnIncreaseCounter.setEnabled(false);
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
