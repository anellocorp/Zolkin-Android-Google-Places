package com.imd.zolkin.model;

import org.json.JSONObject;

import java.io.Serializable;

//uma categoria

public class ZLCategory implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -597731542153034143L;
	public int catId;
	public String iconUrl, name;
	
	public ZLCategory(JSONObject jo)
	{
		try
		{
			catId = jo.getInt("id");
		}
		catch (Exception e)
		{
			catId = 0;
		}
		
		try
		{
			iconUrl = jo.getString("icon").replace("\\", "");
		}
		catch (Exception e)
		{
			iconUrl = "";
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
