package com.imd.zolkin.model;

import org.json.JSONObject;

import java.io.Serializable;

//um item do extrato

public class ZLExtractEntry implements Serializable
{
	public String date, covenant, accumulatedEconomy;
	public double deposit, spent, value;
	public double saldo;
	
	public ZLExtractEntry(JSONObject jo)
	{
		try
		{
			date = jo.getString("date");
			if (date.equals("null"))
				date = "";
		}
		catch (Exception e)
		{
			date = "";
		}
		try
		{
			covenant = jo.getString("covenant");
		}
		catch (Exception e)
		{
			covenant = "";
		}
		try
		{
			value = jo.getDouble("value");
		}
		catch (Exception e)
		{
			value = 0;
		}
		try
		{
			deposit = jo.getDouble("deposit");
		}
		catch (Exception e)
		{
			deposit = 0;
		}
		try
		{
			spent = jo.getDouble("spent");
		}
		catch (Exception e)
		{
			spent = 0;
		}
	}
}
