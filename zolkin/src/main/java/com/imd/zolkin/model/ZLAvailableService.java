package com.imd.zolkin.model;

import org.json.JSONObject;

//um serviço disponível em um estabelecimento

public class ZLAvailableService
{
public String name, iconUrl, description;
	
	public ZLAvailableService(JSONObject jo)
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
			description = jo.getString("description");
		}
		catch (Exception e)
		{
			description = "";
		}
	}
}
