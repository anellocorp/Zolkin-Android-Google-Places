package com.imd.zolkin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.imd.zolkin.model.GoogleGeocodeAddress;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

//lista os endereços retornados por um geocode do google.
//usado na tela de change location
public class ChangeLocationAddressAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	List<GoogleGeocodeAddress> addresses;

	public ChangeLocationAddressAdapter(Context context, List<GoogleGeocodeAddress> addresses)
	{
		c = new WeakReference<Context>(context);
		this.addresses = addresses;
	}
	
	@Override
	public int getCount()
	{
		return addresses.size();
	}

	@Override
	public GoogleGeocodeAddress getItem(int pos)
	{
		return addresses.get(pos);
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
			v = li.inflate(R.layout.item_change_location_address, vg, false);
		}
		
		GoogleGeocodeAddress a = addresses.get(pos);
		
		TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);

		tvAddress.setText(a.formattedAddress);
		
		return v;
	}
}
