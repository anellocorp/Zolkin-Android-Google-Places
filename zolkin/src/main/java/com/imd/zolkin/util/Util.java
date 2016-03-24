package com.imd.zolkin.util;

import android.accounts.NetworkErrorException;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.imd.zolkin.services.ZLServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util
{
	public static final String SHARED_PREFERENCES_FILE = "ChatLocalPrefs.bin";
	public static final String IMAGE_CACHE_DIR = "Images";
	public static final boolean LOG = true;
	protected static final String LOG_TAG = "ChatLocal";

	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
					Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return (dist * meterConversion);
	}
	
	public static String reverseNumberFormat(String num)
	{
		StringBuilder resp = new StringBuilder();
		
		for (int i = 0; i < num.length(); i++)
		{
			if (num.charAt(i) == '.')
				resp.append(',');
			else if (num.charAt(i) == ',')
				resp.append('.');
			else
				resp.append(num.charAt(i));
		}
		return resp.toString();
	}

	public static String prettyPrintDate(Date d)
	{
		try
		{
			SimpleDateFormat df = new SimpleDateFormat("MMMM dd, YYYY - HH:mm");
			return df.format(d);
		}
		catch (Exception e)
		{
			return "";
		}
	}

	public static boolean stringIsNullEmptyOrWhiteSpace(String s)
	{
		if (s == null)
			return true;
		s = s.replace(" ", "");
		return s.equals("");
	}

	public static void crossFade(final View hide, View show)
	{
		show.setTag(null);
		hide.setTag("hide");
		show.setAlpha(0f);
		show.setVisibility(View.VISIBLE);
		show.animate().alpha(1.0f).setDuration(300);
		hide.animate().alpha(0f).setDuration(300).setListener(new AnimatorListener()
		{

			@Override
			public void onAnimationStart(Animator animation)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation)
			{

				// TODO Auto-generated method stub
				if (Util.LOG)
					Log.d(Util.LOG_TAG, hide.getClass().getName());

				if (hide.getTag() != null)
				{
					if (hide.getTag().equals("hide"))
						hide.setVisibility(View.GONE);
				}

			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device
	 * density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on
	 *         device density
	 */
	public static float convertDpToPixel(float dp, Context context)
	{
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method converts device specific pixels to density independent
	 * pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context)
	{
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	public static boolean CheckConnection(Context c)
	{
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected())
		{
			return true;
		}

		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected())
		{
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected())
		{
			return true;
		}
		return false;
	}

	public static void isTransmitting(Context context) {
		int count = 0;
		String str = "";
		final String url = ZLServices.BASE_URL;

		try {
			Process process = null;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
				process = Runtime.getRuntime().exec(
						"/system/bin/ping -w 1 -c 1 " + url);
			} else {
				process = new ProcessBuilder()
						.command("/system/bin/ping", url)
						.redirectErrorStream(true)
						.start();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			StringBuffer output = new StringBuffer();
			String temp;

			while ( (temp = reader.readLine()) != null) {
				output.append(temp);
				count++;
			}

			reader.close();


			if (count > 0)
				str = output.toString();

			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("PING Count", ""+ count);
		Log.i("PING String",  str);
	}
}
