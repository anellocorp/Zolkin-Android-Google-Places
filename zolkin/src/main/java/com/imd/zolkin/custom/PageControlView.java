package com.imd.zolkin.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import br.com.zolkin.R;


public class PageControlView extends View
{
	public int dotRadiusSp = 6;
	public int dotColorSelected = Color.argb(255, 0, 0, 0), dotColorUnselected = Color.argb(255, 180, 180, 180);
	public int numDots = 3;
	private int selectedDot = 0;
	
	public PageControlView(Context context)
	{
		super(context);
	}
	
	public PageControlView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.PageControlView,
		        0, 0);

		   try 
		   {
			   dotRadiusSp = a.getInteger(R.styleable.PageControlView_dotRadiusSp, 6);
			   dotColorSelected = a.getInteger(R.styleable.PageControlView_dotColorSelected, Color.argb(255, 0, 0, 0));
			   dotColorUnselected = a.getInteger(R.styleable.PageControlView_dotColorUnselected, Color.argb(255, 180, 180, 180));
			   numDots = a.getInteger(R.styleable.PageControlView_numDots, 3);
			   selectedDot = a.getInteger(R.styleable.PageControlView_selectedDot, 0);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}

//	public PageControlView(Context context, AttributeSet attrs, int defStyleAttr)
//	{
//		super(context, attrs, defStyleAttr);
//	}
	
	public void setNumDots(int numDots)
	{
		this.numDots = numDots;
		invalidate();
		requestLayout();
	}
	
	public void setSeletedDot(int selectedDot)
	{
		this.selectedDot = selectedDot;
		invalidate();
		requestLayout();
	}
	
	public int getNumDots()
	{
		return numDots;
	}
	
	public int getSeletedDot()
	{
		return selectedDot;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		
		Rect cb = canvas.getClipBounds();
//		int w = canvas.getWidth();
//		int h = canvas.getHeight();
		int w = cb.right;
		int h = cb.bottom;
		
		Paint bgPaint = new Paint();
		bgPaint.setColor(Color.TRANSPARENT);
		bgPaint.setStyle(Paint.Style.FILL);
		
		canvas.drawRect(new Rect(0,0,w,h), bgPaint);
		
		Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dotPaint.setStyle(Paint.Style.FILL);
		
		
		
		int dotsWidth = dotRadiusSp * numDots * 3 - 2 * numDots;
		
		int dotsStartX = (w - dotsWidth) / 2;
		
		for (int i = 0; i < numDots; i++)
		{
			if (i == selectedDot)
			{
				dotPaint.setColor(dotColorSelected);
			}
			else
			{
				dotPaint.setColor(dotColorUnselected);
			}
			canvas.drawCircle(dotsStartX + i * 3 * dotRadiusSp, h / 2, dotRadiusSp, dotPaint);
		}
	}
}
