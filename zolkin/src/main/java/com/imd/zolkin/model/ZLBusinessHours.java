package com.imd.zolkin.model;

import org.json.JSONObject;

//um horário de funcionamento

public class ZLBusinessHours
{
	public String start, end;
	public boolean isClosed;
	
	public ZLBusinessHours(JSONObject jo)
	{
		isClosed = false;
		try
		{
			start = jo.getString("start");
		}
		catch (Exception e)
		{
			start = "";
			isClosed = true;
		}
		try
		{
			end = jo.getString("end");
		}
		catch (Exception e)
		{
			end = "";
			isClosed = true;
		}
	}
}
