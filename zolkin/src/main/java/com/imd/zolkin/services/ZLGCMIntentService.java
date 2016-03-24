package com.imd.zolkin.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.imd.zolkin.activity.SplashActivity;

import br.com.zolkin.R;


public class ZLGCMIntentService extends IntentService
{
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;

	public ZLGCMIntentService()
	{
		super("ZLGCMIntentService");
	}

	public ZLGCMIntentService(String name)
	{
		super("ZLGCMIntentService");
	}
	public static final String TAG = "ZLGCMIntentService";

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		String key = extras.getString("type");
		String param = extras.getString("key");
		String msg = extras.getString("message");

		if (!extras.isEmpty())
		{ 	if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
			{
				sendNotification("Send error: " + key, param, msg);
			}
			else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
			{
				sendNotification("Deleted messages on server: " + key, param, msg);
			}
			else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
			{

				for (int i = 0; i < 3; i++) {
					Log.i(TAG,
							"Working... " + (i + 1) + "/5 @ "
									+ SystemClock.elapsedRealtime());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}

				}
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

				sendNotification(key, param, msg);
				Log.i(TAG, "Received: " + extras.get("m"));
			}
		}
		ZLGCMBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String key, String param, String msg)
	{
		Log.d(TAG, "Enviando notificação:" + msg);

		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, SplashActivity.class);


		intent.putExtra("PendingActionKey", key);
		intent.putExtra("PendingActionParam", param);
		intent.putExtra("PendingActionMessage", msg);

		if (key.equals("extrato")) {
			intent.setData(Uri.parse("zolkin://" + key + "/" + param + "/" + msg));
		} else
			intent.setData(Uri.parse("zolkin://" + key + "/" + param));

		intent.addFlags(PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle("Zolkin")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).
						setContentText(msg).
						setVibrate(new long[]{100, 200, 100, 500}).
						setSound(soundUri).
						setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.cancelAll();
		mNotificationManager.notify(key + param + msg, NOTIFICATION_ID, mBuilder.build());
	}
}
