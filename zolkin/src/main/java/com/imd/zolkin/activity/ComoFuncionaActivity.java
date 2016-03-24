package com.imd.zolkin.activity;

//Tela de tutorial

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.imd.zolkin.adapter.IntroPagerAdapter;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import br.com.zolkin.R;

public class ComoFuncionaActivity extends BaseZolkinActivity
{
	
	android.support.v4.view.ViewPager vpImages = null;
	TextView tvBtPularIntro = null;
	com.imd.zolkin.custom.PageControlView pcImages = null;
	
	IntroPagerAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_como_funciona);
		
		vpImages = (android.support.v4.view.ViewPager) findViewById(R.id.vpImages);
		tvBtPularIntro = (TextView) findViewById(R.id.tvBtPularIntro);
		pcImages = (com.imd.zolkin.custom.PageControlView) findViewById(R.id.pcImages);
		
		
		//configura os pontinhos indicando quantas telas tem e em qual estamos
		pcImages.numDots = 4;
		pcImages.dotRadiusSp = (int) Util.convertDpToPixel(3, this);
		pcImages.dotColorSelected = getResources().getColor(R.color.zl_light_blue);
		pcImages.dotColorUnselected = 0xA0BBBBBB;
		pcImages.invalidate();
		
		adapter = new IntroPagerAdapter(this);
		
		vpImages.setAdapter(adapter);
		vpImages.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int pos)
			{
				//ao mudar de tela
				//atualiza os pontinhos
				pcImages.setSeletedDot(pos);
				
				//se estiver na ultima tela mostra o Comece Já, senão mostra o Pular Introdução
				if (pos < 3)
				{
					tvBtPularIntro.setText("Pular introdução");
				}
				else
				{
					tvBtPularIntro.setText("Comece já!");
					
					try
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(ComoFuncionaActivity.this, Constants.MIXPANEL_TOKEN);

						mixPanel.track("Introducao", null);
						
					}
					catch (Exception e)
					{

					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{

			}
		});
		
		//ao clicar no botão
		tvBtPularIntro.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//sair da tela de tutorial
				finish();
			}
		});
	}
}
