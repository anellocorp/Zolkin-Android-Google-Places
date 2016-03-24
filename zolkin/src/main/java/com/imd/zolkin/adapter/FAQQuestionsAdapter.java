package com.imd.zolkin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imd.zolkin.model.ZLFaqQuestion;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class FAQQuestionsAdapter extends BaseAdapter
{
	//lista perguntas e respostas de um determinado grupo do FAQ
	
	WeakReference<Context> c;
	List<ZLFaqQuestion> questions;

	public FAQQuestionsAdapter(Context context, List<ZLFaqQuestion> questions)
	{
		c = new WeakReference<Context>(context);
		this.questions = questions;
	}
	
	@Override
	public int getCount()
	{
		return questions.size();
	}

	@Override
	public ZLFaqQuestion getItem(int pos)
	{
		return questions.get(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup vg)
	{
		if (v == null)
		{
			LayoutInflater li = LayoutInflater.from(c.get());
			v = li.inflate(R.layout.item_faq_question, vg, false);
		}
		
		final ZLFaqQuestion question = questions.get(pos);
		
		LinearLayout llBtQuestion = (LinearLayout) v.findViewById(R.id.llBtQuestion);
		TextView tvQuestion = (TextView) v.findViewById(R.id.tvQuestion);
		final TextView tvAnswer = (TextView) v.findViewById(R.id.tvAnswer);

		tvAnswer.setText(question.answer);
		tvQuestion.setText(question.question);
		
		if (question.expanded)
		{
			tvAnswer.setVisibility(View.VISIBLE);
		}
		else
		{
			tvAnswer.setVisibility(View.GONE);
		}
		
		llBtQuestion.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				question.expanded = !question.expanded;
				if (question.expanded)
				{
					tvAnswer.setVisibility(View.VISIBLE);
				}
				else
				{
					tvAnswer.setVisibility(View.GONE);
				}
			}
		});
		
		return v;
	}
}
