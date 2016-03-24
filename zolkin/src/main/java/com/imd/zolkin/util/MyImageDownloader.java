package com.imd.zolkin.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.imd.zolkin.services.Http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyImageDownloader
{
	public boolean useExternalStorage = false;
	private Activity c;
	
	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;
	}
	
	public interface GetImageResult
	{
		void imageReceived(String url, String filename, Bitmap bm);
	};
	
	public class ImageDownloaderRequest
	{
		public String url, filename; 
		public GetImageResult gir;
		public int retryCount;
		
		
		public ImageDownloaderRequest(String url, String filename, GetImageResult gir)
		{
			this.url = url;
			this.filename = filename;
			this.gir = gir;
			retryCount = 0;
		}
	}
	
	private class MyRunnable implements Runnable
	{
		File fDir = null;
		public MyRunnable()
		{
			fDir = new File(Util.IMAGE_CACHE_DIR);
	    	fDir.mkdirs();

		}
		@Override
		public void run()
		{
			while (true)
			{
				ImageDownloaderRequest req = null;
				
				synchronized (requests)
				{
					if (requests.size() > 0)
					{
						req = requests.get(0);
						requests.remove(0);
						if (requests.size() > 25)
						{
							while (requests.size() > 15)
							{
								requests.remove(requests.size()-1);
							}
						}
					}
				}
				
				if (req != null)
				{
					processRequest(req);
				}
				else
				{
					try
					{
						Thread.sleep(200);
					} 
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private class MyRunnableFast implements Runnable
	{
		File fDir = null;
		ImageDownloaderRequest req;
		public MyRunnableFast(ImageDownloaderRequest req)
		{
			fDir = new File(Util.IMAGE_CACHE_DIR);
	    	fDir.mkdirs();
	    	this.req = req;
		}
		@Override
		public void run()
		{
			processRequest(req);
		}
		
	}
	
	void processRequest(ImageDownloaderRequest req)
	{
		File imgFile = null;
		if (req.filename != null)
		{
			String path;
			if (useExternalStorage)
			{
				//path = String.format("%s%s", Environment.getExternalStorageDirectory().toString(), Defines.PATH_IMAGENS_CACHE);
				path = c.getExternalFilesDir(null).getAbsolutePath();
			}
			else
			{
				path = c.getFilesDir().getAbsolutePath();
			}
	        String fileName = req.filename;
	
	        imgFile = new File(path,fileName);
	
	
	        if (imgFile.exists())
	        {
	        	Log.d("SenacImageCache", "Image exists: " + imgFile.getName() + ".   User: " + req.filename);
	            final Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	            if (bitmap != null)
		            {
		            final ImageDownloaderRequest reqF = req;
		            c.runOnUiThread(new Runnable()
		            {
		                public void run()
		                {
		                	reqF.gir.imageReceived(reqF.url, reqF.filename, bitmap);
		                }
		            });
		            return;
	            }
	            imgFile.delete();
	            Log.d("SenacImageCache", "Image corrupted: " + imgFile.getName() + ".   User: " + req.filename);
	        } 
		}
        //else 
        {
        	Bitmap bitmap;
        	Log.d("SenacImageCache", "Download image: " + req.url+ ".   User: " + req.filename);
			try
			{
				bitmap = Http.doGetBitmap(req.url);
				//bitmap = downloadBitmap(req.ps.imagem);
				//bitmap = DownloadImageAsync.getRoundedCornerBitmap(bitmap);
				if (bitmap == null)
				{
					req.retryCount++;
					Log.d("SenacImageCache", "Image did not download. ImageUrl: " + req.url + ".   User: " + req.filename);
					if (req.retryCount < 3)
					{
						synchronized (requests)
						{
							requests.add(0, req);
						}
					}
					return;
				}
				
				if (req.filename != null)
				{
					//write to cache
					FileOutputStream fOut = new FileOutputStream(imgFile);
	
					bitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
				    fOut.flush();
				    fOut.close();
				}
				
			    final Bitmap bitmapF = bitmap;
			    final ImageDownloaderRequest reqF = req;
	            c.runOnUiThread(new Runnable()
	            {
	                public void run()
	                {
	                	reqF.gir.imageReceived(reqF.url, reqF.filename, bitmapF);
	                }
	            });
			} 
			catch (Exception e)
			{
				bitmap = null;
				
				Log.d("String url, String filename", "Download image exception. ImageUrl: " + req.url);
				//Log.d("String url, String filename", "Download image exception. Image: " + imgFile.getName() + ".   Exception: " + e.toString() + ".   User: " + req.ps.nome);
			}
        	
        	
        }
	}
	
	List<Thread> threads;
	Thread t1 = null, t2 = null;
	
	private static MyImageDownloader sharedInstance = null;
	
	public static MyImageDownloader getInstance(Activity c)
	{
		if (sharedInstance == null)
			sharedInstance = new MyImageDownloader();
		
		sharedInstance.c = c;
		
		return sharedInstance;
	}
	
	private List<ImageDownloaderRequest> requests = null;
	
	private MyImageDownloader()
	{
		useExternalStorage = isExternalStorageWritable();
		
		requests = new ArrayList<ImageDownloaderRequest>();
		
		threads = new ArrayList<Thread>();
		
		for (int i = 0; i < 1; i++)
		{
			Thread t = new Thread(new MyRunnable());
			t.start();
			threads.add(t);
		}
	}
	
	
	
	public void getImage(String url, String filename, GetImageResult gir)
	{
		synchronized (requests)
		{
			requests.add(0, new ImageDownloaderRequest(url, filename, gir));
		}
	}
	
	public void getImageNow(String url, String filename, GetImageResult gir)
	{
		(new Thread(new MyRunnableFast(new ImageDownloaderRequest(url, filename, gir)))).start();
	}
	
	public static Bitmap downloadBitmap(String url) //throws IOException 
	{
		try
		{
        HttpUriRequest request = new HttpGet(url.toString());
        HttpClient httpClient = new DefaultHttpClient();
        HttpParams p = httpClient.getParams();
        HttpConnectionParams.setSoTimeout(p, 5000);
        HttpConnectionParams.setConnectionTimeout(p, 10000);
        HttpResponse response = httpClient.execute(request);
 
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
 
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                    bytes.length);
            if (bitmap == null)
            {
            	throw new Exception("Download corrupted");
            }
            return bitmap;
        } 
        else 
        {
            throw new IOException("Download failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
		}
		catch (Exception e)
		{
			Log.d("CLImageCache", "Download failed: " + e.toString());
			return null;
		}
    }
}
