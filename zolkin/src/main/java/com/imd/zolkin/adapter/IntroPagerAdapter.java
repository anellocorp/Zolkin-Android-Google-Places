package com.imd.zolkin.adapter;

//usado para mostrar as imagens do tutorial
//ViewPager padrão

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import br.com.zolkin.R;

public class IntroPagerAdapter extends PagerAdapter
{
	// Views that can be reused.
	private final List<View> mDiscardedViews = new ArrayList<View>();
	// Views that are already in use.
	private final SparseArray<View> mBindedViews = new SparseArray<View>();

	public final int[] viewLayoutIDs;
	private final LayoutInflater mInflator;
	
	View[] viewCache;
	
	WeakReference<Context> c;

	public IntroPagerAdapter(Context context)
	{
		//estes são os IDs dos xmls com as 4 telas do tutorial
		this.viewLayoutIDs = new int[] {R.layout.tutorial_0, R.layout.tutorial_1, R.layout.tutorial_2, R.layout.tutorial_3};
		
		
		mInflator = LayoutInflater.from(context);
		c = new WeakReference<Context>(context);
		
		viewCache = new View[this.viewLayoutIDs.length];
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		View child = null;
		if (viewCache[position] != null)
		{
			child = viewCache[position];
		}
		else
		{
			child = mInflator.inflate(viewLayoutIDs[position], container, false);
		}

		container.addView(child, 0);
		child.setTag("" + position);
		return "" + position;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		View view = viewCache[position];
		if (view != null)
		{
			container.removeView(view);
		}
	}

	@Override
	public int getCount()
	{
		return viewLayoutIDs.length;
	}

	@Override
	public boolean isViewFromObject(View v, Object obj)
	{
		return obj.equals(v.getTag());
	}
}
