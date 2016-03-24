package com.imd.zolkin.activity;

//Tela de mudar location do app

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GestureMapView.GestureMapViewTap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.imd.zolkin.adapter.ChangeLocationAddressAdapter;
import com.imd.zolkin.adapter.PlacesAutocompleteAdapter;
import com.imd.zolkin.model.GoogleGeocodeAddress;
import com.imd.zolkin.services.GoogleGeocodeService;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.zolkin.R;

public class ChangeLocationActivity extends BaseZolkinActivity implements GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener
{

	ImageView btVoltar = null;
	FrameLayout flBtGpsLocation = null, flBtClose;
	EditText etSearch = null;
	ListView lvAddresses = null;
	com.google.android.gms.maps.GestureMapView mapView = null;

	ChangeLocationAddressAdapter adapter;
	private HashMap<Marker, GoogleGeocodeAddress> markersMap;
	private ArrayList<Marker> markers;
	private LocationManager manager;
	private Location location;

	GoogleApiClient mGoogleApiClient;
	AutoCompleteTextView mAutocompleteView;
	PlacesAutocompleteAdapter mAdapter;
	AutocompleteFilter mFilter;

	/* Modificadores para a construção do acesso */

	private String bestProvider;
	long minTime = 0, bestTime = 0;
	float bestAccuracy = Float.MAX_VALUE;
	Location bestResult = null;


