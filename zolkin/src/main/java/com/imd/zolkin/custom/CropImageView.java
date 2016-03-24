package com.imd.zolkin.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.SurfaceView;

import com.imd.zolkin.util.Util;

import java.lang.ref.WeakReference;

@SuppressLint("ClickableViewAccessibility")
public class CropImageView extends SurfaceView
{
	WeakReference<Context> c;
	
	private Bitmap bitmap;
	
	int viewWidth, viewHeight;
	int imageX,imageY;
	double imageScale;
	double cropRatio_W_over_H;
	Rect sourceImageRect;
	Rect destRect;
	Paint paint;
	int marginLR = 0;
	Rect unmaskedAreaRect;
	
	int pointerID1, pointerID2;
	
	int lastX1, lastX2, lastY1, lastY2;
	double startScale;
	
	double lastDist = 0, startDist = 0;
	
	enum TouchMode
	{
		Zoom,
		Pan,
		None
	};
	
	TouchMode touchMode = TouchMode.None;

	private Bitmap mask;

	private Rect fullScreenRect;

	private int maskW;

	private int maskH;
	
	public Bitmap getCroppedImage()
	{
		if (bitmap == null)
			return null;
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap fullView = Bitmap.createBitmap(viewWidth, viewHeight, conf); // this creates a MUTABLE bitmap
		Canvas canvasFV = new Canvas(fullView);
		destRect.left = imageX;
		destRect.top = imageY;
		destRect.right = (int) (imageScale * bitmap.getWidth() + imageX);
		destRect.bottom = (int) (imageScale * bitmap.getHeight() + imageY);
		canvasFV.drawBitmap(bitmap, sourceImageRect, destRect, paint);		
		
		Bitmap croppedBitmap = Bitmap.createBitmap(maskW, maskH, conf); // this creates a MUTABLE bitmap
		Canvas canvas = new Canvas(croppedBitmap);
		
		Paint ppp = new Paint();
		ppp.setARGB(255, 255, 0, 0);
		//canvas.drawRect(new Rect(0,0,maskW, maskH), ppp);
		canvas.drawBitmap(fullView, unmaskedAreaRect, new Rect(0,0,maskW, maskH), paint);
		
		//fullView.recycle();
		
		return croppedBitmap;
	}
	

	public CropImageView(Context context)
	{
		super(context);
		
		c = new WeakReference<Context>(context);
		
		setWillNotDraw(false);
	}
	public CropImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		c = new WeakReference<Context>(context);
		
