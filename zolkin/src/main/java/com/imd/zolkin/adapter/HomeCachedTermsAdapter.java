package com.imd.zolkin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.imd.zolkin.services.ZLListCache;
import com.imd.zolkin.services.ZLListCache.ZLTerm;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class HomeCachedTermsAdapter extends BaseAdapter
{
	//lista de termos recentes pesquisados.
	//usado na Home
	WeakReference<Context> c;
	List<ZLTerm> terms;

	public HomeCachedTermsAdapter(Context context)
	{
		c = new WeakReference<Context>(context);
		terms = ZLListCache.getInstance().getTerms();
	}

	@Override
	public int getCount()
	{
		return terms.size();
	}

	@Override
	public ZLTerm getItem(int pos)
	{
		return terms.get(pos);
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
			v = li.inflate(R.layout.item_home_terms, vg, false);
		}
		TextView tvCachedTerm = (TextView) v.findViewById(R.id.tvCachedTerm);
		
		tvCachedTerm.setText(getItem(pos).term);
		
		return v;
	}

}