	private static final String TAG = ChangeLocationActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_location);

		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		flBtGpsLocation = (FrameLayout) findViewById(R.id.flBtGpsLocation);
		flBtClose = (FrameLayout) findViewById(R.id.flBtClose);
		etSearch = (EditText) findViewById(R.id.etSearch);
		lvAddresses = (ListView) findViewById(R.id.lvAddresses);
		mapView = (com.google.android.gms.maps.GestureMapView) findViewById(R.id.mapView);


		mFilter = new AutocompleteFilter.Builder()
				.setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, 0, this)
				.addApi(Places.GEO_DATA_API)
				.build();

		mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete_location);
		mAdapter = new PlacesAutocompleteAdapter(this, mGoogleApiClient, null, mFilter);
		mAutocompleteView.setAdapter(mAdapter);
		mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

		//inicializa o mapa
		mapView.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());
		mapView.setClickable(true);
		
		//depois de mudar de location, a Lista vai precisar recarregar seus dados
		ListaActivity.needReload = true;
		
		//limpar o texto quando clica no X
		flBtClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				etSearch.setText("");
			}
		});
		
		//quando tocar no mapa, muda a location para o local escolhido
		mapView.setTapCallback(new GestureMapViewTap() {
			@Override
			public void tapDetected(LatLng location) {
				//pega o endereço relativo à latitue e longitude escolhidas
				reverseGeocode(location.latitude, location.longitude);
			}
		});

		btVoltar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		//busca de endereço
		etSearch.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{

				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
					return false;

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

				if (!Util.stringIsNullEmptyOrWhiteSpace(etSearch.getText().toString()))
				{
					GoogleGeocodeService.geocode(etSearch.getText().toString(), true, ChangeLocationActivity.this, new ZLServiceOperationCompleted<List<GoogleGeocodeAddress>>()
					{

						@Override
						public void operationCompleted(ZLServiceResponse<List<GoogleGeocodeAddress>> response)
						{
							if (response.errorMessage != null)
							{
								Toast.makeText(ChangeLocationActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
							}
							else
							{
								adapter = new ChangeLocationAddressAdapter(ChangeLocationActivity.this, response.serviceResponse);
								lvAddresses.setAdapter(adapter);
								rebuildMap();
							}
						}
					});
				}

				return true;
			}
		});


		//ao selecionar um endereço na lista
		lvAddresses.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3)
			{
				//zoom no mapa para o endereço selecionado
				GoogleGeocodeAddress addr = adapter.getItem(pos);
				zoomMapToLocation(addr.latitude, addr.longitude);
				
				
				//mostra alerta informando que a location vai ser alterada
				new AlertDialog.Builder(ChangeLocationActivity.this).setTitle("Atenção")
				.setMessage("A sua localização vai ser alterada para o endereço selecionado")
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// set address as current
						GoogleGeocodeAddress addr = adapter.getItem(pos);
						ZLLocationService.setCustomAddress(addr);
						
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(ChangeLocationActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							adapter.getItem(pos).addAddressComponentsToJsonObject(props);
							mixPanel.track("ChangeLocation", props);
						}
						catch (Exception e)
						{

						}
						
						//volta para a tela de lista
						finish();
					}
				})
				.setNegativeButton("Cancelar", null)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			}
		});

		flBtGpsLocation.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//remove a localização customizada
				Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				try {
					List<Address> addresses =
                            geocoder.getFromLocation(ZLLocationService.getLatitude(),ZLLocationService.getLongitude(), 1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				reverseGeocode(ZLLocationService.getLatitude(), ZLLocationService.getLongitude());
				etSearch.setText("Localização Atual: Lat: " + ZLLocationService.getLatitude() + "    Lon: " + ZLLocationService.getLongitude());
			}
		});
		
		//reverseGeocode(ZLLocationService.getLatitude(), ZLLocationService.getLongitude());
	}

	void reverseGeocode(double la, double lo)
	{
		if (la == 0 && lo == 0)
		{
			return;
		}
		GoogleGeocodeService.reverseGeocode(la, lo, true, ChangeLocationActivity.this, new ZLServiceOperationCompleted<List<GoogleGeocodeAddress>>()
		{

			@Override
			public void operationCompleted(ZLServiceResponse<List<GoogleGeocodeAddress>> response)
			{
				if (response.errorMessage != null)
				{
					Toast.makeText(ChangeLocationActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
				}
				else
				{
					adapter = new ChangeLocationAddressAdapter(ChangeLocationActivity.this, response.serviceResponse);
					lvAddresses.setAdapter(adapter);
					rebuildMap();
				}
			}
		});
	}

	
	//zoom no mapa para uma location escolhida
	void zoomMapToLocation(double lat, double lng)
	{
		GoogleMap map = mapView.getMap();
		
		if (map == null)
			return;

		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17);
		map.animateCamera(cu);
	}
	
	
	//mostra a lista de endereços no mapa
	void rebuildMap()
	{

		if (adapter == null)
			return;
		try
		{
			GoogleMap map = mapView.getMap();

			//verifica se o mapa já foi carregado
			if (map != null)
			{
				//limpa os pinos antigos
				map.clear();

				markersMap = new HashMap<Marker, GoogleGeocodeAddress>();

				markers = new ArrayList<Marker>();

				//adiciona os novos pinos
				for (int i = 0; i < adapter.getCount(); i++)
				{
					GoogleGeocodeAddress a = adapter.getItem(i);

					Marker m = null;
					m = map.addMarker(new MarkerOptions().position(new LatLng(a.latitude, a.longitude)).title(a.formattedAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_zolkin)));

					markersMap.put(m, a);
					markers.add(m);
				}

				//zoom no mapa para que todos os locais estajam visiveis
				if (adapter.getCount() > 1)
				{
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Marker marker : markers)
					{
						builder.include(marker.getPosition());
					}
					LatLngBounds bounds = builder.build();
					int padding = 50; // offset from edges of the

					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
					map.animateCamera(cu);
				}
				else if (adapter.getCount() > 0)
				{
					CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 17);
					map.animateCamera(cu);
				}
			}
		}
		catch (Exception e)
		{
			if (Util.LOG)
				Log.d("ChatLocal", "mpa icons error: " + e.toString());
		}
	}// end RebuildMap()

	@Override
	protected void onResume()
	{
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mapView.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();

		mapView.onLowMemory();
	}

	private AdapterView.OnItemClickListener mAutocompleteClickListener
			= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final AutocompletePrediction item = mAdapter.getItem(position);
			final String placeId = item.getPlaceId();
			final CharSequence primaryText = item.getPrimaryText(null);

			Log.i(TAG, "Autocomplete item selected: " + primaryText);

			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(mGoogleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

			Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
		}
	};

	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
			= new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
				places.release();
				return;
			}
			final Place place = places.get(0);

			Log.i(TAG, "Place details received: " + place.getName());

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);

			if (!Util.stringIsNullEmptyOrWhiteSpace(mAutocompleteView.getText().toString()))
				GoogleGeocodeService.geocode(mAutocompleteView.getText().toString(), true, ChangeLocationActivity.this, new ZLServiceOperationCompleted<List<GoogleGeocodeAddress>>() {

					@Override
					public void operationCompleted(ZLServiceResponse<List<GoogleGeocodeAddress>> response) {
						if (response.errorMessage != null) {
							Toast.makeText(ChangeLocationActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							adapter = new ChangeLocationAddressAdapter(ChangeLocationActivity.this, response.serviceResponse);
							rebuildMap();
							new AlertDialog.Builder(ChangeLocationActivity.this).setTitle("Atenção")
									.setMessage("A sua localização vai ser alterada para o endereço selecionado")
									.setPositiveButton("OK", new DialogInterface.OnClickListener()
									{

										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											// set address as current
											GoogleGeocodeAddress addr = adapter.getItem(0);
											ZLLocationService.setCustomAddress(addr);
											//volta para a tela de lista
											finish();
										}
									})
									.setNegativeButton("Cancelar", null)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.show();
						}
					}
				});

			places.release();
		}
	};

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());

		Toast.makeText(this,
				"Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPlaceSelected(Place place) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);

		if (!Util.stringIsNullEmptyOrWhiteSpace(mAutocompleteView.getText().toString()))
			GoogleGeocodeService.geocode(mAutocompleteView.getText().toString(), true, ChangeLocationActivity.this, new ZLServiceOperationCompleted<List<GoogleGeocodeAddress>>() {

				@Override
				public void operationCompleted(ZLServiceResponse<List<GoogleGeocodeAddress>> response) {
					if (response.errorMessage != null) {
						Toast.makeText(ChangeLocationActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
					} else {
						adapter = new ChangeLocationAddressAdapter(ChangeLocationActivity.this, response.serviceResponse);
						rebuildMap();
						new AlertDialog.Builder(ChangeLocationActivity.this).setTitle("Atenção")
								.setMessage("A sua localização vai ser alterada para o endereço selecionado")
								.setPositiveButton("OK", new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										// set address as current
										GoogleGeocodeAddress addr = adapter.getItem(0);
										ZLLocationService.setCustomAddress(addr);
										//volta para a tela de lista
										finish();
									}
								})
								.setNegativeButton("Cancelar", null)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
					}
				}
			});

	}

	@Override
	public void onError(Status status) {

	}
} // end ChangeLocationActivity
