package com.imd.zolkin.model;

//uma subCategoria

import org.json.JSONObject;

public class ZLSubCategory
{
	public int subCatId;
	public String iconUrl, name;
	public boolean selected;
	
	public ZLSubCategory(JSONObject jo)
	{
		selected = true;
		try
		{
			subCatId = jo.getInt("id");
		}
		catch (Exception e)
		{
			subCatId = 0;
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
			name = jo.getString("name");
		}
		catch (Exception e)
		{
			name = "";
		}
	}
}
