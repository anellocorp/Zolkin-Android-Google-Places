package com.imd.zolkin.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ZLSurveyQuestion
{
	public enum ZLSurveyQuestionType
	{
		ZLSurveyQuestionTypeText,
	    ZLSurveyQuestionTypeMultiChoiceUniqueAnswer,
	    ZLSurveyQuestionTypeMultiChoiceMultiAnswers,
	    ZLSurveyQuestionTypeStars,
	    ZLSurveyQuestionTypeRating
	};
	
	public int questionId, totalVotes, thresholdVotes;
	public String question;
	public ZLSurveyQuestionType questionType;
	public List<ZLSurveyAnswer> answers;
	
	//answer data
	public String answerText;
	public int answerRating;
	
	public ZLSurveyQuestion(JSONObject jo)
	{
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
			questionId = jo.getInt("id");
		}
		catch (Exception e)
		{
			questionId = 0;
		}
		
		String typeString = "";
		try
		{
			typeString = jo.getString("type");
		}
		catch (Exception e)
		{
			typeString = "";
		}
		
		if (typeString.equals("TEXT"))
		{
			questionType = ZLSurveyQuestionType.ZLSurveyQuestionTypeText;
			totalVotes = 0;
			thresholdVotes = 0;
		}
		else if (typeString.equals("MULTIPLE_CHOICE_UNIQUE_ANSWER"))
		{
			questionType = ZLSurveyQuestionType.ZLSurveyQuestionTypeMultiChoiceUniqueAnswer;
			answers = new ArrayList<ZLSurveyAnswer>();
			try
			{
				JSONArray jaa = jo.getJSONArray("answers");
				for (int i = 0; i < jaa.length(); i++)
				{
					JSONObject joa = jaa.getJSONObject(i);
					ZLSurveyAnswer ans = new ZLSurveyAnswer(joa);
					answers.add(ans);
				}
			}
			catch (Exception e)
			{
			}
			totalVotes = answers.size();
			thresholdVotes = 0;
		}
		else if (typeString.equals("MULTIPLE_CHOICE_MULTIPLE_ANSWERS"))
		{
			questionType = ZLSurveyQuestionType.ZLSurveyQuestionTypeMultiChoiceMultiAnswers;
			answers = new ArrayList<ZLSurveyAnswer>();
			try
			{
				JSONArray jaa = jo.getJSONArray("answers");
				for (int i = 0; i < jaa.length(); i++)
				{
					JSONObject joa = jaa.getJSONObject(i);
					ZLSurveyAnswer ans = new ZLSurveyAnswer(joa);
					answers.add(ans);
				}
			}
			catch (Exception e)
			{
			}
			totalVotes = answers.size();
			thresholdVotes = 0;
		}
		else if (typeString.equals("VOTES"))
		{
			try
			{
				boolean stars = jo.getBoolean("stars");
				questionType = stars ? ZLSurveyQuestionType.ZLSurveyQuestionTypeStars : ZLSurveyQuestionType.ZLSurveyQuestionTypeRating;
				//answers = jo.getBoolean("answers");
				totalVotes = jo.getInt("quantityTotalVotes");
				thresholdVotes = jo.getInt("quantityThresholdVotes");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
