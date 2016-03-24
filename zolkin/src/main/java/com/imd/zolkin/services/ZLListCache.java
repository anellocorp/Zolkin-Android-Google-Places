package com.imd.zolkin.services;

//classe que gerencia a lista de termos buscados recentemente.
//usado na Home

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.imd.zolkin.activity.BaseZolkinActivity;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class ZLListCache
{
	public class ZLTerm
	{
		public String term;
		public String updateTime;

		public ZLTerm(String term, String updateTime)
		{
			this.term = term;
			this.updateTime = updateTime;
		}

		public ZLTerm(JSONObject jo)
		{
			try
			{
				this.term = jo.getString("term");
				this.updateTime = jo.getString("updateTime");
			}
			catch (Exception e)
			{
				this.term = "";
				this.updateTime = "";
			}
		}

		public JSONObject getJsonObject()
		{
			JSONObject jo = new JSONObject();
			try
			{
				jo.put("term", term);
				jo.put("updateTime", updateTime);
			}
			catch (JSONException e)
			{

			}

			return jo;
		}
	}

	private ArrayList<ZLTerm> terms;

	private static ZLListCache sharedCache = null;

	public static ZLListCache getInstance()
	{
		if (sharedCache == null)
			sharedCache = new ZLListCache();
		return sharedCache;
	}

	private ZLListCache()
	{
		// load terms
		load();
	}

	private void load()
	{
		terms = new ArrayList<ZLTerm>();
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("cachedTerms"))
		{
			String termsJson = prefs.getString("cachedTerms", "[]");
			try
			{
				JSONArray ja = new JSONArray(termsJson);
				for (int i = 0; i < ja.length(); i++)
				{
					JSONObject jo = ja.getJSONObject(i);
					terms.add(new ZLTerm(jo));
				}
			}
			catch (Exception e)
			{

			}

		}
	}

	private void save()
	{
		JSONArray ja = new JSONArray();
		for (int i = 0; i < terms.size(); i++)
		{
			ja.put(terms.get(i).getJsonObject());
		}
		String json = ja.toString();
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("cachedTerms", json);
		editor.apply();
	}

	public ArrayList<ZLTerm> getTerms()
	{
		return terms;
	}

	public void addTerm(String term, String json)
	{
		for (int i = 0; i < terms.size(); i++)
		{
			if (terms.get(i).term.toLowerCase().equals(term.toLowerCase()))
			{
				terms.remove(i);
				break;
			}
		}
		Calendar c = Calendar.getInstance();
		terms.add(new ZLTerm(term, c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)));
		
		save();

		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("TERM-" + term.toLowerCase(), json);
		editor.apply();
	}
	
	public void deleteTerm(ZLTerm t)
	{
		for (int i = 0; i < terms.size(); i++)
		{
			if (terms.get(i).term.toLowerCase().equals(t.term.toLowerCase()))
			{
				terms.remove(i);
				break;
			}
		}
		save();
		
		
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		if (prefs.contains("TERM-" + t.term.toLowerCase()))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove("TERM-" + t.term.toLowerCase());
			editor.apply();
			
		}
	}

	public ArrayList<ZLStore> getListForTerm(String term)
	{
		ArrayList<ZLStore> result = new ArrayList<ZLStore>();
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("TERM-" + term.toLowerCase()))
		{
			String json = prefs.getString("TERM-" + term.toLowerCase(), "[]");
			try
			{
				JSONArray ja = new JSONArray(json);

				for (int i = 0; i < ja.length(); i++)
				{
					JSONObject jof = ja.getJSONObject(i);
					ZLStore cov = new ZLStore(jof);
					result.add(cov);
				}

				return result;

			}
			catch (Exception e)
			{
				try
				{
					JSONObject jo = new JSONObject(json);
					JSONArray ja = jo.getJSONArray("data");
					int cnt = ja.length();
					Log.d("Zolkin", "CNT = " + cnt);
					for (int i = 0; i < cnt; i++)
					{
						JSONObject jof = ja.getJSONObject(i);
						ZLStore cov = new ZLStore(jof);
						result.add(cov);
						Log.d("Zolkin", "" + i);
					}

					return result;

				}
				catch (Exception ee)
				{
					return null;
				}
			}
		}
		else
		{
			// this should never happen
			return null;
		}
	}
}
