package com.imd.zolkin.activity;
/*
Todas as activities deste app derivam desta.
Serve para o serviço de push e outras funcionalidades saber se e qual activity do Zolkin esta rodando no momento.
*/

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import br.com.zolkin.R;

public class  BaseZolkinActivity extends FragmentActivity
{

	public static AppEventsLogger logger = null;

	//Guarda a Activity que esta na frente no momento. Se for != null, o app esta rodando em foreground.
	public static Activity latestActivity = null;

	static boolean isVisualyzed = false;
	protected void onCreate(android.os.Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (!isVisualyzed) {
			isVisualyzed = true;
			SharedPreferences settings;
			SharedPreferences.Editor editor;
			settings = getApplicationContext().getSharedPreferences("Zolkin-Location-Settings", Context.MODE_PRIVATE);
			editor = settings.edit();
			editor.putString("LocationService", "não");
			editor.apply();
		}

		latestActivity = this;

		AppEventsLogger.activateApp(this);
		AppEventsLogger.setFlushBehavior(AppEventsLogger.FlushBehavior.AUTO);

		Bundle bundle = new Bundle();

		logger = AppEventsLogger.newLogger(this, getResources().getString(R.string.app_id_0));

		logger.logEvent("App Lauched Event", bundle);
		logger.flush();


	}

	@Override
	protected void onResume()
	{
		super.onResume();

		latestActivity = this;

	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
