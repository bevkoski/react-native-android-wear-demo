package com.reactnativeandroidweardemo;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


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
