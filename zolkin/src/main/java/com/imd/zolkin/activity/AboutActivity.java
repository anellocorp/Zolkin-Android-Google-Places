package com.imd.zolkin.activity;

//Tela de Termos de Serviço

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.zolkin.BuildConfig;
import br.com.zolkin.R;

public class AboutActivity extends BaseZolkinActivity
{
	ImageView btVoltar = null;
	com.innovattic.font.FontTextView tvSiteZolkin = null;
	com.innovattic.font.FontTextView tvEmailZolkin = null;
    com.innovattic.font.FontTextView tvVersionZolkin = null;
	com.innovattic.font.FontTextView tvDateUpdate = null;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		tvSiteZolkin = (com.innovattic.font.FontTextView) findViewById(R.id.tvSiteZolkin);
		tvEmailZolkin = (com.innovattic.font.FontTextView) findViewById(R.id.tvEmailZolkin);
        tvVersionZolkin = (com.innovattic.font.FontTextView) findViewById(R.id.tvVersionZolkin);
		tvDateUpdate = (com.innovattic.font.FontTextView) findViewById(R.id.tvDateUpdate);

        try {

            String versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			Date buildDate = BuildConfig.buildTime;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

			String version = "Versão: " + versionName;
			String buildVersion = "atualizado em " + dateFormat.format(buildDate);

			tvVersionZolkin.setText(version);
			tvDateUpdate.setText(buildVersion);
        }
        catch (Exception ex){
			tvVersionZolkin.setText("-");
        }


		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		tvSiteZolkin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("http://www.zolkin.com.br"));
				startActivity(i);
			}
		});

		tvEmailZolkin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "contato@zolkin.com.br", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Fale Conosco");
				startActivity(Intent.createChooser(emailIntent, "Enviar Email"));
			}
		});
	}

}
