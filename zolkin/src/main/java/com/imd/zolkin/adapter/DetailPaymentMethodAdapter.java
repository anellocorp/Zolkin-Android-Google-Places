package com.imd.zolkin.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.imd.zolkin.model.ZLPaymentOption;
import com.imd.zolkin.util.MyImageDownloader;
import com.imd.zolkin.util.MyImageDownloader.GetImageResult;
import com.imd.zolkin.util.Util;

import java.lang.ref.WeakReference;
import java.util.List;

public class DetailPaymentMethodAdapter extends BaseAdapter
{
	//cria views para os métodos de pagamento de um estabelecimento
	
	WeakReference<Context> c;
	List<ZLPaymentOption> paymentOptions;
	
	public DetailPaymentMethodAdapter(Context c, List<ZLPaymentOption> options)
	{
		this.c = new WeakReference<Context>(c);
		paymentOptions = options;
	}

	@Override
	public int getCount()
	{
		return paymentOptions.size();
	}

	@Override
	public ZLPaymentOption getItem(int pos)
	{
		return paymentOptions.get(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup vg)
	{
		final ImageView imageView = new ImageView(c.get());
        imageView.setLayoutParams(new GridView.LayoutParams((int)Util.convertPixelsToDp(80, c.get()), (int)Util.convertPixelsToDp(80, c.get())));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(1, 1, 1, 1);

        
        ZLPaymentOption po = paymentOptions.get(pos);

        MyImageDownloader.getInstance((Activity) c.get()).getImage(po.iconUrl, "po" + po.name + ".png", new GetImageResult()
		{
			
			@Override
			public void imageReceived(String url, String filename, Bitmap bm)
			{
				if (bm != null)
				{
					imageView.setImageBitmap(bm);
				}
			}
		});
        
        return imageView;
	}

}
