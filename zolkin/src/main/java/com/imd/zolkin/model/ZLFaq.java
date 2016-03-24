package com.imd.zolkin.model;

//um grupo do FAQ

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ZLFaq
{
	public int faqGroupId;
	public String faqGroupName;
	public List<ZLFaqQuestion> questions;
	
	public boolean expanded = false;
	
	public static List<ZLFaq> faq;
	
	public ZLFaq(JSONObject jo)
	{
		try
		{
			faqGroupId = jo.getInt("id");
		}
		catch (Exception e)
		{
			faqGroupId = 0;
		}
		try
		{
			faqGroupName = jo.getString("name");
		}
		catch (Exception e)
		{
			faqGroupName = "";
		}
		
		try
		{
			questions = new ArrayList<ZLFaqQuestion>();
			
			JSONArray jaQuestions = jo.getJSONArray("questionData");
			
			for (int i = 0; i < jaQuestions.length(); i++)
			{
				JSONObject joq = jaQuestions.getJSONObject(i);
				ZLFaqQuestion q = new ZLFaqQuestion(joq);
				questions.add(q);
			}
		}
		catch (Exception e)
		{
			
		}
	}
}
