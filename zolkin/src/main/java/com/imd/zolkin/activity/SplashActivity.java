package com.imd.zolkin.activity;

//Tela de Splash inicial do app
//esta é sempre a primeira tela mostrada ao abrir o app
//Verifica se tem um usuário logado. Se tem, vai direto para a Home. Senão, vai para a tela de login

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.imd.zolkin.custom.ZolkinApplication;
import com.imd.zolkin.model.ZLPendingAction;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Calendar;
import java.util.Date;

import br.com.zolkin.R;

public class SplashActivity extends BaseZolkinActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		final Intent intent = getIntent();
		final Uri myURI=intent.getData();

		/* Inicio do Tracker para o Analytics e marcação de evento lauch app */
		ZolkinApplication zolkin = (ZolkinApplication) getApplication();
		Tracker mTracker = zolkin.getDefaultTracker();
		mTracker.setScreenName("SplashScreen");
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());


		if (myURI != null)
		{
			String scheme = myURI.getScheme(); //zolkin
			String uri = myURI.toString().replace("zolkin://", ""); //zolkin://cazzo/cazzo2

			String[] parts = uri.split("/");
			if (parts.length > 1)
			{
				BaseZolkinMenuActivity.pendingAction = new ZLPendingAction(parts[0], parts[1]);
			}
		}

		Intent zlIntent = getIntent();
		if (zlIntent.hasExtra("PendingActionKey"))
		{
			String key = zlIntent.getStringExtra("PendingActionKey");
			String param = zlIntent.getStringExtra("PendingActionParam");
			if (key.equals("extrato")) {
				String uri = myURI.toString().replace("zolkin://", ""); //zolkin://cazzo/cazzo2
				String[] parts = uri.split("/");
				String msg = parts[2];
				ExtratoActivity.extractMessage = msg;
			}

			BaseZolkinMenuActivity.pendingAction = new ZLPendingAction(key, param);
			NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		}

		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);


		Intent i = new Intent("ALARME_LOCATION");

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);

		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);


		/*if (PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_NO_CREATE) == null)
		{
			PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 5, pi);
		}*/

		MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, Constants.MIXPANEL_TOKEN);

		//debug: avisa se esta rodando em ambiente de homologação
		if (!ZLServices.BASE_URL.equals(ZLServices.BASE_URL_PROD))
		{
			Toast.makeText(this, "Atenção: app rodando em ambiente de homologação", Toast.LENGTH_LONG).show();
		}

		//inicializa o backend, chamando o método init do serviço, que é necessário para autenticar todas as chamadas sucessivas.
		AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params_)
			{
				try
				{
					ZLServices s = ZLServices.getInstance();

				}
				catch (Exception e)
				{
				}
				//
				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				//depois de inicializar, pega a cidade.
				//isto pode não ser mais necessário no core novo
				ZLServices.getInstance().retrieveCity(ZLLocationService.getLocation(), true, SplashActivity.this, new ZLServiceOperationCompleted<Boolean>()
				{

					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response)
					{
						// now home if saved user, else go to login screen
						ZLUser u = ZLUser.getLoggedUser();
						if (u.isAuthenticated())
						{
							ZLServices.getInstance().login(u.email, u.password, u.facebookToken, true, SplashActivity.this, new ZLServiceOperationCompleted<Boolean>()
							{

								@Override
								public void operationCompleted(ZLServiceResponse<Boolean> response)
								{
									if (response.errorMessage != null)
									{
										// go to login screen
										Toast.makeText(SplashActivity.this, "Erro no login automatico. Por favor, verifique seus dados e tente novamente", Toast.LENGTH_LONG).show();
										ZLUser.logout();
										startActivity(new Intent(SplashActivity.this, LoginActivity.class));
										finish();
									}
									else
									{
										startActivity(new Intent(SplashActivity.this, HomeActivity.class));
										finish();
									}
								}
							});
						}
						else
						{
							startActivity(new Intent(SplashActivity.this, LoginActivity.class));
							finish();
						}
					}
				});
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)  {
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} else {
			asyncTask.execute((Void[]) null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
}
