package com.imd.zolkin.activity;

//Tela de extrato

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.imd.zolkin.adapter.ListExtratoAVencerAdapter;
import com.imd.zolkin.adapter.ListExtratoKINsAdapter;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

import java.text.DecimalFormat;

import br.com.zolkin.R;

public class ExtratoActivity extends BaseZolkinMenuActivity
{
	static String extractMessage = "";
	FrameLayout btMenu = null;
	ImageView imgvBtTipoExtrato = null;
	TextView tvSaldoExtrato = null;
	TextView tvEconomiaTotal = null;
	LinearLayout llHeaderZolkins = null;
	LinearLayout llHeaderAVencer = null;
	ListView lvExtrato = null;



	ListExtratoKINsAdapter kinsAdapter;
	ListExtratoAVencerAdapter aVencerAdapter;

	enum TipoExtrato
	{
		TipoExtratoKINs,
		TipoExtratoAVencer
	};

	public static String extrato;
	public static String economia;
	TipoExtrato tipoExtrato;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_extrato);


		btMenu = (FrameLayout) findViewById(R.id.btMenu);
		imgvBtTipoExtrato = (ImageView) findViewById(R.id.imgvBtTipoExtrato);
		tvSaldoExtrato = (TextView) findViewById(R.id.tvSaldoExtrato);
		tvEconomiaTotal = (TextView) findViewById(R.id.tvEconomiaTotal);
		llHeaderZolkins = (LinearLayout) findViewById(R.id.llHeaderZolkins);
		llHeaderAVencer = (LinearLayout) findViewById(R.id.llHeaderAVencer);
		lvExtrato = (ListView) findViewById(R.id.lvExtrato);

		tipoExtrato = TipoExtrato.TipoExtratoKINs;

		//AdWords Campain Tracker
		//Purchase
		AdWordsConversionReporter.reportWithConversionId(
				this.getApplicationContext(),
				"932901513",
				"957-CIv-yWIQieXrvAM",
				"0.00",
				true);

		update();
		//implementa a troca de tipo de extrato
		imgvBtTipoExtrato.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tipoExtrato == TipoExtrato.TipoExtratoKINs) {
					tipoExtrato = TipoExtrato.TipoExtratoAVencer;
					imgvBtTipoExtrato.setImageResource(R.drawable.segmented_avencer);
				} else {
					tipoExtrato = TipoExtrato.TipoExtratoKINs;
					imgvBtTipoExtrato.setImageResource(R.drawable.segmented_zolkins);
				}

				//atualiza os dados
				update();
			}
		});



		/*
		DecimalFormat df = new DecimalFormat("0.00");
		extrato = df.format(ZLUser.getLoggedUser().kinBalance);
		economia = df.format(ZLUser.getLoggedUser().accumulatedEconomy);
		*/

		tvSaldoExtrato.setText(extrato);
		tvEconomiaTotal.setText(economia);

		tvSaldoExtrato.postInvalidate();
		tvEconomiaTotal.postInvalidate();

	}

	void update() {
		if (tipoExtrato == TipoExtrato.TipoExtratoKINs) {
			llHeaderZolkins.setVisibility(View.VISIBLE);
			llHeaderAVencer.setVisibility(View.GONE);
			//se já tinha carregado antes
			if (kinsAdapter != null) {
				//usa os dados que já tinha
				lvExtrato.setAdapter(kinsAdapter);
			} else {
				//senão zera a lista e carrega do serviço
				lvExtrato.setAdapter(null);
				ZLServices.getInstance().getExtract(true, this, new ZLServiceOperationCompleted<Boolean>()
				{
					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response)
					{
						if (response.errorMessage != null)
						{
							Toast.makeText(ExtratoActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							//mostra o extrato recebido do serviço
							kinsAdapter = new ListExtratoKINsAdapter(ExtratoActivity.this, ZLUser.getLoggedUser().extract);
							lvExtrato.setAdapter(kinsAdapter);
							DecimalFormat df = new DecimalFormat("0.00");
							extrato = df.format(ZLUser.getLoggedUser().kinBalance);
							economia = df.format(ZLUser.getLoggedUser().accumulatedEconomy);
							tvSaldoExtrato.setText(extrato);
							tvEconomiaTotal.setText(economia);

							tvSaldoExtrato.postInvalidate();
							tvEconomiaTotal.postInvalidate();

						}
					}
				});
			}
		} else {
			llHeaderZolkins.setVisibility(View.GONE);
			llHeaderAVencer.setVisibility(View.VISIBLE);
			//se já tinha carregado antes
			if (aVencerAdapter != null) {
				//usa os dados que já tinha
				lvExtrato.setAdapter(aVencerAdapter);
			} else {
				//senão zera a lista e carrega do serviço
				lvExtrato.setAdapter(null);
				ZLServices.getInstance().getOvercomeExtract(true, this, new ZLServiceOperationCompleted<Boolean>()
				{

					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response)
					{
						if (response.errorMessage != null) {
							Toast.makeText(ExtratoActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							//mostra o extrato recebido do serviço
							aVencerAdapter = new ListExtratoAVencerAdapter(ExtratoActivity.this, ZLUser.getLoggedUser().overcomeExtract);
							lvExtrato.setAdapter(aVencerAdapter);
							DecimalFormat df = new DecimalFormat("0.00");
							extrato = df.format(ZLUser.getLoggedUser().kinBalance);
							economia = df.format(ZLUser.getLoggedUser().accumulatedEconomy);
							tvSaldoExtrato.setText(extrato);
							tvEconomiaTotal.setText(economia);

							tvSaldoExtrato.postInvalidate();
							tvEconomiaTotal.postInvalidate();
						}
					}
				});
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (extractMessage.equals("")){
			return;
		} else {
			displayPopup(extractMessage);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();
	}

	private void displayPopup(String displayMessage) {
		AlertDialog.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog);
		else
			builder = new AlertDialog.Builder(this);

		builder.setTitle("Zolkin");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setMessage(displayMessage);
		builder.setPositiveButton("Visualizar extrato",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						extractMessage = "";
					}
				});

		AlertDialog alerta = builder.create();
		alerta.show();
	}
}
