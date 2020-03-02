package com.friansh.medfokinov;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("frianshLog", "BootUpReceiver onReceive Called");
        Intent notifService = new Intent(context, notificationService.class);
        context.startService(notifService);
    }
}