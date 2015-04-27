package com.stofa.shopping;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by stofa on 26.04.2015.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notify = intent.getStringExtra("RECEIVER");

        Toast.makeText(context, notify, Toast.LENGTH_SHORT).show();

    }
}
