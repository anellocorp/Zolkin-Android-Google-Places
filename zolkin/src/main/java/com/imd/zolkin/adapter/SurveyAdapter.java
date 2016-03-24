package com.imd.zolkin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imd.zolkin.model.ZLSurvey;
import com.imd.zolkin.model.ZLSurveyAnswer;
import com.imd.zolkin.model.ZLSurveyQuestion;

import java.lang.ref.WeakReference;

import br.com.zolkin.R;

public class SurveyAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	ZLSurvey survey;
	LayoutInflater li;
	
	public SurveyAdapter(Context context, ZLSurvey s)
	{
		c = new WeakReference<Context>(context);
		survey = s;
		li = LayoutInflater.from(c.get());
	}

	@Override
	public int getCount()
	{
		return survey.questions.size();
	}

	@Override
	public Object getItem(int pos)
	{
		return survey.questions.get(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup vg)
	{
		if (pos > survey.questions.size())
		{
			return li.inflate(R.layout.item_survey_padding, vg, false);
		}
		if (pos == survey.questions.size())
		{
			return li.inflate(R.layout.item_survey_bt_enviar, vg, false);
		}
		
		ZLSurveyQuestion q = survey.questions.get(pos);
		switch (q.questionType)
		{
			case ZLSurveyQuestionTypeText:
				return getTextQuestionView(q, pos, vg);
			case ZLSurveyQuestionTypeMultiChoiceMultiAnswers:
				return getMultiMultiQuestionView(q, pos, vg);
			case ZLSurveyQuestionTypeMultiChoiceUniqueAnswer:
				return getMultiSingleQuestionView(q, pos, vg);
			case ZLSurveyQuestionTypeRating:
				return getRatingCircleQuestionView(q, pos, vg);
			case ZLSurveyQuestionTypeStars:
				return getRatingStarQuestionView(q, pos, vg);


		}
		return null;
	}
	
	View getTextQuestionView(final ZLSurveyQuestion q, final int pos, ViewGroup vg)
	{
		View v = li.inflate(R.layout.item_survey_text, vg, false);
		
		final com.innovattic.font.FontTextView tvQuestion = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvQuestion);
		final com.innovattic.font.FontEditText etAnswer = (com.innovattic.font.FontEditText) v.findViewById(R.id.etAnswer);
		
		if (q.answerText == null)
			q.answerText = "";
		
		tvQuestion.setText(q.question);
		etAnswer.setText(q.answerText);

		etAnswer.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				q.answerText = s.toString();
			}
		});
		
		return v;
	}
	
	View getMultiMultiQuestionView(ZLSurveyQuestion q, int pos, ViewGroup vg)
	{
		View v = li.inflate(R.layout.item_survey_multi_multi, vg, false);
		
		LinearLayout lvRoot = (LinearLayout) v.findViewById(R.id.lvRoot);
		com.innovattic.font.FontTextView tvQuestion = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvQuestion);

		tvQuestion.setText(q.question);
		
		for (int i = 0; i < q.answers.size(); i++)
		{
			final ZLSurveyAnswer answer = q.answers.get(i);
			View va = li.inflate(R.layout.item_survey_multi_item_answer, lvRoot, false);
			lvRoot.addView(va);
			
			final ImageView imgvCheckbox = (ImageView) va.findViewById(R.id.imgvCheckbox);
			com.innovattic.font.FontTextView tvAnswerText = (com.innovattic.font.FontTextView) va.findViewById(R.id.tvAnswerText);
			
			tvAnswerText.setText(answer.answer);
			if (answer.selected)
			{
				imgvCheckbox.setImageResource(R.drawable.checkbox_s);
			}
			else
			{
				imgvCheckbox.setImageResource(R.drawable.checkbox_n);
			}
			
			va.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View arg0)
				{
					answer.selected = !answer.selected;
					if (answer.selected)
					{
						imgvCheckbox.setImageResource(R.drawable.checkbox_s);
					}
					else
					{
						imgvCheckbox.setImageResource(R.drawable.checkbox_n);
					}
				}
			});
		}
		
		return v;
	}
	
	View getMultiSingleQuestionView(final ZLSurveyQuestion q, int pos, ViewGroup vg)
	{
		View v = li.inflate(R.layout.item_survey_multi_single, vg, false);
		
		final LinearLayout lvRoot = (LinearLayout) v.findViewById(R.id.lvRoot);
		com.innovattic.font.FontTextView tvQuestion = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvQuestion);
		
		
		tvQuestion.setText(q.question);
		
		
		
		for (int i = 0; i < q.answers.size(); i++)
		{
			final ZLSurveyAnswer answer = q.answers.get(i);
			View va = li.inflate(R.layout.item_survey_multi_item_answer, lvRoot, false);
			lvRoot.addView(va);
			
			final ImageView imgvCheckbox = (ImageView) va.findViewById(R.id.imgvCheckbox);
			com.innovattic.font.FontTextView tvAnswerText = (com.innovattic.font.FontTextView) va.findViewById(R.id.tvAnswerText);
			
			tvAnswerText.setText(answer.answer);
			if (answer.selected)
			{
				imgvCheckbox.setImageResource(R.drawable.checkbox2_s);
			}
			else
			{
				imgvCheckbox.setImageResource(R.drawable.checkbox2_n);
			}
			
			va.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View arg0)
				{
					
					
					for (int j = 0; j < q.answers.size(); j++)
					{
						final ZLSurveyAnswer ao = q.answers.get(j);
						ao.selected = false;
					}
					answer.selected = true;
					
					for (int k = 0; k < lvRoot.getChildCount(); k++)
					{
						View vao = lvRoot.getChildAt(k);
						ImageView imgvCheckboxO = (ImageView) vao.findViewById(R.id.imgvCheckbox);
						if (imgvCheckboxO != null)
							imgvCheckboxO.setImageResource(R.drawable.checkbox2_n);
					}
					imgvCheckbox.setImageResource(R.drawable.checkbox2_s);
				}
			});
		}
		
		return v;
	}
	
	View getRatingCircleQuestionView(final ZLSurveyQuestion q, int pos, ViewGroup vg)
	{
		View v = li.inflate(R.layout.item_survey_rating_circle, vg, false);
		
		LinearLayout lvRoot = (LinearLayout) v.findViewById(R.id.lvRoot);
		com.innovattic.font.FontTextView tvQuestion = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvQuestion);
		FrameLayout flR1 = (FrameLayout) v.findViewById(R.id.flR1);
		com.innovattic.font.FontTextView tvR1 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR1);
		FrameLayout flR2 = (FrameLayout) v.findViewById(R.id.flR2);
		com.innovattic.font.FontTextView tvR2 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR2);
		FrameLayout flR3 = (FrameLayout) v.findViewById(R.id.flR3);
		com.innovattic.font.FontTextView tvR3 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR3);
		FrameLayout flR4 = (FrameLayout) v.findViewById(R.id.flR4);
		com.innovattic.font.FontTextView tvR4 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR4);
		FrameLayout flR5 = (FrameLayout) v.findViewById(R.id.flR5);
		com.innovattic.font.FontTextView tvR5 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR5);
		/*FrameLayout flR6 = (FrameLayout) v.findViewById(R.id.flR6);
		com.innovattic.font.FontTextView tvR6 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR6);
		FrameLayout flR7 = (FrameLayout) v.findViewById(R.id.flR7);
		com.innovattic.font.FontTextView tvR7 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR7);
		FrameLayout flR8 = (FrameLayout) v.findViewById(R.id.flR8);
		com.innovattic.font.FontTextView tvR8 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR8);
		FrameLayout flR9 = (FrameLayout) v.findViewById(R.id.flR9);
		com.innovattic.font.FontTextView tvR9 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR9);
		FrameLayout flR10 = (FrameLayout) v.findViewById(R.id.flR10);
		com.innovattic.font.FontTextView tvR10 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR10);*/
		
		tvQuestion.setText(q.question);
		
		final FrameLayout[] fls = new FrameLayout[] { flR1, flR2, flR3, flR4, flR5/*, flR6, flR7, flR8, flR9, flR10*/};
		final TextView[] tvs = new TextView[] { tvR1, tvR2, tvR3, tvR4, tvR5/*, tvR6, tvR7, tvR8, tvR9, tvR10*/};
		
		for (int i = 0; i < 5; i++)
		{
			TextView tv = tvs[i];
			//set background and text color
			tv.setBackgroundResource(i < q.answerRating ? R.drawable.circle_s : R.drawable.circle_n);
			tv.setTextColor(i < q.answerRating ? Color.WHITE : c.get().getResources().getColor(R.color.zl_purple));
			
			
		}
		
		OnClickListener cl = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int pos = ( (Integer) v.getTag()).intValue();
				q.answerRating = pos+1;
				for (int i = 0; i < 5; i++)
				{
					TextView tv = tvs[i];
					//set background and text color
					tv.setBackgroundResource(i <= pos ? R.drawable.circle_s : R.drawable.circle_n);
					tv.setTextColor(i <= pos ? Color.WHITE : c.get().getResources().getColor(R.color.zl_purple));
				}
			}
		};
		
		for (int i = 0; i < 5; i++)
		{
			FrameLayout fl = fls[i];
			TextView tv = tvs[i];
			fl.setTag(new Integer(i));
			tv.setTag(new Integer(i));
			fl.setOnClickListener(cl);
			
			fl.setVisibility(i < q.totalVotes ? View.VISIBLE : View.INVISIBLE);
		}
		
		
		
		return v;
	}
	
	View getRatingStarQuestionView(final ZLSurveyQuestion q, int pos, ViewGroup vg)
	{
		View v = li.inflate(R.layout.item_survey_rating_star, vg, false);
		
		LinearLayout lvRoot = (LinearLayout) v.findViewById(R.id.lvRoot);
		com.innovattic.font.FontTextView tvQuestion = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvQuestion);
		FrameLayout flR1 = (FrameLayout) v.findViewById(R.id.flR1);
		com.innovattic.font.FontTextView tvR1 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR1);
		FrameLayout flR2 = (FrameLayout) v.findViewById(R.id.flR2);
		com.innovattic.font.FontTextView tvR2 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR2);
		FrameLayout flR3 = (FrameLayout) v.findViewById(R.id.flR3);
		com.innovattic.font.FontTextView tvR3 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR3);
		FrameLayout flR4 = (FrameLayout) v.findViewById(R.id.flR4);
		com.innovattic.font.FontTextView tvR4 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR4);
		FrameLayout flR5 = (FrameLayout) v.findViewById(R.id.flR5);
		com.innovattic.font.FontTextView tvR5 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR5);
		/*FrameLayout flR6 = (FrameLayout) v.findViewById(R.id.flR6);
		com.innovattic.font.FontTextView tvR6 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR6);
		FrameLayout flR7 = (FrameLayout) v.findViewById(R.id.flR7);
		com.innovattic.font.FontTextView tvR7 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR7);
		FrameLayout flR8 = (FrameLayout) v.findViewById(R.id.flR8);
		com.innovattic.font.FontTextView tvR8 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR8);
		FrameLayout flR9 = (FrameLayout) v.findViewById(R.id.flR9);
		com.innovattic.font.FontTextView tvR9 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR9);
		FrameLayout flR10 = (FrameLayout) v.findViewById(R.id.flR10);
		com.innovattic.font.FontTextView tvR10 = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvR10);*/

		tvQuestion.setText(q.question);

		final FrameLayout[] fls = new FrameLayout[] { flR1, flR2, flR3, flR4, flR5/*, flR6, flR7, flR8, flR9, flR10*/};
		final TextView[] tvs = new TextView[] { tvR1, tvR2, tvR3, tvR4, tvR5/*, tvR6, tvR7, tvR8, tvR9, tvR10*/};
		
		for (int i = 0; i < 5; i++)
		{
			TextView tv = tvs[i];
			//set background and text color
			tv.setBackgroundResource(i < q.answerRating ? R.drawable.star_s : R.drawable.star_n);
			tv.setTextColor(i < q.answerRating ? Color.WHITE : c.get().getResources().getColor(R.color.zl_purple));
		}
		
		OnClickListener cl = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int pos = ( (Integer) v.getTag()).intValue();
				q.answerRating = pos+1;
				for (int i = 0; i < 5; i++)
				{
					TextView tv = tvs[i];
					//set background and text color
					tv.setBackgroundResource(i <= pos ? R.drawable.star_s : R.drawable.star_n);
					tv.setTextColor(i <= pos ? Color.WHITE : c.get().getResources().getColor(R.color.zl_purple));
				}
			}
		};
		
		for (int i = 0; i < 5; i++)
		{
			FrameLayout fl = fls[i];
			TextView tv = tvs[i];
			Integer ii = new Integer(i);
			fl.setTag(ii);
			tv.setTag(ii);
			fl.setOnClickListener(cl);
			
			fl.setVisibility(i < q.totalVotes ? View.VISIBLE : View.INVISIBLE);
		}
		
		return v;
	}
}
