package com.imd.zolkin.custom;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class CadastroCheckboxUncheckedDrawable extends Drawable
{

	@Override
	public void draw(Canvas c)
	{
		
		Paint paint = new Paint();
		paint.setColor(Color.argb(255, 0x59, 0x2d, 0x7f));
		paint.setStyle(Style.STROKE);
		
		c.drawArc(new RectF(1, 1, c.getWidth() - 2, c.getHeight() - 2), 0, 360, true, paint);
		
		//c.drawRect(new Rect(0, 0, c.getWidth(), c.getHeight()), paint);
	}

	@Override
	public int getOpacity()
	{
		// TODO Auto-generated method stub
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter arg0)
	{
		// TODO Auto-generated method stub

	}

}
