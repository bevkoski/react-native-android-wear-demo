package com.reactnativeandroidweardemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
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

import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends WearableActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
  MessageApi.MessageListener {
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  private int currentCount = 0;

  private Button btnIncreaseCounter;
  private TextView tvMessage;

  private GoogleApiClient client = null;
  private String node = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnIncreaseCounter = (Button) findViewById(R.id.btnWearIncreaseCounter);
    tvMessage = (TextView) findViewById(R.id.tvMessage);

    client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

    btnIncreaseCounter.setOnClickListener(clickListener);
  }

  private final View.OnClickListener clickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
          Wearable.MessageApi.sendMessage(client, node, "/counter", Integer.toString(currentCount).getBytes());
          // FIXME: 17.03.2017 transform counter directly to byte array using ByteBuffer
        }
      });
    }
  };

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(LOG_TAG, "onConnected: GoogleApiClient successfully connected.");
    Wearable.MessageApi.addListener(client, this);
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        final List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(client).await().getNodes();
        for (Node connectedNode : connectedNodes) {
          if (connectedNode.isNearby()) {
            node = connectedNode.getId();
            btnIncreaseCounter.setEnabled(true);
            break;
          }
        }
        if (TextUtils.isEmpty(node)) {
          Log.e(LOG_TAG, "Failed to connect to a nearby node");
        }
      }
    });
  }

  private void decomposeGoogleApiClient() {
    if (client != null && client.isConnected()) {
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
    decomposeGoogleApiClient();
    super.onStop();
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.e(LOG_TAG, "onConnectionSuspended: " + (i == CAUSE_NETWORK_LOST ? "NETWORK LOST" : "SERVICE_DISCONNECTED"));
    decomposeGoogleApiClient();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e(LOG_TAG, "Connection to GoogleApiClient failed." + connectionResult.getErrorMessage());
    decomposeGoogleApiClient();
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/react_native_message")) {
      tvMessage.setText(new String(messageEvent.getData(), Charset.defaultCharset()));
    }
  }
}
