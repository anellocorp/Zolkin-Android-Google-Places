package com.imd.zolkin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imd.zolkin.model.ZLSubCategory;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class FilterAdapter extends BaseAdapter
{
	//mostra as subcategorias
	//usado na tela de filtro
	
	WeakReference<Context> c;
	List<ZLSubCategory> subCats;

	public FilterAdapter(Context context, List<ZLSubCategory> subCats)
	{
		c = new WeakReference<Context>(context);
		this.subCats = subCats;
	}
	
	@Override
	public int getCount()
	{
		return subCats.size();
	}

	@Override
	public ZLSubCategory getItem(int pos)
	{
		return subCats.get(pos);
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
			v = li.inflate(R.layout.item_filter_subcat, vg, false);
		}
		
		final ZLSubCategory subCat = subCats.get(pos);
		
		TextView tvSubCatName = (TextView) v.findViewById(R.id.tvSubCatName);
		final ImageView imgvCheck = (ImageView) v.findViewById(R.id.imgvCheck);
		
		tvSubCatName.setText(subCat.name);
		
		if (subCat.selected)
		{
			imgvCheck.setImageResource(R.drawable.check_h);
		}
		else
		{
			imgvCheck.setImageResource(R.drawable.check_n);
		}

		//ao clicar em uma categoria, altera seu estado de seleção
		v.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				subCat.selected = !subCat.selected;
				
				if (subCat.selected)
				{
					imgvCheck.setImageResource(R.drawable.check_h);
				}
				else
				{
					imgvCheck.setImageResource(R.drawable.check_n);
				}
			}
		});
		
		return v;
	}
}
