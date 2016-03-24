package com.imd.zolkin.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.imd.zolkin.activity.BaseZolkinActivity;
import com.imd.zolkin.util.Constants;

import org.json.JSONObject;

import java.io.Serializable;

//uma cidade, conforme retornada pelo serviço getCity do Zolkin

public class ZLCity implements Serializable
{
	/**
	 * NOTA: variáveis estaticas podem ser zeradas quando o app volta do background
	 * por isso, sempre salvamos em persisten storage
	 */
	private static final long serialVersionUID = -369482501356181245L;
	
	
	public int id;
	public String name;
	
	private static ZLCity currentCity = null;
	
	public static ZLCity getCurrentCity(Context c)
	{
		if (currentCity == null)
		{
			SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
			if (prefs.contains("CityId"))
			{
				currentCity = new ZLCity(null);
				currentCity.id = prefs.getInt("CityId", 0);
				currentCity.name = prefs.getString("CityName", "SP");
			}
		}
		return currentCity;
	}
	
	public static void setCurrentCity(ZLCity city, Context c)
	{
		currentCity = city;
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("CityId", city.id);
		editor.putString("CityName", city.name);
		editor.apply();
	}
	
	public ZLCity(JSONObject jo)
	{
		try
		{
			id = jo.getInt("id");
		}
		catch (Exception e)
		{
			id = -1;
		}
		try
		{
			name = jo.getString("name");
		}
		catch (Exception e)
		{
			name = "";
		}
	}
}
