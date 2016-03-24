package com.imd.zolkin.model;

//endereço de um estabelecimento

import org.json.JSONObject;

import java.io.Serializable;

public class ZLAddress implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5610026758876191941L;
	
	public String street, additionalDetails, neighborhood, city, state, zipCode, phone, mobile, mobileOperator, number;
	
	
	public ZLAddress(JSONObject jo)
	{
		try
		{
			number = jo.getString("number");
		}
		catch (Exception e)
		{
			number = "";
		}
		try
		{
			street = jo.getString("street");
		}
		catch (Exception e)
		{
			street = "";
		}
		try
		{
			additionalDetails = jo.getString("additionalDetails");
		}
		catch (Exception e)
		{
			additionalDetails = "";
		}
		try
		{
			neighborhood = jo.getString("neighborhood");
		}
		catch (Exception e)
		{
			neighborhood = "";
		}
		try
		{
			city = jo.getString("city");
		}
		catch (Exception e)
		{
			city = "";
		}
		try
		{
			state = jo.getString("state");
		}
		catch (Exception e)
		{
			state = "";
		}
		try
		{
			zipCode = jo.getString("zipCode");
		}
		catch (Exception e)
		{
			zipCode = "";
		}
		try
		{
			phone = jo.getString("phone");
		}
		catch (Exception e)
		{
			phone = "";
		}
		try
		{
			mobile = jo.getString("mobile");
		}
		catch (Exception e)
		{
			mobile = "";
		}
		try
		{
			mobileOperator = jo.getString("mobileOperator");
		}
		catch (Exception e)
		{
			mobileOperator = "";
		}
	}
}
