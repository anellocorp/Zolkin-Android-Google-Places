package com.imd.zolkin.activity;

//Tela de FAQ

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.imd.zolkin.adapter.FAQGroupsAdapter;
import com.imd.zolkin.model.ZLFaq;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

import br.com.zolkin.R;

public class FAQActivity extends BaseZolkinMenuActivity
{
	ImageView btVoltar = null;
	ListView lvFAQ = null;
	
	FAQGroupsAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		lvFAQ = (ListView) findViewById(R.id.lvFAQ);
		
		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});
		
		//pega o FAQ do serviço
		ZLServices.getInstance().getFaq(true, this, new ZLServiceOperationCompleted<Boolean>()
		{
			
			@Override
			public void operationCompleted(ZLServiceResponse<Boolean> response)
			{
				if (response.errorMessage != null)
				{
					Toast.makeText(FAQActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
					finish();
				}
				else
				{
					//cria o adapter e mostra na lista
					adapter = new FAQGroupsAdapter(FAQActivity.this, ZLFaq.faq);
					lvFAQ.setAdapter(adapter);
				}
			}
		});
	}
}
