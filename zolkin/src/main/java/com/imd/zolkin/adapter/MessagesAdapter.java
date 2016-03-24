package com.imd.zolkin.adapter;

//Lista de estabelecimentos
//usado na tela de lista

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.imd.zolkin.custom.CircleDrawable;
import com.imd.zolkin.model.ZLMessage;
import com.imd.zolkin.model.ZLMessage.ZLMessageType;
import com.imd.zolkin.util.Util;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.zolkin.R;

public class MessagesAdapter extends BaseAdapter
{
	WeakReference<Context> c;
	List<ZLMessage> messages;

	public MessagesAdapter(Context context, List<ZLMessage> messages)
	{
		c = new WeakReference<Context>(context);
		this.messages = messages;
	}
	
	@Override
	public int getCount()
	{
		return messages.size();
	}

	@Override
	public ZLMessage getItem(int pos)
	{
		return messages.get(pos);
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
			v = li.inflate(R.layout.item_messages_message, vg, false);
		}
		
		View vNew = (View) v.findViewById(R.id.vNew);
		com.innovattic.font.FontTextView tvTitle = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvTitle);
		com.innovattic.font.FontTextView tvTime = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvTime);
		com.innovattic.font.FontTextView tvSurvey = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvSurvey);
		com.innovattic.font.FontTextView tvStoreName = (com.innovattic.font.FontTextView) v.findViewById(R.id.tvStoreName);
		
		ZLMessage message = messages.get(pos);
		
		vNew.setVisibility(message.status == ZLMessageType.ZLMessageTypeNew ? View.VISIBLE : View.INVISIBLE);
		
		if (message.status == ZLMessageType.ZLMessageTypeNew)
		{
			vNew.setVisibility(View.VISIBLE);
			vNew.setBackground(new CircleDrawable(c.get().getResources().getColor(R.color.zl_light_blue), 1));
		}
		else
		{
			vNew.setVisibility(View.INVISIBLE);
		}
		
		tvTitle.setText(message.subject);
		try
		{
			tvTime.setText(message.dateString.substring(11, 16));
		}
		catch (Exception e)
		{
			
		}
		
		if (message.status == ZLMessageType.ZLMessageTypeNew || message.status == ZLMessageType.ZLMessageTypeUnread)
		{
			//show as unread
			if (message.idPesquisa != null)
			{
				RoundRectShape s = new RoundRectShape(new float[] {5,5, 5,5, 5,5, 5,5}, null, null);
				ShapeDrawable sd = new ShapeDrawable(s);
				sd.getPaint().setColor(c.get().getResources().getColor(R.color.zl_green));
				tvSurvey.setBackground(sd);
				tvSurvey.setTextColor(Color.BLACK);
				tvSurvey.setVisibility(View.VISIBLE);
				tvStoreName.setText(message.desc);
			}
			else
			{
				tvSurvey.setVisibility(View.GONE);
				tvStoreName.setText(message.desc);
			}
			tvTitle.setTextColor(c.get().getResources().getColor(R.color.zl_purple));
			tvStoreName.setTextColor(Color.argb(255, 0, 0, 0));
		}
		else
		{
			//show as read
			if (message.idPesquisa != null)
			{
				float px = Util.convertDpToPixel(5, c.get());
				RoundRectShape s = new RoundRectShape(new float[] {px,px, px,px, px,px, px,px}, null, null);
				ShapeDrawable sd = new ShapeDrawable(s);
				sd.getPaint().setColor(Color.argb(255, 0, 0, 0));
				tvSurvey.setBackground(sd);
				tvSurvey.setTextColor(Color.WHITE);
				tvSurvey.setVisibility(View.VISIBLE);
				tvStoreName.setText(message.desc);
			}
			else
			{
				tvSurvey.setVisibility(View.GONE);
				tvStoreName.setText(message.desc);
			}
			tvTitle.setTextColor(Color.argb(255, 170, 170, 170));
			tvStoreName.setTextColor(Color.argb(255, 170, 170, 170));
		}

//		tvSurvey.setVisibility(message.idPesquisa != null ? View.VISIBLE : View.GONE);
//		tvStoreName.setText(message.desc);
		
		return v;
	}
}
