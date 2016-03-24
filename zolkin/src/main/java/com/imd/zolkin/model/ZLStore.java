package com.imd.zolkin.model;

//um estabelecimento do Zolkin

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ZLStore
{

	public int covenantId;
	public String name;
	public double latitude, longitude, distance;
	public int percentageEconomy;
	public String currentEconomyStopTime;
	public boolean isFavorite;
	public String concatCategoryAndParent;
	public String categoryIconUrl;
	public ZLBusinessHours businessHours;
	public ZLAddress address;
	public ZLCategory category;
	public ZLSubCategory subCategory;
	public List<ZLSubCategory> subCategories;
	public ZLDiscountForecast newEconomy;
	
	//detail stuff
	public String phoneNumber, contactMail, officialWebsite, facebookProfile, twitterProfile, detail, openingTimes, logo;
	
	public List<String> imageList, mobileImageList;
	public List<ZLPaymentOption> paymentOptions;
	public List<ZLAvailableService> availableServices;
	public List<ZLDiscountForecast> discountForecast;
	
	//cria a partir do json
	public ZLStore(JSONObject jo)
	{
		subCategories = new ArrayList<ZLSubCategory>();
		try
		{
			covenantId = jo.getInt("id");
		}
		catch (Exception e)
		{
			covenantId = 0;
		}
		try
		{
			percentageEconomy = jo.getInt("currentPercentageEconomy");
		}
		catch (Exception e)
		{
			percentageEconomy = 0;
		}
		try
		{
			distance = jo.getDouble("distance");
		}
		catch (Exception e)
		{
			distance = 0;
		}
		try
		{
			latitude = jo.getDouble("latitude");
		}
		catch (Exception e)
		{
			latitude = 0;
		}
		try
		{
			longitude = jo.getDouble("longitude");
		}
		catch (Exception e)
		{
			longitude = 0;
		}
		try
		{
			isFavorite = jo.getBoolean("favorite");
		}
		catch (Exception e)
		{
			isFavorite = false;
		}
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
			
			newEconomy = new ZLDiscountForecast(jo.getJSONObject("nextEconomy"));
		}
		catch (Exception e)
		{
			newEconomy = null;
		}
		try
		{
			currentEconomyStopTime = jo.getString("currentEconomyStopTime");
		}
		catch (Exception e)
		{
			currentEconomyStopTime = "";
		}
		try
		{
			concatCategoryAndParent = jo.getString("concatCategoryAndParent");
		}
		catch (Exception e)
		{
			concatCategoryAndParent = "";
		}
		try
		{
			categoryIconUrl = jo.getString("categoryIcon");
		}
		catch (Exception e)
		{
			categoryIconUrl = "";
		}
		try
		{
			JSONObject joAddr = jo.getJSONObject("address");
			address = new ZLAddress(joAddr);
		}
		catch (Exception e)
		{
			address = new ZLAddress(null);
		}
		try
		{
			JSONObject joCat = jo.getJSONObject("mainCategory");
			category = new ZLCategory(joCat);
		}
		catch (Exception e)
		{
			category = new ZLCategory(null);
		}
		try
		{
			JSONObject joSubCat = jo.getJSONObject("subCategory");
			subCategory = new ZLSubCategory(joSubCat);
			
			try
			{
				
				subCategories.add(subCategory);
				JSONArray subcats = jo.getJSONArray("subCategoryList");
				for (int i = 0; i < subcats.length(); i++)
				{
					ZLSubCategory sc = new ZLSubCategory(subcats.getJSONObject(i));
					subCategories.add(sc);
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		catch (Exception e)
		{
			subCategory = new ZLSubCategory(null);
		}
		try
		{
			JSONObject joBH = jo.getJSONObject("currentBusinessHours");
			businessHours = new ZLBusinessHours(joBH);
		}
		catch (Exception e)
		{
			businessHours = new ZLBusinessHours(null);
		}
		
		//detail stuff
		try
		{
			phoneNumber = jo.getString("phoneNumber");
		}
		catch (Exception e)
		{
			phoneNumber = "";
		}
		try
		{
			contactMail = jo.getString("contactMail");
		}
		catch (Exception e)
		{
			contactMail = "";
		}
		try
		{
			officialWebsite = jo.getString("officialWebsite");
		}
		catch (Exception e)
		{
			officialWebsite = "";
		}
		try
		{
			facebookProfile = jo.getString("facebookProfile");
		}
		catch (Exception e)
		{
			facebookProfile = "";
		}
		try
		{
			twitterProfile = jo.getString("twitterProfile");
		}
		catch (Exception e)
		{
			twitterProfile = "";
		}
		try
		{
			detail = jo.getString("detail");
		}
		catch (Exception e)
		{
			detail = "";
		}
		try
		{
			openingTimes = jo.getString("openingTimes");
		}
		catch (Exception e)
		{
			openingTimes = "";
		}
		try
		{
			logo = jo.getString("logo");
		}
		catch (Exception e)
		{
			logo = "";
		}
		try
		{
			paymentOptions = new ArrayList<ZLPaymentOption>();
			JSONArray ja = jo.getJSONArray("paymentOptions");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject itemJson = ja.getJSONObject(i);
				ZLPaymentOption item = new ZLPaymentOption(itemJson);
				paymentOptions.add(item);
			}
		}
		catch (Exception e)
		{
		}
		try
		{
			availableServices = new ArrayList<ZLAvailableService>();
			JSONArray ja = jo.getJSONArray("availableServices");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject itemJson = ja.getJSONObject(i);
				ZLAvailableService item = new ZLAvailableService(itemJson);
				availableServices.add(item);
			}
		}
		catch (Exception e)
		{
		}
		try
		{
			discountForecast = new ArrayList<ZLDiscountForecast>();
			JSONArray ja = jo.getJSONArray("forecast");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject itemJson = ja.getJSONObject(i);
				ZLDiscountForecast item = new ZLDiscountForecast(itemJson);
				discountForecast.add(item);
			}
		}
		catch (Exception e)
		{
		}
//		try
//		{
//			imageList = new ArrayList<String>();
//			JSONArray ja = jo.getJSONArray("imageList");
//			for (int i = 0; i < ja.length(); i++)
//			{
//				String imageUrl = ja.getString(i);
//				imageList.add(imageUrl);
//			}
//		}
//		catch (Exception e)
//		{
//		}
		try
		{
			mobileImageList = new ArrayList<String>();
			JSONArray ja = jo.getJSONArray("mobileImageList");
			for (int i = 0; i < ja.length(); i++)
			{
				String imageUrl = ja.getString(i);
				mobileImageList.add(imageUrl);
			}
		}
		catch (Exception e)
		{
		}
	}
}
