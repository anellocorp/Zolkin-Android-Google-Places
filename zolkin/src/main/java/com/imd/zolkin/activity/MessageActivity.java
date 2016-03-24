package com.imd.zolkin.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import br.com.zolkin.R;

import com.imd.zolkin.model.ZLMessage;
import com.imd.zolkin.model.ZLMessage.ZLMessageType;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

public class MessageActivity extends BaseZolkinActivity
{
	ImageView btVoltar = null;
	FrameLayout btMenu = null;
	TextView tvTitle = null;
	TextView tvDateTime = null;
	TextView tvMessage = null;
	
	public static ZLMessage message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvDateTime = (TextView) findViewById(R.id.tvDateTime);
		tvMessage = (TextView) findViewById(R.id.tvMessage);
		
		btVoltar.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});
		
		tvTitle.setText(message.subject);
		try
		{
			tvDateTime.setText(message.dateString.substring(11, 16));
		}
		catch (Exception e)
		{
			
		}
		tvMessage.setText(message.message);
		
		if (message.status == ZLMessageType.ZLMessageTypeNew || message.status == ZLMessageType.ZLMessageTypeUnread)
		{
			ZLServices.getInstance().changeMessageStatus(message, ZLMessageType.ZLMessageTypeRead, false, this, new ZLServiceOperationCompleted<Boolean>()
			{
				
				@Override
				public void operationCompleted(ZLServiceResponse<Boolean> response)
				{
					if (response.errorMessage == null)
						message.status = ZLMessageType.ZLMessageTypeRead;
				}
			});
		}
	}
}
