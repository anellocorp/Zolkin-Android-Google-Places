package com.imd.zolkin.activity;

//Tela de estabelecimentos favoritos

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.imd.zolkin.adapter.ListStoresAdapter;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

import java.util.List;

import br.com.zolkin.R;

public class FavoritosActivity extends BaseZolkinMenuActivity
{
	ListStoresAdapter adapter;
	
	TextView tvCatName = null;
	FrameLayout btMenu = null;
	ListView lvFavoritos = null;
	ScrollView scNaoEncontrada = null;

	Double currentLat;
	Double currentLon;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favoritos);

		tvCatName = (TextView) findViewById(R.id.tvCatName);
		btMenu = (FrameLayout) findViewById(R.id.btMenu);
		lvFavoritos = (ListView) findViewById(R.id.lvFavoritos);
		scNaoEncontrada = (ScrollView) findViewById(R.id.scNaoEncontrada);

	}
	
	@Override
	protected void onResume()
	{
		super.onResume();

		
		//carrega a listam de favoritos do serviço
		ZLServices.getInstance().listFavorites(true, this, new ZLServiceOperationCompleted<List<ZLStore>>()
		{
			@Override
			public void operationCompleted(ZLServiceResponse<List<ZLStore>> response)
			{
				if (response.errorMessage != null) {
                    Toast.makeText(FavoritosActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();

                } else {
					//se veio uma lista vazia, mostra a tela de que não tem favoritos
					if (response.serviceResponse.size() == 0)
					{
						scNaoEncontrada.setVisibility(View.VISIBLE);
						lvFavoritos.setVisibility(View.GONE);
					}
					else
					{
						scNaoEncontrada.setVisibility(View.GONE);
						lvFavoritos.setVisibility(View.VISIBLE);
					}
					
					//cria o adapter e mostra na lista
					adapter = new ListStoresAdapter(FavoritosActivity.this, response.serviceResponse);
					lvFavoritos.setAdapter(adapter);

					
					//se clicar em um favorito, mostra os detalhes do estabelecimento
					lvFavoritos.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
						{
							DetailActivity.storeID = adapter.getItem(pos).covenantId;
							// if (adapter.getItem(pos).distance == 0.0)
								DetailActivity.distance = (createDistance(
										adapter.getItem(pos).latitude,
										adapter.getItem(pos).longitude) / 1000);
							//else
								//DetailActivity.distance = (adapter.getItem(pos).distance) / 1000;

							DetailActivity.saveIDs(FavoritosActivity.this);
							startActivity(new Intent(FavoritosActivity.this, DetailActivity.class));
						}
					});
				}
			}
		});
	}

	protected double createDistance(double latStore, double lonStore) {

		ZLLocationService.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		currentLat = ZLLocationService.getLatitude();
		currentLon = ZLLocationService.getLongitude();

		final int R = 6371; // Raio da Terra em KM

		Double latDistance = Math.toRadians(currentLat - latStore);
		Double lonDistance = Math.toRadians(currentLon - lonStore);

		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(latStore)) * Math.cos(Math.toRadians(currentLat))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // conversão para metros

		distance = Math.pow(distance, 2);

		return Math.sqrt(distance);
	}
}
