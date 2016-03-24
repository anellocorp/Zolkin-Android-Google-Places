package com.imd.zolkin.custom;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class CircleWithBorderDrawable extends Drawable
{

	private Paint paint;
	private RectF rectF;
	private int color;
	private double sizePercentage;

	public enum Direction
	{
		LEFT, RIGHT, TOP, BOTTOM
	}

	public CircleWithBorderDrawable()
	{
		this(Color.BLUE, 1);
	}

	public CircleWithBorderDrawable(int color, double sizePercentage)
	{
		this.color = color;
		this.sizePercentage = sizePercentage;
		paint = new Paint();
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		rectF = new RectF();
	}

	public int getColor()
	{
		return color;
	}

	/**
	 * A 32bit color not a color resources.
	 * 
	 * @param color
	 */
	public void setColor(int color)
	{
		this.color = color;
		paint.setColor(color);
	}

	@Override
	public void draw(Canvas canvas)
	{
		canvas.save();

		Rect bounds = getBounds();
		double width = bounds.width();
		double newWidth = width * sizePercentage;
		double start = (width - newWidth) / 2;
		
		rectF.set(new Rect((int) start,(int) start,(int) (start + newWidth), (int)(start + newWidth)));

		canvas.drawArc(rectF, 0, 360, true, paint);

	}

	@Override
	public void setAlpha(int alpha)
	{
		// Has no effect
	}

	@Override
	public void setColorFilter(ColorFilter cf)
	{
		// Has no effect
	}

	@Override
	public int getOpacity()
	{
		// Not Implemented
		return 0;
	}

}
