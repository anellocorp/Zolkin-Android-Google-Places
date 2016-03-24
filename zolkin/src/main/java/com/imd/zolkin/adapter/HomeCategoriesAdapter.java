package com.imd.zolkin.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imd.zolkin.activity.FavoritosActivity;
import com.imd.zolkin.activity.ListaActivity;
import com.imd.zolkin.model.ZLCategory;
import com.imd.zolkin.model.ZLCity;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.MyImageDownloader;
import com.imd.zolkin.util.MyImageDownloader.GetImageResult;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.zolkin.R;

public class HomeCategoriesAdapter extends BaseAdapter
{
	//lista as cateogorias na home
	//4 itens por linha

	WeakReference<Context> c;
	List<ZLCategory> categories;
	Context appContext;
	private int base = 0;
	boolean have;

	public HomeCategoriesAdapter(Context context, List<ZLCategory> cats)
	{
		c = new WeakReference<Context>(context);
		if (ZLUser.getLoggedUser().isAuthenticated())
			cats.add(0, null);

		categories = cats;
		appContext = context;
	}

	@Override
	public int getCount()
	{
		return (int) ((categories.size() / 4.0) + 0.9);
	}

	@Override
	public Object getItem(int arg0)
	{
		return null;
	}

	@Override
	public long getItemId(int arg0)
	{
		return 0;
	}

	@Override
	public View getView(int pos, View v, ViewGroup vg) {
		if (v == null) {
			LayoutInflater li = LayoutInflater.from(c.get());
			v = li.inflate(R.layout.item_home_categories, vg, false);
		}


		//pega as 4 views
		View vHomeCat1 = (View) v.findViewById(R.id.vHomeCat1);
		View vHomeCat2 = (View) v.findViewById(R.id.vHomeCat2);
		View vHomeCat3 = (View) v.findViewById(R.id.vHomeCat3);
		View vHomeCat4 = (View) v.findViewById(R.id.vHomeCat4);

		// Analisa o JSON e insere a na home o item favorito, não como uma categoria
		// mas sim como um botão de acesso rapido.

		// TODO: 17/08/15
		/*
			Verifica se o usuário esta autenticado e cria o item favorito na tela
			principal
			Se o usuário estiver acessando o app em modo convidado, não cria o item
			favoritos na tela principal, assim como no menu.
		*/


		base = pos * 4;

		if (pos == 0 && ZLUser.getLoggedUser().isAuthenticated()) {
			createFavorite(vHomeCat1,asFavorities());
			vHomeCat1.setVisibility(View.VISIBLE);
			ZLCategory cat2 = categories.get(1);
			configureViewWithCategory(vHomeCat2, cat2);
			vHomeCat2.setVisibility(View.VISIBLE);
			ZLCategory cat3 = categories.get(2);
			configureViewWithCategory(vHomeCat3, cat3);
			vHomeCat3.setVisibility(View.VISIBLE);
			ZLCategory cat4 = categories.get(3);
			configureViewWithCategory(vHomeCat4, cat4);
			vHomeCat4.setVisibility(View.VISIBLE);

			return v;
		} else {
			// TODO: 12/08/15
		/*
			Continua a criação das categorias caso esteja ou não logado.
			Ponto de anlise de performasse.
			Criar função que verifique se houveram alterações nos assets do
			app, para não busca novamente na internet as imagens.
		*/

			ZLCategory cat1 = categories.get(base);
			configureViewWithCategory(vHomeCat1, cat1);

			//verifica se não tem categorias suficientes para preencher a linha toda, e se não tem esconde as views excedentes
			if (categories.size() > base + 1) {
				ZLCategory cat2 = categories.get(base + 1);
				configureViewWithCategory(vHomeCat2, cat2);
				vHomeCat2.setVisibility(View.VISIBLE);
			} else {
				vHomeCat2.setVisibility(View.INVISIBLE);
			}

			if (categories.size() > base + 2) {
				ZLCategory cat3 = categories.get(base + 2);
				configureViewWithCategory(vHomeCat3, cat3);
				vHomeCat3.setVisibility(View.VISIBLE);
			} else {
				vHomeCat3.setVisibility(View.INVISIBLE);
			}

			if (categories.size() > base + 3) {
				ZLCategory cat4 = categories.get(base + 3);
				configureViewWithCategory(vHomeCat4, cat4);
				vHomeCat4.setVisibility(View.VISIBLE);
			} else {
				vHomeCat4.setVisibility(View.INVISIBLE);
			}

			return v;
		}
	}