		setWillNotDraw(false);
	}
	public CropImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		c = new WeakReference<Context>(context);
		
		setWillNotDraw(false);
	}

	public void setBitmap(Bitmap bm, double cropRatio_W_over_H_)
	{
		bitmap = bm;
		this.cropRatio_W_over_H = cropRatio_W_over_H_;
		
		//calculate scale
		viewWidth = getWidth();
		viewHeight = getHeight();
		
		double scaleH = viewWidth / (double) bitmap.getWidth();
		double scaleV = viewHeight / (double) bitmap.getHeight();
		
		imageScale = Math.min(scaleH, scaleV);
		
		imageX = 0;
		imageY = 0;
		
		sourceImageRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		destRect = new Rect(imageX, imageY, viewWidth, viewHeight);
		
		if (imageScale * bitmap.getWidth() < viewWidth)
		{
			imageX = (int) ((viewWidth - imageScale * bitmap.getWidth()) / 2);
		}
		if (imageScale * bitmap.getHeight() < viewHeight)
		{
			imageY = (int) ((viewHeight - imageScale * bitmap.getHeight()) / 2);
		}
		
		paint = new Paint();
		
		marginLR = (int) Util.convertDpToPixel(10, c.get());
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		mask = Bitmap.createBitmap(viewWidth, viewHeight, conf); // this creates a MUTABLE bitmap
		Canvas canvas = new Canvas(mask);
		
		//draw the dark rect
		maskW = viewWidth - marginLR * 2;
		maskH = (int) (maskW / cropRatio_W_over_H);
		Rect rl = new Rect(0, 0, marginLR, viewHeight);
		Rect rr = new Rect(viewWidth - marginLR, 0 ,viewWidth , viewHeight);
		Rect rt = new Rect(marginLR, 0, viewWidth - marginLR, (viewHeight - maskH - (viewHeight - maskH) / 2));
		Rect rb = new Rect(marginLR, (viewHeight - (viewHeight - maskH) / 2), viewWidth - marginLR, viewHeight);
		fullScreenRect = new Rect(0,0,viewWidth, viewHeight);
		unmaskedAreaRect = new Rect(marginLR,(viewHeight - maskH - (viewHeight - maskH) / 2),viewWidth - marginLR, (viewHeight - (viewHeight - maskH) / 2));
		
		Paint pr = new Paint();
		pr.setARGB(190, 0, 0, 0);
		
		canvas.drawRect(rl, pr);
		canvas.drawRect(rr, pr);
		canvas.drawRect(rt, pr);
		canvas.drawRect(rb, pr);
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		if (bitmap == null)
		{
			super.onDraw(canvas);
			return; //nothing to draw
		}
		
		destRect.left = imageX;
		destRect.top = imageY;
		destRect.right = (int) (imageScale * bitmap.getWidth() + imageX);
		destRect.bottom = (int) (imageScale * bitmap.getHeight() + imageY);
		canvas.drawBitmap(bitmap, sourceImageRect, destRect, paint);
		canvas.drawBitmap(mask, fullScreenRect, fullScreenRect, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
			{
				Log.d("CropImageView", "ACTION_DOWN");
				touchMode = TouchMode.Pan;
				PointerCoords pc = new PointerCoords();
				event.getPointerCoords(0, pc);
				
				lastX1 = (int) pc.x;
				lastY1 = (int) pc.y;
			}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				if (event.getPointerCount() == 2)
				{
					Log.d("CropImageView", "ACTION_POINTER_DOWN");
					touchMode = TouchMode.Zoom;
					startScale = imageScale;
					
					PointerCoords pc = new PointerCoords();
					event.getPointerCoords(1, pc);
					lastX2 = (int) pc.x;
					lastY2 = (int) pc.y;
					
					startDist = Math.sqrt((lastX2 - lastX1)*(lastX2 - lastX1) + (lastY2 - lastY1)*(lastY2 - lastY1));
				}
			}
				break;
			case MotionEvent.ACTION_UP:
				Log.d("CropImageView", "ACTION_UP");
				touchMode = TouchMode.None;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				Log.d("CropImageView", "ACTION_POINTER_UP");
				touchMode = TouchMode.None;
				break;
			case MotionEvent.ACTION_MOVE:
			{
				Log.d("CropImageView", "ACTION_MOVE");
				PointerCoords pc1 = new PointerCoords();
				event.getPointerCoords(0, pc1);
				
				if (touchMode == TouchMode.Pan)
				{
					int x1 = (int) pc1.x;
					int y1 = (int) pc1.y;
					
					int dx = x1 - lastX1;
					int dy = y1 - lastY1;
					
					imageX += dx;
					imageY += dy;
					
					lastX1 = x1;
					lastY1 = y1;
					
					invalidate();
				}
				else if (touchMode == TouchMode.Zoom)
				{
					PointerCoords pc2 = new PointerCoords();
					event.getPointerCoords(1, pc2);
					
					int x1 = (int) pc1.x;
					int y1 = (int) pc1.y;
					
					int x2 = (int) pc2.x;
					int y2 = (int) pc2.y;
					
					lastDist = Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
					
					double ratio = lastDist / startDist;
					
					double newScale = startScale * ratio;
					
					int oldW = (int) (bitmap.getWidth() * imageScale);
					int newW = (int) (bitmap.getWidth() * newScale);
					
					int oldH = (int) (bitmap.getHeight() * imageScale);
					int newH = (int) (bitmap.getHeight() * newScale);
					
					int deltaW = newW - oldW;
					int deltaH = newH - oldH;
					
					//need to transform based on the center of the two touch points;
					int touchCenterX = (x1 + x2) / 2;
					int touchCenterY = (y1 + y2) / 2;
					
					double propX = (double) (touchCenterX - imageX) / (bitmap.getWidth() * imageScale);
					double propY = (double) (touchCenterY - imageY) / (bitmap.getHeight() * imageScale);
					
					imageX -= deltaW * propX;
					imageY -= deltaH * propY;
					
//					imageX -= deltaW / 2;
//					imageY -= deltaH / 2;
					
					imageScale = newScale;
					
					lastX1 = x1;
					lastY1 = y1;
					lastX2 = x2;
					lastY2 = y2;
					
					//adjust center of zoom
					invalidate();
				}
				
			}
				break;
			default:
				break;
		}
		
		//return super.onTouchEvent(event);
		return true;
	}
}
