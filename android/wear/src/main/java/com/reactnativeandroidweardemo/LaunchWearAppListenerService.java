package com.reactnativeandroidweardemo;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/** Service used to listen to messages coming from {@link com.google.android.gms.wearable.Wearable#MessageApi}. */
public class LaunchWearAppListenerService extends WearableListenerService {
  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    if (messageEvent.getPath().equals("/launch_wear_app")) {
      final Intent intent = new Intent(this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    } else {
      super.onMessageReceived(messageEvent);
    }
  }
}