	//configura a view de uma singola categoria (uma coluna)
	void configureViewWithCategory(View v, final ZLCategory cat)
	{
		final ImageView imgvCatIcon = (ImageView) v.findViewById(R.id.imgvCatIcon);
		TextView tvCatName = (TextView) v.findViewById(R.id.tvCatName);
		imgvCatIcon.setImageBitmap(null);
		tvCatName.setText(cat.name);

		//para cache, usa o nome do arquivo do ícone de categoria
		//se o nome não muda, a imagem nunca é carregada de novo do serviço
		//e é sempre usada a que esta em cache
		int pos = 0;
		for (int i = cat.iconUrl.length() - 1; i >= 0; i--)
		{
			if (cat.iconUrl.charAt(i) == '/')
			{
				pos = i;
				break;
			}
		}
		String filename = cat.iconUrl.substring(pos + 1);
		//pega o ícone da categoria
		MyImageDownloader.getInstance((Activity) c.get()).getImage(cat.iconUrl, filename, new GetImageResult()
		{

			@Override
			public void imageReceived(String url, String filename, Bitmap bm)
			{
				if (bm != null)
				{
					imgvCatIcon.setImageBitmap(bm);
				}
			}
		});

		//ao clicar em uma categoria
		v.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ListaActivity.category = cat;

				try
				{

					MixpanelAPI mixPanel = MixpanelAPI.getInstance(c.get(), Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					props.put("Categoria", cat.name);
					props.put("IdCategoria", cat.catId);
					mixPanel.track("HomeCategoria", props);

				}
				catch (Exception e)
				{

				}

				//chama o serviço de lista de estabelecimentos passando a categoria selecionada
				ZLServices.getInstance().listCovenants("" + ZLLocationService.getLatitude(), "" + ZLLocationService.getLongitude(), false, 1000, 0, ZLCity.getCurrentCity(c.get()).id, false, "" + cat.catId, null, null,
						null, true, c.get(), new ZLServiceOperationCompleted<List<ZLStore>>() {

							@Override
							public void operationCompleted(ZLServiceResponse<List<ZLStore>> response) {
								if (response.errorMessage != null) {
									Toast.makeText(c.get(), response.errorMessage, Toast.LENGTH_LONG).show();
								} else {
									if (response.serviceResponse.size() > 0) {
										// show results
										//mostra a tela de lista com o resultado
										ListaActivity.category = cat;
										ListaActivity.list = response.serviceResponse;
										ListaActivity.needReload = false;
										c.get().startActivity(new Intent(c.get(), ListaActivity.class));
									} else {
										Toast.makeText(c.get(), "Nenhum estabelecimento encontrado para essa categoria.", Toast.LENGTH_LONG).show();
									}
								}

							}
						});

			}
		});
	}

	// Criação do item favorito a tela home
	void createFavorite(View view, boolean have) {
		final ImageView imgvCatIcon = (ImageView) view.findViewById(R.id.imgvCatIcon);
		Drawable d;
		InputStream is = null;
		TextView tvCatName = (TextView) view.findViewById(R.id.tvCatName);
		imgvCatIcon.setImageBitmap(null);
		tvCatName.setText(R.string.favorites);

		try {
			if (have)
				is = c.get().getAssets().open("favoritosOFF.png");
			else
				is = c.get().getAssets().open("favoritosON.png");

		} catch (IOException e) {
			e.printStackTrace();
		}

		d = Drawable.createFromStream(is, null);

		imgvCatIcon.setImageDrawable(d);
		imgvCatIcon.invalidate();

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(c.get(), "Listando favoritos", Toast.LENGTH_SHORT).show();
				//Log.d("Tocou no favoritos", "HomeCategoriesAdapter");
				c.get().startActivity(new Intent(c.get().getApplicationContext(), FavoritosActivity.class));
			}
		});
	}

	private synchronized boolean asFavorities() {
		AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				have = ZLServices.getInstance().asFavorities();
				return null;
			}
		};
		return have;
	}
}
