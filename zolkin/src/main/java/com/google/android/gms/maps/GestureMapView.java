package com.google.android.gms.maps;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.gms.maps.model.LatLng;

public class GestureMapView extends MapView
{
	private GestureDetectorCompat mDetector;
	
	public interface GestureMapViewTap
	{
		void tapDetected(LatLng location);
	}
	
	private GestureMapViewTap callback;
	
	public void setTapCallback(GestureMapViewTap callback)
	{
		this.callback = callback;
		if (callback != null)
		{
			mDetector = new GestureDetectorCompat(getContext(), new OnGestureListener()
			{
				
				@Override
				public boolean onSingleTapUp(MotionEvent e)
				{
					if (GestureMapView.this.callback != null)
					{
						Projection proj = GestureMapView.this.getMap().getProjection();
						LatLng latLng = proj.fromScreenLocation(new Point((int) e.getX(), (int) e.getY()));
						GestureMapView.this.callback.tapDetected(latLng);
					}
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e)
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean onDown(MotionEvent e)
				{
					// TODO Auto-generated method stub
					return false;
				}
			});
		}
	}
	
	
	public GestureMapView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	public GestureMapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public GestureMapView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public GestureMapView(Context context, GoogleMapOptions options)
	{
		super(context, options);
		// TODO Auto-generated constructor stub
	}
	
	//never gets called anyway
//	@Override
//	public boolean onTouchEvent(MotionEvent event) 
//	{
//		if (mDetector != null)
//			mDetector.onTouchEvent(event);
//		
//		return super.onTouchEvent(event);
//	};
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (mDetector != null)
			mDetector.onTouchEvent(ev);

		return super.dispatchTouchEvent(ev);
	}

}
