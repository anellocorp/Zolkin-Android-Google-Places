package com.imd.zolkin.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLServices;

import java.io.File;
import java.io.IOException;

public class GCMUtils {
    public static final String GCM_TAG = "GCMUtils";

    public static String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "1.0";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected static final String SENDER_ID = "95639131236";

    private static GCMUtils sharedInstance = null;

    private Context c = null;

    public GoogleCloudMessaging gcm;
    public String regid;

    private GCMUtils(Context c) {
        this.c = c;


        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(c);
            regid = getRegistrationId();

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                sendRegistrationIdToBackend(regid);
            }
        }
    }

    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(c);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                ((Activity) c).runOnUiThread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) c, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });

            } else {
                Log.i(GCM_TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public static GCMUtils getInstance(Context c) {
        if (sharedInstance == null)
            sharedInstance = new GCMUtils(c);
        return sharedInstance;
    }

    public String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(GCM_TAG, "Registration not found.");
            try {
                registrationId = gcm.register(SENDER_ID);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_REG_ID, registrationId);
                editor.apply();
                sendRegistrationIdToBackend(registrationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(GCM_TAG, "App version changed.");
            try {
                registrationId = gcm.register(SENDER_ID);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_REG_ID, registrationId);
                editor.apply();
                sendRegistrationIdToBackend(registrationId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i("GCMUtils", registrationId);
        return registrationId;
    }

    public SharedPreferences getGCMPreferences() {
        return c.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerInBackground() {
        new AsyncTask() {
            protected void onPostExecute(Object result) {
                // mDisplay.append(result.toString() + "\n");
            }

            @Override
            protected Object doInBackground(Object... arg0) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(c);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    sendRegistrationIdToBackend(regid);
                    storeRegistrationId(c, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);

    }

    protected void sendRegistrationIdToBackend(String regId) {
        try {
            File tf = new File(c.getFilesDir(), "zolkinpushtoken.txt");
            ReadAndWriteObjectToFile.writeObjectToFile(tf, regId);
        } catch (Exception eee) {
            eee.printStackTrace();
        }

        if (ZLUser.getLoggedUser().isAuthenticated()) {
            ZLServices.getInstance().sendPushToken(regId, false, c, null);
            storeRegistrationId(c,regId);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion();
        Log.i(GCM_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
}