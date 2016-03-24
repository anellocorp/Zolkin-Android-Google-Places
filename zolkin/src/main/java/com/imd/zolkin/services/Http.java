package com.imd.zolkin.services;

//classe para facilitar o acesso http

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Http
{

	private static final String TAG = "Http";
	public static final int TIMEOUT = 25000; // 25s
	public static final String CHARSET = "UTF-8";

	public static byte[] toBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				bos.write(buf, 0, len);
			}

			byte[] bytes = bos.toByteArray();
			return bytes;
		}
		finally
		{
			bos.close();
		}
	}

	public static final String toString(InputStream in, String charset) throws IOException
	{
		byte[] bytes = toBytes(in);
		if (charset == null)
		{
			charset = CHARSET;
		}
		String string = new String(bytes, charset);
		return string;
	}

	public static final String toString(InputStream in) throws IOException
	{
		return toString(in, null);
	}

	public static String getQueryString(Map<String, Object> params) throws IOException
	{
		if (params == null || params.size() == 0)
		{
			return null;
		}
		String urlParams = null;
		for (String key : params.keySet())
		{
			Object obj = params.get(key);
			if (obj != null && obj.getClass() == String.class)
			{
				String value = obj.toString();
				urlParams = urlParams == null ? "" : urlParams + "&";
				urlParams += key + "=" + new URLCodec().encode(value, Http.CHARSET);
			}
			else if (obj != null && obj.getClass() == ArrayList.class)
			{
				@SuppressWarnings("unchecked")
				ArrayList<String> values = (ArrayList<String>) obj;
				for (String v : values)
				{
					urlParams = urlParams == null ? "" : urlParams + "&";
					urlParams += key + "%5B%5D=" + new URLCodec().encode(v, Http.CHARSET);
				}
			}
		}
		return urlParams;

	}

	public static final InputStream doGet(String url, Map<String, Object> params, Map<String, String> headers) throws IOException
	{
		if (params != null)
		{
			String queryString = getQueryString(params);
			if (queryString != null)
			{
				url += "?" + queryString;
			}
		}

		HttpGet httpGet = new HttpGet(url);

		if (headers != null)
		{
			for (String key : headers.keySet())
			{
				httpGet.setHeader(key, headers.get(key));
			}
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT);

		HttpClient client = new DefaultHttpClient(my_httpParams);
		HttpResponse response = client.execute(httpGet);

		return response.getEntity().getContent();
	}

	public static final InputStream doPost(String url, Map<String, Object> params, Map<String, String> headers) throws IOException
	{

		HttpPost httpPost = new HttpPost(url);

		if (params != null)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
			for (Map.Entry<String, Object> entry : params.entrySet())
			{
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
		}

		if (headers != null)
		{
			for (String key : headers.keySet())
			{
				httpPost.setHeader(key, headers.get(key));
			}
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT);

		HttpClient client = new DefaultHttpClient(my_httpParams);
		HttpResponse response = client.execute(httpPost);

		return response.getEntity().getContent();
	}

	
	public static final InputStream doPostBody(String url, String postBody, Map<String, Object> headers) throws IOException
	{

		HttpPost httpPost = new HttpPost(url);

		
		httpPost.setEntity(new StringEntity(postBody, "UTF-8"));

		if (headers != null)
		{
			for (String key : headers.keySet())
			{
				httpPost.setHeader(key, headers.get(key).toString());
			}
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT);

		HttpClient client = new DefaultHttpClient(my_httpParams);
		HttpResponse response = client.execute(httpPost);

		
		
		return response.getEntity().getContent();
	}
	
	
	public static final int doPostBodyGetResponseCode(String url, String postBody, Map<String, Object> headers) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(postBody, "UTF-8"));

		if (headers != null) {
			for (String key : headers.keySet()) {
				httpPost.setHeader(key, headers.get(key).toString());
			}
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT);

		HttpClient client = new DefaultHttpClient(my_httpParams);
		HttpResponse response = client.execute(httpPost);
		
		return response.getStatusLine().getStatusCode();
	}

	public static final InputStream doPostDirectUrl(String url, Map<String, String> headers) throws IOException
	{

		HttpPost httpPost = new HttpPost(url);

		if (headers != null)
		{
			for (String key : headers.keySet())
			{
				httpPost.setHeader(key, headers.get(key));
			}
		}

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT);

		HttpClient client = new DefaultHttpClient(my_httpParams);
		HttpResponse response = client.execute(httpPost);

		return response.getEntity().getContent();
	}

	public static final String doGetString(String url, Map<String, Object> params, Map<String, String> headers, String charset) throws IOException
	{
		return toString(doGet(url, params, headers), charset);
	}

	public static final String doGetString(String url, String charset) throws IOException
	{
		return doGetString(url, null, null, charset);
	}

	public static final String doGetString(String url) throws IOException
	{
		return doGetString(url, null, null, CHARSET);
	}

	public static final String doPostString(String url, Map<String, Object> params, Map<String, String> headers, String charset) throws IOException
	{
		return toString(doPost(url, params, headers), charset);
	}
	
	public static final String doPostStringBody(String url, String body, Map<String, Object> headers, String charset) throws IOException
	{
		return toString(doPostBody(url, body, headers), charset);
	}

	public static final String doPostStringNoParams(String url, Map<String, String> headers, String charset) throws IOException
	{
		return toString(doPostDirectUrl(url, headers), charset);
	}

	public static final String doPostString(String url, String charset) throws IOException
	{
		return doPostString(url, null, null, charset);
	}

	public static final String doPostString(String url) throws IOException
	{
		return doPostString(url, null, null, CHARSET);
	}

	public static final byte[] doGetImage(String url, Map<String, Object> params, Map<String, String> headers) throws IOException
	{
		return toBytes(doGet(url, params, headers));
	}

	public static final byte[] doGetImage(String url) throws IOException
	{
		return doGetImage(url, null, null);
	}

	public static final Bitmap doGetBitmap(String url, Map<String, Object> params, Map<String, String> headers) throws IOException
	{
		try
		{
			byte[] result = toBytes(doGet(url, params, headers));
			Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
			return bitmap;
		}
		catch (Exception e)
		{
			Log.d("ChatLocal", "Error " + e.toString() + " decoding bitmap " + url);
			return null;
		}
	}

	public static final Bitmap doGetBitmap(String url) throws IOException
	{
		return doGetBitmap(url, null, null);
	}

}
