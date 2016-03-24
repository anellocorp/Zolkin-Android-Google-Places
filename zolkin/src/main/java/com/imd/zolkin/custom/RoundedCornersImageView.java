package com.imd.zolkin.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.imd.zolkin.util.Util;

import br.com.zolkin.R;

public class RoundedCornersImageView extends ImageView
{
	public float radius = 12.0f;
	private Path clipPath;
	private RectF rect;

	public RoundedCornersImageView(Context context)
	{
		super(context);
	}

	public RoundedCornersImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.PageControlView,
		        0, 0);

		   try 
		   {
			   int cornerRadiusDp = a.getInteger(R.styleable.RoundedCornersImageView_cornerRadiusDp, 12);
			   radius = Util.convertDpToPixel(cornerRadiusDp, context);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		clipPath = new Path();
		rect = new RectF(0, 0, this.getWidth(), this.getHeight());
	}
	
	public RoundedCornersImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (clipPath == null)
		{
			clipPath = new Path();
			rect = new RectF(0, 0, this.getWidth(), this.getHeight());
		}
		clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
		canvas.clipPath(clipPath);
		super.onDraw(canvas);
	}
}
