package com.imd.zolkin.adapter;

//Lista de estabelecimentos
//usado na tela de lista

import android.content.Context;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.services.ZLLocationService;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class ListStoresAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	List<ZLStore> stores;

	public ListStoresAdapter(Context context, List<ZLStore> stores)
	{
		c = new WeakReference<Context>(context);
		this.stores = stores;
	}
	
	@Override
	public int getCount()
	{
		return stores.size();
	}

	@Override
	public ZLStore getItem(int pos)
	{
		return stores.get(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup vg)
	{
		if (v == null)
		{
			LayoutInflater li = LayoutInflater.from(c.get());
			v = li.inflate(R.layout.item_list_store, vg, false);
		}
		
		ZLStore store = stores.get(pos);
		
		TextView tvName = (TextView) v.findViewById(R.id.tvName);
		TextView tvNeighborhood = (TextView) v.findViewById(R.id.tvNeighborhood);
		ImageView imgvPin = (ImageView) v.findViewById(R.id.imgvPin);
		TextView tvStreetDistance = (TextView) v.findViewById(R.id.tvStreetDistance);
		ImageView imgvBadge = (ImageView) v.findViewById(R.id.imgvBadge);
		TextView tvPercentageDiscount = (TextView) v.findViewById(R.id.tvPercentageDiscount);
		TextView tvUntilText = (TextView) v.findViewById(R.id.tvUntilText);
		TextView tvUntilTime = (TextView) v.findViewById(R.id.tvUntilTime);

		//preenche os dados
		tvName.setText(store.name);
		tvNeighborhood.setText(store.address.neighborhood);

		if (store.distance == 0.0) {
			double distance = createDistance(store.latitude, store.longitude) / 1000;
			tvStreetDistance.setText(String.format("%.1f km - %s,  %s",
					distance,
					store.address.street,
					store.address.number));
		} else {
			tvStreetDistance.setText(String.format("%.1f km - %s,  %s",
					(store.distance / 1000),
					store.address.street,
					store.address.number));
		}

		tvPercentageDiscount.setText("" + store.percentageEconomy);
		tvUntilText.setText("até as");
		tvUntilTime.setText(store.currentEconomyStopTime);

		//se o estabelecimento esta fechado
		if (store.businessHours.isClosed)
		{
			//adapta o layout para o fechado
			imgvPin.setImageResource(R.drawable.pin_zolkin_cinza);
			imgvBadge.setImageResource(R.drawable.badge_cinza);
			tvName.setTextColor(0xFF686868);
			tvNeighborhood.setTextColor(0xFF686868);
			tvStreetDistance.setTextColor(0xFF686868);
			
			
			if (store.newEconomy != null)
			{
				tvPercentageDiscount.setText("" + store.newEconomy.discountPercentage);
				tvUntilText.setText("a partir das");
				tvUntilTime.setText(store.newEconomy.startTime);
			}
			else
			{
				tvPercentageDiscount.setText("--");
				tvUntilText.setText("");
				tvUntilTime.setText("");
			}
			
			tvStreetDistance.setText("Fechado no momento.");
		}
		else
		{
			//mantém o layout de aberto
			imgvPin.setImageResource(R.drawable.pin_zolkin);
			imgvBadge.setImageResource(R.drawable.badge_gnd);
			tvName.setTextColor(c.get().getResources().getColor(R.color.zl_purple));
			tvNeighborhood.setTextColor(0xFF000000);
			tvStreetDistance.setTextColor(0xFF000000);
		}
		
		return v;
	}

	protected double createDistance(double latStore, double lonStore) {

		ZLLocationService.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		double currentLat = ZLLocationService.getLatitude();
		double currentLon = ZLLocationService.getLongitude();

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