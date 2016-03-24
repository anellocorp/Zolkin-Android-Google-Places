package com.imd.zolkin.model;

import org.json.JSONObject;

//uma economia por hora

public class ZLDiscountForecast
{
public String startTime, stopTime, date, day;
public int discountPercentage;
	
	public ZLDiscountForecast(JSONObject jo)
	{
		try
		{
			startTime = jo.getString("startTime");
		}
		catch (Exception e)
		{
			startTime = "";
		}
		try
		{
			stopTime = jo.getString("stopTime");
		}
		catch (Exception e)
		{
			stopTime = "";
		}
		try
		{
			date = jo.getString("date");
		}
		catch (Exception e)
		{
			date = "";
		}
		try
		{
			day = jo.getString("day");
			if (day.equals("today"))
				day = "Hoje";
		}
		catch (Exception e)
		{
			day = "";
		}
		try
		{
			discountPercentage = jo.getInt("saving");
		}
		catch (Exception e)
		{
			discountPercentage = 0;
		}
	}
}
