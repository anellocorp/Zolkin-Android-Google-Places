package com.imd.zolkin.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class ZLUpdateLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

	private Context context;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private double latitudeAtual;
	private double longitudeAtual;
	private final String TAG = "FusedLocationService";
	private long timeElapsed;

	public Location location;

	public ZLUpdateLocationService() {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
	}

	@Override
	public void onDestroy() {
		mGoogleApiClient.disconnect();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		CheckPlayServices();

		Log.i(TAG,"FusedLocationService");

		return super.onStartCommand(intent, flags, startId);
	}

	public boolean CheckPlayServices() {

		int response = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

		if (response == ConnectionResult.SUCCESS) {

			mGoogleApiClient = new GoogleApiClient.Builder(context, this, this)
					.addApi(LocationServices.API)
					.build();

			createLocationRequest();

			mGoogleApiClient.connect();

			Log.i(TAG,"Entered");

			return true;
		} else

			return false;
	}

	private LocationRequest createLocationRequest() {

		Log.i(TAG, "Request");

		/*locationRequest = new LocationRequest();
		locationRequest.setInterval(1000 * 60 * 5); //pega localização a cada 10 minutos - 600000 1000 * 60 * 5
		locationRequest.setFastestInterval(1000 * 60 * 5); //pega localização com um mínimo de 5 minutos de intervalo - 300000
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/

		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		return mLocationRequest;
	}

	@Override
	public void onConnected(Bundle bundle) {

		/*Log.i("fretz.trucker.services","Conectado");

		LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
		location = LocationServices.FusedLocationApi.getLastLocation(locationClient);
		if(location != null) {
			latitudeAtual = location.getLatitude();
			longitudeAtual = location.getLongitude();
			if (timeElapsed == 0)
				timeElapsed = new Date().getTime();
		}*/

		LocationRequest mLocationRequest = createLocationRequest();
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
		mGoogleApiClient.disconnect();

	}

	private String getReason(int i) {
		switch (i) {
			case ConnectionResult.API_UNAVAILABLE:
				return "API UNAVAILABLE";
			case ConnectionResult.CANCELED:
				return "CANCELED";
			case ConnectionResult.DEVELOPER_ERROR:
				return "DEVELOPER ERROR";
			case ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED:
				return "DRIVE EXTERNAL STORAGE REQUIRED";
			case ConnectionResult.INTERNAL_ERROR:
				return "INTERNAL ERROR";
			case ConnectionResult.INTERRUPTED:
				return "INTERRUPTED";
			case ConnectionResult.INVALID_ACCOUNT:
				return "INVALID ACCOUNT";
			case ConnectionResult.LICENSE_CHECK_FAILED:
				return "LICENSE CHECK FAILED";
			case ConnectionResult.NETWORK_ERROR:
				return "NETWORK ERROR";
			case ConnectionResult.RESOLUTION_REQUIRED:
				return "RESOLUTION REQUIRED";
			case ConnectionResult.SERVICE_DISABLED:
				return "SERVICE DISABLED";
			case ConnectionResult.SERVICE_INVALID:
				return "SERVICE INVALID";
			case ConnectionResult.SERVICE_MISSING:
				return "SERVICE MISSING";
			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
				return "SERVICE VERSION UPDATE REQUIRED";
			case ConnectionResult.SIGN_IN_REQUIRED:
				return "SIGN IN REQUIRED";
			case ConnectionResult.TIMEOUT:
				return "TIMEOUT";
			default:
				return null;
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.e(TAG, "Conexão GPS suspensa: " + getReason(i));
	}

	@Override
	public void onLocationChanged(Location location) {

		try {

			SharedPreferences universalPreferences = context.getSharedPreferences("universal", Context.MODE_PRIVATE);
			double lat = Double.parseDouble(universalPreferences.getString("latitude", "0"));
			double lon = Double.parseDouble(universalPreferences.getString("longitude","0"));

			Location pointA = new Location("PA");
			pointA.setLatitude(lat);
			pointA.setLongitude(lon);

			float dist = pointA.distanceTo(location);

			//Toast.makeText(getApplicationContext(), "distance " + dist, Toast.LENGTH_LONG).show();

			if (dist > 50.0 || lat == 0.0 || lon == 0.0)
			{

				ZLServices.getInstance().sendLocation(false, location, getApplicationContext(), new ZLServiceOperationCompleted<String>()
				{

					@Override
					public void operationCompleted(ZLServiceResponse<String> response)
					{

					}
				});
			}

		}
		catch (Exception ex){
			Log.e(TAG, ex.getMessage() == null ? "" : ex.getMessage());
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e("FusedLocationService", "Conexão Falhou: " + getReason(connectionResult.getErrorCode()));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
