package com.imd.zolkin.activity;

//Tela de filtro de subcategorias

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imd.zolkin.adapter.FilterAdapter;
import com.imd.zolkin.model.ZLSubCategory;
import com.imd.zolkin.util.Constants;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.zolkin.R;

public class FilterActivity extends BaseZolkinActivity
{
	ImageView btVoltar = null;
	ImageView btLocation = null;
	ListView lvSubCategories = null;
	TextView tvBtLimpar = null;
	TextView tvBtFiltrar = null;
	
	//recebe Activity chamante um mapa de subcategorias, onde a chave é o ID da subCategoria
	public static HashMap<Integer, ZLSubCategory> dSubCategories;
	
	List<ZLSubCategory> subCategories;
	
	FilterAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		btLocation = (ImageView) findViewById(R.id.btLocation);
		lvSubCategories = (ListView) findViewById(R.id.lvSubCategories);
		tvBtLimpar = (TextView) findViewById(R.id.tvBtLimpar);
		tvBtFiltrar = (TextView) findViewById(R.id.tvBtFiltrar);
		
		//pega as subCategorias do Map e adiciona em uma lista
		subCategories = new ArrayList<ZLSubCategory>();

		for (Integer intg : dSubCategories.keySet())
		{
			ZLSubCategory sc = dSubCategories.get(intg);
			subCategories.add(sc);
		}
		//cria o adapter e mostra no listview
		adapter = new FilterAdapter(this, subCategories);
		lvSubCategories.setAdapter(adapter);
		
		
		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				boolean atLeastOneSelected = false;
				for (ZLSubCategory sc : subCategories)
				{
					if (sc.selected)
					{
						atLeastOneSelected = true;
					}
				}
				
				if (!atLeastOneSelected)
				{
					for (ZLSubCategory sc : subCategories)
					{
						sc.selected = true;
					}
					dSubCategories = null;
					ListaActivity.subCatsFilter = null;
					finish();
				}
				
				finish();
			}
		});
		
		//ao clicar em limpar, seleciona todas as categorias, remove o filtro, e volta para a tela de lista
		tvBtLimpar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (ZLSubCategory sc : subCategories)
				{
					sc.selected = true;
				}
				dSubCategories = null;
				ListaActivity.subCatsFilter = null;
				finish();
			}
		});
		
		//ao clicar em filtrar
		tvBtFiltrar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					//loga no mixpanel as categorias que foram selecionadas e deselecionadas
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(FilterActivity.this, Constants.MIXPANEL_TOKEN);
					boolean atLeastOneSelected = false;
					for (int i = 0; i < adapter.getCount(); i++)
					{
						ZLSubCategory sc = (ZLSubCategory) adapter.getItem(i);
						JSONObject props2 = new JSONObject();
						props2.put("Nome", sc.name);
						props2.put("ID", sc.subCatId);
						if (sc.selected)
						{
							mixPanel.track("FiltroSubcategoriaIncluida", props2);
							atLeastOneSelected = true;
						}
						else
							mixPanel.track("FiltroSubcategoriaExcluida", props2);
					}
					//se todas estamas deselecionadas, remove o filtro
					if (!atLeastOneSelected)
					{
						dSubCategories = null;
						ListaActivity.subCatsFilter = null;
					}
				}
				catch (Exception e)
				{

				}
				//volta para a Lista
				finish();
			}
		});
	}
}
