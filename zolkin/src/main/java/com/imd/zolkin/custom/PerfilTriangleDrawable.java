package com.imd.zolkin.custom;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class PerfilTriangleDrawable extends Drawable
{

	@Override
	public void draw(Canvas c)
	{
		Path p = new Path();
		
		p.setFillType(FillType.EVEN_ODD);
		
		p.moveTo(0, c.getHeight());
		p.lineTo(c.getWidth(), c.getHeight());
		p.lineTo(c.getWidth(), 0);
		p.lineTo(0, c.getHeight());
		
		p.close();
		
		Paint paint = new Paint();
		paint.setColor(Color.argb(255, 0x26, 0xd2, 0xdb));
		paint.setStyle(Style.FILL_AND_STROKE);
		
		c.drawPath(p, paint);
		
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
