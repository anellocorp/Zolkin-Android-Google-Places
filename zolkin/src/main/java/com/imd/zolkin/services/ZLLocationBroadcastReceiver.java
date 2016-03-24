package com.imd.zolkin.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by omar on 6/23/15.
 */
public class ZLLocationBroadcastReceiver extends BroadcastReceiver {

    public ZLLocationBroadcastReceiver() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        if(!isFusedServiceRunning(context)){

            //context.startService(new Intent(context, ZLUpdateLocationService.class));
        }

    }

    private boolean isFusedServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (ZLUpdateLocationService.class.getName().equals(service.service.getClassName())) {

                return false;

            }
        }

        return false;
    }
}