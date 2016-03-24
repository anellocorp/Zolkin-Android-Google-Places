package com.imd.zolkin.model;

//um endereço retornado pelo serviço de GeoCode do Google

import com.imd.zolkin.services.ZLLocationService;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleGeocodeAddress
{
	public String formattedAddress;
	public double latitude, longitude;
	JSONObject addrJson;

	
	public GoogleGeocodeAddress(JSONObject jo)
	{
		latitude = ZLLocationService.getLatitude();
		longitude = ZLLocationService.getLongitude();

		try
		{
			formattedAddress = jo.getString("formatted_address");
		}
		catch (Exception e)
		{
			formattedAddress = "";
		}
		
		try
		{
			latitude = jo.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		}
		catch (Exception e)
		{
			latitude = ZLLocationService.getLatitude();
		}
		
		try
		{
			longitude = jo.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		}
		catch (Exception e)
		{
			longitude = ZLLocationService.getLongitude();
		}
	}
	
	public void addAddressComponentsToJsonObject(JSONObject jo)
	{
		try
		{
			JSONArray ja = addrJson.getJSONArray("address_components");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject component = ja.getJSONObject(i);
				jo.put(component.getJSONArray("types").getString(0), component.get("long_name"));
			}
		}
		catch (Exception e)
		{
			
		}
	}
}
