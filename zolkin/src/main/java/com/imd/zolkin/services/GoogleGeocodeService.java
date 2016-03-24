package com.imd.zolkin.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.imd.zolkin.model.GoogleGeocodeAddress;
import com.imd.zolkin.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoogleGeocodeService
{
	
	static List<GoogleGeocodeAddress> processGoogleLocationResponse(JSONArray ja)
	{
		List<GoogleGeocodeAddress> l = new ArrayList<GoogleGeocodeAddress>();
		for (int i = 0; i < ja.length(); i++)
		{
			try
			{
				l.add(new GoogleGeocodeAddress(ja.getJSONObject(i)));
			}
			catch (JSONException e)
			{

			}
		}
		return l;
	}
	
	public static void geocode(final String address, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<GoogleGeocodeAddress>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<GoogleGeocodeAddress>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<GoogleGeocodeAddress>>>()
		{
			@Override
			protected ZLServiceResponse<List<GoogleGeocodeAddress>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<GoogleGeocodeAddress>> result = new ZLServiceResponse<List<GoogleGeocodeAddress>>();

				String response;
				try
				{
					response = Http.doGetString(String.format("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyDT2RuyuHzCAp7P0GvN3p6ldDHdLqeQnVk&region=br&address=%s", URLEncoder.encode(address, "utf-8")));

					try
					{
						JSONObject jo = new JSONObject(response);
						
						if (jo.has("status"))
						{
							if (jo.getString("status").equals("OK"))
							{
								JSONArray ja = jo.getJSONArray("results");
								result.serviceResponse = processGoogleLocationResponse(ja);
							}
							else
							{
								result.errorMessage = "Erro no Google Maps. Status: " + jo.getString("status");
							}
						}
						else
						{
							result.errorMessage = "Resposta incorreta do Google Maps. Verifique o endereço e tente novamente.";
						}

					}
					catch (Exception e)
					{
						result.errorMessage = response + e.toString();
					}
					
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<GoogleGeocodeAddress>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.HONEYCOMB*/11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end geocode
	
	public static void reverseGeocode(final double lat, final double lng, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<GoogleGeocodeAddress>> callback)
	{
		final ProgressDialog progress;

		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<GoogleGeocodeAddress>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<GoogleGeocodeAddress>>>()
		{
			@Override
			protected ZLServiceResponse<List<GoogleGeocodeAddress>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<GoogleGeocodeAddress>> result = new ZLServiceResponse<List<GoogleGeocodeAddress>>();

				String response;
				try
				{
					NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "us"));
					String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=AIzaSyDT2RuyuHzCAp7P0GvN3p6ldDHdLqeQnVk", nf.format( lat ), nf.format( lng ));
					response = Http.doGetString(url);

					try
					{
						JSONObject jo = new JSONObject(response);
						
						if (jo.has("status"))
						{
							if (jo.getString("status").equals("OK"))
							{
								JSONArray ja = jo.getJSONArray("results");
								result.serviceResponse = processGoogleLocationResponse(ja);
							}
							else
							{
								result.errorMessage = "Erro no Google Maps. Status: " + jo.getString("status");
							}
						}
						else
						{
							result.errorMessage = "Resposta incorreta do Google Maps. Verifique o endereço e tente novamente.";
						}

					}
					catch (Exception e)
					{
						result.errorMessage = response + e.toString();
					}
					
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<GoogleGeocodeAddress>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.HONEYCOMB*/11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end reverse geocode
}
