package com.imd.zolkin.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;

import com.imd.zolkin.model.GoogleGeocodeAddress;

import java.io.FileWriter;
import java.text.ParseException;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.imd.zolkin.services.ZLServices.sendDataToServer;

public class ZLLocationService  extends Service implements LocationListener {

	public static LocationManager locationManager;
	static double latitude;
	static double longitude;
	public static Location location;
	private static final float MIN_DISTANCE_CHANGE_UPDATE = 50; // 100 METROS
	private static final long MIN_TIME_BETWEEN_UPDATE = 252000; // 5 MINUTOS
	private static final float DISTANCE_TO = 100;

	private static GoogleGeocodeAddress customAddress = null;

	private String bestProvider;
	long minTime = 0, bestTime = 0;
	float bestAccuracy = Float.MAX_VALUE;
	Location bestResult = null;


	private void setLocation(Location l) {
		if (l != null) {
			location = l;
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}

	public static void setCustomAddress(GoogleGeocodeAddress addr) {
		customAddress = addr;
	}

	public static boolean isCustomAddressSet() {
		return customAddress != null;
	}

	public static Location getLocation() {
		return location;
	}

	@Override
	public void onLocationChanged(Location loc) {
		//TODO: Checar Localização alterada.
		SharedPreferences settings;
		SharedPreferences.Editor editor;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		settings = getApplicationContext().getSharedPreferences("Zolkin-Location", Context.MODE_PRIVATE);

		// Verifica se existiu algum erro no salvamento da localização e se ocorreu algum erro
		// sai da rotina de locationchanged
		if (settings.getString("latitude","").equals("") && settings.getString("longitude","").equals(""))
			return;

		// Calcula o tempo que foi atualizado a localização, se for menor que 3 minutos, não envia
		// ao servidor
		// Bloco comentado para não ser usado em produção - 27/11/15
		if(calculateTime(settings.getString("time", ""), dateFormat.format(new Date())) < 3) {
			/*String Header = ("\nRegra dos 3 minutos\n");
			String Content =
					"Data e hora inicial: " + settings.getString("time","") +
							"Data e hora final: " + dateFormat.format(new Date()) + "\n" +
							"Ultima localização salva - Latitude: " +
							settings.getString("latitude", "") + " Longitude: " + settings.getString("longitude", "") + "\n";

			Content = Content + "Localização atual - Latitude: " + loc.getLatitude() +
					" Longitude: " + loc.getLongitude() + "\n";

			Content = Content + "Variação de tempo: " + calculateTime(settings.getString("time", ""), dateFormat.format(new Date())) + " minutos\n";
			InsertDataOnFile(Header + Content);*/
			return;
		}

		// Verifica se a distancia que foi alterada é maior ou igual a 100 metros para
		// enviar os dados ao servidor
		Location lastLocation = new Location("");

		lastLocation.setLatitude(Double.parseDouble(settings.getString("latitude","")));
		lastLocation.setLongitude(Double.parseDouble(settings.getString("longitude","")));

		if (loc.distanceTo(lastLocation) >= DISTANCE_TO) {
			if (settings.contains("activeUser")) {

				editor = settings.edit();
				editor.putString("latitude", String.valueOf(loc.getLatitude()));
				editor.putString("longitude", String.valueOf(loc.getLongitude()));
				dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
				editor.putString("time", String.valueOf(dateFormat.format(new Date())));
				editor.apply();

                sendDataToServer(getApplicationContext());
                setLocation(loc);
				return;
			}
		} /*else {
			// Caso a distancia tenha sido menor que a 100 metros, ele apenas salva no log
			// a distancia que enviou ao servidor.
			// Bloco comentado para não ser usado em produção - 27/11/15
			String Header = ("\nRegra dos 100 metros\n");
			String Content = "Data: " + dateFormat.format(new Date()) + "\n" +
							"Ultima localização salva - Latitude: " +
							settings.getString("latitude", "") + " Longitude: " + settings.getString("longitude", "") + "\n";

			Content = Content + "Localização atual - Latitude: " + loc.getLatitude() +
					" Longitude: " + loc.getLongitude() + "\n";
			Content = Content + "Distancia: "+ String.valueOf(loc.distanceTo(lastLocation) + " metros\n");
			InsertDataOnFile(Header + Content);
			return;
		}*/
    }

	@Override
	public void onProviderDisabled(String provider) {
		//TODO
	}

	@Override
	public void onProviderEnabled(String provider) {
		//TODO
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub


	}

	public static double getLatitude()
	{
		if (customAddress != null)
			return customAddress.latitude;
		return latitude;
	}

	public static double getLongitude()
	{
		if (customAddress != null)
			return customAddress.longitude;
		return longitude;
	}

	@Override
	public void onCreate() {
		//TODO: Verifica a disponibilidade do serviço de localização e busca o melhor provedor

		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(true);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);

		try {
			locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
			locationManager.getBestProvider(criteria, true);
			List<String> matchingProviders = locationManager.getAllProviders();

			for (final String provider : matchingProviders) {
				Location location = locationManager.getLastKnownLocation(provider);
				if (location != null) {
					float accuracy = location.getAccuracy();
					long time = location.getTime();

					if ((time > minTime && accuracy < bestAccuracy)) {
						bestResult = location;
						bestAccuracy = accuracy;
						bestTime = time;
					} else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
						bestResult = location;
						bestTime = time;
					}
					bestProvider = provider;
				}
			}

			if (bestResult != null) {
				setLocation(bestResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		locationManager.removeUpdates(this);
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Reavalia os critérios de funcionamento do provedor e altera o provedor de localização
		// quando necessário.
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

		try {
			locationManager.requestLocationUpdates(bestProvider,
					MIN_TIME_BETWEEN_UPDATE,
					MIN_DISTANCE_CHANGE_UPDATE, this);
		} catch (Exception e) {
			e.printStackTrace();
			return super.onStartCommand(intent, flags, startId);
		}

		if (locationManager.isProviderEnabled(bestProvider))
			setLocation(locationManager.getLastKnownLocation(bestProvider));

		// Se a localização recebida for nula, saio da rotina esperando nova localização.
		if (location != null) {
            Location lastLocation = new Location(bestProvider);

            SharedPreferences settings;
            SharedPreferences.Editor editor;
            settings = getApplicationContext().getSharedPreferences("Zolkin-Location", Context.MODE_PRIVATE);

            // Verifica se no arquivo de preferences tem a ultima localização salva e seta a localização atual
			// para efetuar a regra dos 100 metros
			if (!settings.getString("latitude", "").equals("") && !settings.getString("longitude", "").equals("")) {
				lastLocation.setLatitude(Double.parseDouble(settings.getString("latitude", "")));
				lastLocation.setLongitude(Double.parseDouble(settings.getString("longitude", "")));
            } else {
				// Salva a primeira localização quando o app for instalado pela primeira vez
				if (settings.contains("activeUser")) {
                    editor = settings.edit();
                    editor.putString("latitude", String.valueOf(location.getLatitude()));
                    editor.putString("longitude", String.valueOf(location.getLongitude()));
					editor.putString("time", String.valueOf(dateFormat.format(new Date())));
                    editor.apply();

                    sendDataToServer(getApplicationContext());
					return super.onStartCommand(intent, flags, startId);
				}
            }

            // Verifica na se o tempo de envio para a atualização é maior que 3 minutos
			// caso não seja, não envia ao servidor e salva em arquivo de log
			// Bloco comentado para não ser usado em produção - 27/11/15
			if(calculateTime(settings.getString("time",""), dateFormat.format(new Date())) < 3) {
				/*String Header = ("\nRegra dos 3 minutos\n");
				String Content =
						"Data e hora inicial: " + settings.getString("time","") + "\n" +
						"Data e hora final: " + dateFormat.format(new Date()) + "\n" +
						"Ultima localização salva - Latitude: " +
								settings.getString("latitude", "") + " Longitude: " + settings.getString("longitude", "") + "\n";

				Content = Content + "Localização atual - Latitude: " + location.getLatitude() +
						" Longitude: " + location.getLongitude() + "\n";
				Content = Content + "Variação de tempo: " + calculateTime(settings.getString("time", ""), dateFormat.format(new Date())) + " minutos\n";
				InsertDataOnFile(Header + Content);*/
				return super.onStartCommand(intent, flags, startId);
			}

			// Verifica se a distancia de envio é maior ou igual a 100 metros para enviar ao servidor
			// passando pelo tempo.
			if (location.distanceTo(lastLocation) >= DISTANCE_TO) {
                if (settings.contains("activeUser")) {

                    editor = settings.edit();
                    editor.putString("latitude", String.valueOf(location.getLatitude()));
                    editor.putString("longitude", String.valueOf(location.getLongitude()));
					dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
					editor.putString("time", String.valueOf(dateFormat.format(new Date())));
                    editor.apply();

                    sendDataToServer(getApplicationContext());
					return super.onStartCommand(intent, flags, startId);
                }
            } /*else {
				// Caso nao tenha aceito a regra dos 100 metros
				// Salva a distância, não enviada ao servidor em um arq no cache. (Apelas para consulta)
				String Header = ("\nRegra dos 100 metros\n");
                String Content = "Data: " + dateFormat.format(new Date()) + "\n" +
                        "Ultima localização salva - Latitude: " +
                                settings.getString("latitude", "") + " Longitude: " + settings.getString("longitude", "") + "\n";

                Content = Content + "Localização atual - Latitude: " + location.getLatitude() +
                                " Longitude: " + location.getLongitude() + "\n";
				Content = Content + "Distancia: "+ String.valueOf(location.distanceTo(lastLocation) + " metros\n");
                InsertDataOnFile(Header + Content);
				return super.onStartCommand(intent, flags, startId);
            }*/
        }
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

    private void InsertDataOnFile(String data) {
        File file = new File(getBaseContext().getExternalFilesDir(null), "logFileZolkin.txt");
        try {
            FileWriter fw = new FileWriter(file, true);
            fw.append(data);
            fw.close();
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

	private long calculateTime(String startDate, String endDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		long diff = 0;
		Date dateStart, dateEnd;
		try {
			dateStart = dateFormat.parse(startDate);
			dateEnd = dateFormat.parse(endDate);

			diff = dateEnd.getTime() - dateStart.getTime();


		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		return (diff / (60 * 1000) % 60);
	}
}
