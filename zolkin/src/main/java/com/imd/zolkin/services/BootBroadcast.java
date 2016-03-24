package com.imd.zolkin.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Omar on 6/12/2015.
 */
public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, ZLLocationService.class);
        context.startService(service);
    }
}