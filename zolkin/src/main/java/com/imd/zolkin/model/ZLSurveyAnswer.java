package com.imd.zolkin.model;

import android.view.View.OnClickListener;

import org.json.JSONObject;

public class ZLSurveyAnswer
{
	public int answerId, score;
	public String answer;
	public boolean selected;
	
	public OnClickListener toggleClick = null;
	
	public ZLSurveyAnswer(JSONObject jo)
	{
		try
		{
			answer = jo.getString("answer");
		}
		catch (Exception e)
		{
			answer = "";
		}
		try
		{
			answerId = jo.getInt("id");
		}
		catch (Exception e)
		{
			answerId = 0;
		}
		
		selected = false;
		score = 0;
	}
	
	
}
