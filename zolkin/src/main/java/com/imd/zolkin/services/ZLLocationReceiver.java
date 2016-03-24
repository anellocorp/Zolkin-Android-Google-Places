package com.imd.zolkin.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class ZLLocationReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context c, Intent arg1) {
		SharedPreferences settings;
        final LocationManager manager = (LocationManager) c.getSystemService(c.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return;

		settings = c.getSharedPreferences("Zolkin-Location", Context.MODE_PRIVATE);
		if (settings.contains("initialized")) {
            if (!isServiceRunning(ZLLocationService.class, c)) {
                c.startService(new Intent(c, ZLLocationService.class));
            }
        } else {
            return;
        }
    }

    @SuppressLint("ShowToast")
    protected boolean isServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Toast.makeText(context,"Serviço já iniciado", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        Toast.makeText(context,"Serviço não iniciado, por erros", Toast.LENGTH_LONG).show();
        return false;
    }
}
