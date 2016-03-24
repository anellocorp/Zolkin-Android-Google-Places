package com.imd.zolkin.adapter;

//callouts (InfoWindow) customizados para o mapa que tem na tela ListActivity

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.imd.zolkin.model.ZLStore;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import br.com.zolkin.R;

public class ListMapCalloutAdapter implements InfoWindowAdapter 
{
	WeakReference<Context> c;
	HashMap<Marker, ZLStore> markersMap;
	
	public ListMapCalloutAdapter(Context context, HashMap<Marker, ZLStore> markersMap)
	{
		c = new WeakReference<Context>(context);
		this.markersMap = markersMap;
	}

	@Override
	public View getInfoContents(Marker m) 
	{
		
		
		if (markersMap.containsKey(m))
		{
			final ZLStore r = markersMap.get(m);
			
			LayoutInflater li = LayoutInflater.from(c.get());
			View v = li.inflate(R.layout.callout_map_info, null, false);
			
			
			TextView tvTitle = (TextView) v.findViewById(R.id.tvCalloutName);
			TextView tvAddr = (TextView) v.findViewById(R.id.tvCalloutAddr);
			
			tvTitle.setText(r.name);
			tvAddr.setText(r.address.street + ", " + r.address.number);
			
			return v;
		}
		else
		{
			return null;
		}
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
