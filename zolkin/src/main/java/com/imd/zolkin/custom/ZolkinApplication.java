package com.imd.zolkin.custom;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.innovattic.font.TypefaceManager;

import br.com.zolkin.R;

public class ZolkinApplication extends Application
{
	private static Tracker gaTracker;
	private static GoogleAnalytics analytics;

	@Override
	public void onCreate() {
		super.onCreate();
		TypefaceManager.initialize(getApplicationContext(), R.xml.fonts);

	}

	synchronized public Tracker getDefaultTracker() {
		if (gaTracker == null) {
			analytics = GoogleAnalytics.getInstance(this);
			gaTracker = analytics.newTracker(R.xml.analytics);
			analytics.setDryRun(false);
			analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
		}
		return gaTracker;
	}

}
