package com.example.minu.voicemailtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceOnBoot extends BroadcastReceiver{
    private static final String TAG = "CallRecordTest";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, TService.class);
        context.startService(startServiceIntent);
    }
}
