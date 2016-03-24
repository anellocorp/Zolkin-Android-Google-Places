package com.imd.zolkin.adapter;

//lista o extrato a vencer.
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

public class ListExtratoAVencerAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	List<ZLExtractEntry> extract;

	public ListExtratoAVencerAdapter(Context context, List<ZLExtractEntry> extract)
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
			v = li.inflate(R.layout.item_extract_avencer, vg, false);
		}
		
		ZLExtractEntry entry = extract.get(pos);
		
		
		
		TextView tvData = (TextView) v.findViewById(R.id.tvData);
		TextView tvKins = (TextView) v.findViewById(R.id.tvKins);
		TextView tvSaldo = (TextView) v.findViewById(R.id.tvSaldo);
		
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
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		tvKins.setText(df.format(entry.value));
		if (entry.saldo < 0 && entry.saldo > -0.01)
		{
			entry.saldo = 0;
		}
			
		String saldoString = df.format(entry.saldo);
		tvSaldo.setText(saldoString);
		
		return v;
	}
}
