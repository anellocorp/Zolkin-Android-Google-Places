package com.imd.zolkin.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ZLSurvey
{
	public String surveyId, covenantId, messageId, desc, name;
	public List<ZLSurveyQuestion> questions;
	
	public boolean trigger;
	public String commentAnswer, commentOther;
	public boolean phoneContact, mailContact, answered;
	public boolean isAnswered;
	public ZLSurvey(JSONObject jo, String surveyId, String covenantId, String messageId)
	{
		this.surveyId = surveyId;
		this.covenantId = covenantId;
		this.messageId = messageId;

		try {
			isAnswered = jo.getBoolean("answered");
		} catch (Exception e) {
			isAnswered = false;
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
			desc = jo.getString("description");
		}
		catch (Exception e)
		{
			desc = "";
		}
		questions = new ArrayList<ZLSurveyQuestion>();
		try
		{
			JSONArray ja = jo.getJSONArray("questions");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject joq = ja.getJSONObject(i);
				ZLSurveyQuestion q = new ZLSurveyQuestion(joq);
				questions.add(q);
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public JSONObject getResult()
	{
		trigger = false;
		
		JSONObject root = new JSONObject();
		try
		{
			root.put("surveyId", surveyId);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		try
		{
			root.put("covenantId", covenantId);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		try
		{
			root.put("consumerId", ZLUser.getLoggedUser().userId);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		try {
			root.put("messageId", messageId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONArray answers = new JSONArray();
		try
		{
			root.put("answers", answers);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		for (int i = 0; i < questions.size(); i++)
		{
			ZLSurveyQuestion sq = questions.get(i);
			
			JSONObject qjo = new JSONObject();
			
			try
			{
				qjo.put("questionId", sq.questionId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			switch (sq.questionType)
			{
				case ZLSurveyQuestionTypeMultiChoiceMultiAnswers:
				case ZLSurveyQuestionTypeMultiChoiceUniqueAnswer:
				{
					JSONArray answerIds = new JSONArray();
					for (int j = 0; j < sq.answers.size(); j++)
					{
						ZLSurveyAnswer sa = sq.answers.get(j);
						if (sa.selected)
						{
							answerIds.put(sa.answerId);
						}
					}
					try
					{
						qjo.put("answersId", answerIds);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
					break;
				case ZLSurveyQuestionTypeRating:
				case ZLSurveyQuestionTypeStars:
				{
					if (sq.answerRating <= sq.thresholdVotes)
					{
						trigger = true;
					}
					try
					{
						qjo.put("answer", sq.answerRating);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					break;
				}
				case ZLSurveyQuestionTypeText:
				{
					try
					{
						qjo.put("answer", sq.answerText);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					break;
				}
				default:
					break;
			}
			
			try
			{
				answers.put(qjo);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}//end for each question
		return root;
	}//end getResult
	
} //end class ZLSurvey
