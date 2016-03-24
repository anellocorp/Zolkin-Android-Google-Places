package com.imd.zolkin.activity;

//Tela de Termos de Serviço

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.imd.zolkin.services.Http;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.util.Constants;

import br.com.zolkin.R;

public class TermosActivity extends BaseZolkinActivity
{
	ImageView btVoltar = null;
	TextView tvTermos = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_termos);
		
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		tvTermos = (TextView) findViewById(R.id.tvTermos);
		
		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		tvTermos.setText(getResources().getString(R.string.termos2));
		
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("termos"))
		{
			String t = prefs.getString("termos", null);
			tvTermos.setText(t);
		}
		
		update();
	}
	
	void update()
	{
		//final ProgressDialog progress = ProgressDialog.show(this, Constants.APP_NAME, "Loading...");


		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>()
		{
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_)
			{
				ZLServiceResponse<String> result = new ZLServiceResponse<String>();

				String response;
				try
				{

					response = Http.doGetString("http://s3-sa-east-1.amazonaws.com/zolkinv2institucional/termo_de_uso.txt", "UTF-8");
					
					SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("termos", response);
					editor.apply();
					
					result.serviceResponse = response;
				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result)
			{
				//progress.cancel();
				if (result.errorMessage == null)
				{
					tvTermos.setText(result.serviceResponse);
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
	}
}
