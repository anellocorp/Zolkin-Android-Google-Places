package com.imd.zolkin.adapter;

//lista o extrato de KINs.
//usado na tela de extrato


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.imd.zolkin.model.ZLExtractEntry;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.List;

import br.com.zolkin.R;

public class ListExtratoKINsAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	List<ZLExtractEntry> extract;

	public ListExtratoKINsAdapter(Context context, List<ZLExtractEntry> extract)
	{
		c = new WeakReference<Context>(context);
		this.extract = extract;
	}
	
	@Override
	public int getCount()
	{
		return extract.size();
	}

	@Override
	public ZLExtractEntry getItem(int pos)
	{
		return extract.get(pos);
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
			v = li.inflate(R.layout.item_extract_kins, vg, false);
		}
		
		ZLExtractEntry entry = extract.get(pos);
		
		TextView tvData = (TextView) v.findViewById(R.id.tvData);
		TextView tvDesc = (TextView) v.findViewById(R.id.tvDesc);
		TextView tvUsados = (TextView) v.findViewById(R.id.tvUsados);
		TextView tvRecebidos = (TextView) v.findViewById(R.id.tvRecebidos);
		
		try
		{
			String year =  entry.date.substring(0, 4);
			String month = entry.date.substring(5, 7);
			String day = entry.date.substring(8, 10);
			tvData.setText(day + "/" + month);
		}
		catch (Exception e)
		{
			tvData.setText(entry.date);
		}
		
		
		tvDesc.setText(entry.covenant);
		DecimalFormat df = new DecimalFormat("0.00");
		tvUsados.setText(df.format(entry.spent));
		tvRecebidos.setText(df.format(entry.deposit));
		
		return v;
	}
}
