package com.imd.zolkin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imd.zolkin.model.ZLFaq;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class FAQGroupsAdapter extends BaseAdapter
{
	//lista os grupos do FAQ
	WeakReference<Context> c;
	List<ZLFaq> groups;

	public FAQGroupsAdapter(Context context, List<ZLFaq> groups)
	{
		c = new WeakReference<Context>(context);
		this.groups = groups;
	}
	
	@Override
	public int getCount()
	{
		return groups.size();
	}

	@Override
	public ZLFaq getItem(int pos)
	{
		return groups.get(pos);
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
			v = li.inflate(R.layout.item_faq_group, vg, false);
		}
		
		final ZLFaq group = groups.get(pos);
		
		LinearLayout llBtGroup = (LinearLayout) v.findViewById(R.id.llBtGroup);
		TextView tvGroupName = (TextView) v.findViewById(R.id.tvGroupName);
		final LinearLayout llGroupQuestions = (LinearLayout) v.findViewById(R.id.llGroupQuestions);
		
		tvGroupName.setText(group.faqGroupName);
		
		if (group.expanded)
		{
			llGroupQuestions.setVisibility(View.VISIBLE);
		}
		else
		{
			llGroupQuestions.setVisibility(View.GONE);
		}
		
		llBtGroup.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				group.expanded = !group.expanded;
				if (group.expanded)
				{
					llGroupQuestions.setVisibility(View.VISIBLE);
				}
				else
				{
					llGroupQuestions.setVisibility(View.GONE);
				}
			}
		});
		
		FAQQuestionsAdapter adapter = new FAQQuestionsAdapter(c.get(), group.questions);
		
		llGroupQuestions.removeAllViews();
		
		for (int i = 0; i < adapter.getCount(); i++)
		{
			llGroupQuestions.addView(adapter.getView(i, null, llGroupQuestions));
		}
		
		return v;
	}
}
