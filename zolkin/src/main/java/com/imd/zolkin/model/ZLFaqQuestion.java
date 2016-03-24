package com.imd.zolkin.model;

import org.json.JSONObject;

//um par pergunta/resposta do FAQ

public class ZLFaqQuestion
{
	public int questionId;
	public String question, answer;
	
	public boolean expanded = false;
	
	
	public ZLFaqQuestion(JSONObject jo)
	{
		try
		{
			questionId = jo.getInt("id");
		}
		catch (Exception e)
		{
			questionId = 0;
		}
		try
		{
			question = jo.getString("question");
		}
		catch (Exception e)
		{
			question = "";
		}
		try
		{
			answer = jo.getString("answer").replace("<br>", "\n");
		}
		catch (Exception e)
		{
			answer = "";
		}
	}
}
