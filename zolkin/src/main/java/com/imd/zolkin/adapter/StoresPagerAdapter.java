package com.imd.zolkin.adapter;

//usado na tela DetailActivity
//serve para mostrar as fotos do estabelecimento

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.imd.zolkin.util.MyImageDownloader;
import com.imd.zolkin.util.MyImageDownloader.GetImageResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import br.com.zolkin.R;

public class StoresPagerAdapter extends PagerAdapter
{
	// Views that can be reused.
	private final List<View> mDiscardedViews = new ArrayList<View>();
	// Views that are already in use.
	private final SparseArray<View> mBindedViews = new SparseArray<View>();

	public final List<String> images;
	private final LayoutInflater mInflator;
	private final int mResourceId = R.layout.item_detail_picture;
	
	Bitmap[] bmCache;
	
	WeakReference<Context> c;

	public StoresPagerAdapter(Context context, List<String> images)
	{
		this.images = images;
		mInflator = LayoutInflater.from(context);
		c = new WeakReference<Context>(context);
		bmCache = new Bitmap[this.images.size()];
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		View child = mDiscardedViews.isEmpty() ? mInflator.inflate(mResourceId, container, false) : mDiscardedViews.remove(0);

		ImageView imgv = (ImageView) child;
		imgv.setImageBitmap(null);
		String url = images.get(position);
		initView(child, url, position);

		mBindedViews.append(position, child);
		container.addView(child, 0);
		return url;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		View view = mBindedViews.get(position);
		if (view != null)
		{
			mDiscardedViews.add(view);
			mBindedViews.remove(position);
			container.removeView(view);
		}
	}

	@Override
	public int getCount()
	{
		return images.size();
	}

	@Override
	public boolean isViewFromObject(View v, Object obj)
	{
		return v == mBindedViews.get(images.indexOf(obj));
	}

	/**
	 * Initiate the view here
	 */
	public void initView(View v, String url, final int position)
	{
		final ImageView imgv = (ImageView) v;
		if (bmCache[position] != null)
		{
			imgv.setImageBitmap(bmCache[position]);
		}
		else
		{
			MyImageDownloader.getInstance((Activity)c.get()).getImage(url, null, new GetImageResult()
			{
				@Override
				public void imageReceived(String url, String filename, Bitmap bm)
				{
					if (bm != null)
					{
						bmCache[position] = bm;
						imgv.setImageBitmap(bm);
					}
				}
			});
		}
	}
}
