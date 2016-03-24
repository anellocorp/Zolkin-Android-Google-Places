package com.imd.zolkin.model;

//uma forma de pagamento

import org.json.JSONObject;

public class ZLPaymentOption
{
	public String name, iconUrl, poType;
	
	public ZLPaymentOption(JSONObject jo)
	{
		try
		{
			name = jo.getString("name");
		}
		catch (Exception e)
		{
			name = "";
		}
		try
		{
			iconUrl = jo.getString("icon");
		}
		catch (Exception e)
		{
			iconUrl = "";
		}
		try
		{
			poType = jo.getString("type");
		}
		catch (Exception e)
		{
			poType = "";
		}
	}
}
