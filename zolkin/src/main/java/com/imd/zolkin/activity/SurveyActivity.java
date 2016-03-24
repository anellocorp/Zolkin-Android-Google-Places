package com.imd.zolkin.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import br.com.zolkin.R;

import com.imd.zolkin.adapter.SurveyAdapter;
import com.imd.zolkin.model.ZLMessage;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLSurvey;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.model.ZLMessage.ZLMessageType;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

public class SurveyActivity extends BaseZolkinActivity
{
	ImageView btVoltar = null;

	com.innovattic.font.FontTextView tvNomeEC = null;
	com.innovattic.font.FontTextView tvNomePesquisa = null;
	//ListView lvSurvey = null;
	LinearLayout llSurvey = null;
	FrameLayout flComment = null;
	FrameLayout flSurvey = null;
	LinearLayout lLTelefone = null;
	ImageView imgvCheckboxTelefone = null;
	com.innovattic.font.FontTextView tvTelefone = null;
	LinearLayout lLEmail = null;
	ImageView imgvCheckboxEmail = null;
	com.innovattic.font.FontTextView tvEmail = null;
	LinearLayout lLOutros = null;
	ImageView imgvCheckboxOutros = null;
	com.innovattic.font.FontEditText etOutros = null;
	LinearLayout lLNao = null;
	ImageView imgvCheckboxNao = null;
	com.innovattic.font.FontEditText etComment = null;
	com.innovattic.font.FontTextView tvBtEnviarComentario = null;

	View vBtEnviar = null;

	
	public static ZLMessage message;
	private ZLSurvey survey;
	private SurveyAdapter adapter = null;
	
	void resetTriggerSelections()
	{
		imgvCheckboxEmail.setImageResource(R.drawable.checkbox2_n);
		imgvCheckboxOutros.setImageResource(R.drawable.checkbox2_n);
		imgvCheckboxNao.setImageResource(R.drawable.checkbox2_n);
		imgvCheckboxTelefone.setImageResource(R.drawable.checkbox2_n);
		
		survey.phoneContact = false;
		survey.mailContact = false;
		survey.commentOther = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		tvNomePesquisa = (com.innovattic.font.FontTextView) findViewById(R.id.tvNomePesquisa);
		tvNomeEC = (com.innovattic.font.FontTextView) findViewById(R.id.tvNomeEC);
		//lvSurvey = (ListView) findViewById(R.id.lvSurvey);
		flSurvey = (FrameLayout) findViewById(R.id.flSurvey);
		flComment = (FrameLayout) findViewById(R.id.flComment);
		lLTelefone = (LinearLayout) findViewById(R.id.lLTelefone);
		llSurvey = (LinearLayout) findViewById(R.id.llSurvey);
		imgvCheckboxTelefone = (ImageView) findViewById(R.id.imgvCheckboxTelefone);
		tvTelefone = (com.innovattic.font.FontTextView) findViewById(R.id.tvTelefone);
		lLEmail = (LinearLayout) findViewById(R.id.lLEmail);
		imgvCheckboxEmail = (ImageView) findViewById(R.id.imgvCheckboxEmail);
		tvEmail = (com.innovattic.font.FontTextView) findViewById(R.id.tvEmail);
		lLOutros = (LinearLayout) findViewById(R.id.lLOutros);
		imgvCheckboxOutros = (ImageView) findViewById(R.id.imgvCheckboxOutros);
		etOutros = (com.innovattic.font.FontEditText) findViewById(R.id.etOutros);
		lLNao = (LinearLayout) findViewById(R.id.lLNao);
		imgvCheckboxNao = (ImageView) findViewById(R.id.imgvCheckboxNao);
		etComment = (com.innovattic.font.FontEditText) findViewById(R.id.etComment);
		tvBtEnviarComentario = (com.innovattic.font.FontTextView) findViewById(R.id.tvBtEnviarComentario);
		vBtEnviar = findViewById(R.id.vBtEnviar);
		
		tvNomePesquisa.setText(message.nomePesquisa);
		tvTelefone.setText("Por telefone (" + ZLUser.getLoggedUser().mobileNumber + ")");
		tvEmail.setText("Por e-mail (" + ZLUser.getLoggedUser().email + ")");
		
		btVoltar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		flComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		
		flComment.setVisibility(View.GONE);
		lLTelefone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetTriggerSelections();
				imgvCheckboxTelefone.setImageResource(R.drawable.checkbox2_s);
				survey.phoneContact = true;
			}
		});
		lLEmail.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				resetTriggerSelections();
				imgvCheckboxEmail.setImageResource(R.drawable.checkbox2_s);
				survey.mailContact = true;
			}
		});
		lLOutros.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				resetTriggerSelections();
				imgvCheckboxOutros.setImageResource(R.drawable.checkbox2_s);
				survey.commentOther = "";
			}
		});
		lLNao.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				resetTriggerSelections();
				imgvCheckboxNao.setImageResource(R.drawable.checkbox2_s);
			}
		});
		
		tvBtEnviarComentario.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (survey.commentOther != null)
					survey.commentOther = etOutros.getText().toString();
				survey.commentAnswer = etComment.getText().toString();
				ZLServices.getInstance().commentSurvey(survey, true, SurveyActivity.this, new ZLServiceOperationCompleted<Boolean>() {
					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response) {
						if (response.errorMessage != null) {
							Toast.makeText(SurveyActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(SurveyActivity.this, "Obrigado por participar da pesquisa do Zolkin.", Toast.LENGTH_LONG).show();
							finish();
						}
					}
				});
			}
		});

		try
		{
			ZLServices.getInstance().getCovenantDetails(Integer.parseInt(message.covenantID), false, this, new ZLServiceOperationCompleted<ZLStore>()
			{
				@Override
				public void operationCompleted(ZLServiceResponse<ZLStore> response)
				{
					if (response.errorMessage == null)
					{
						tvNomeEC.setText(response.serviceResponse.name);
					}
				}
			});
		}
		catch (Exception eeaa)
		{

		}
		
		ZLServices.getInstance().getSurvey(message.messageID, message.idPesquisa, message.covenantID, true, this, new ZLServiceOperationCompleted<ZLSurvey>()
		{
			
			@Override
			public void operationCompleted(ZLServiceResponse<ZLSurvey> response)
			{
				if (response.errorMessage != null)
				{
					Toast.makeText(SurveyActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
				}
				else
				{
					survey = response.serviceResponse;
					adapter = new SurveyAdapter(SurveyActivity.this, survey);

					vBtEnviar.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//enviar clicked
							ZLServices.getInstance().answerSurvey(message, survey, true, SurveyActivity.this, new ZLServiceOperationCompleted<String>() {

								@Override
								public void operationCompleted(ZLServiceResponse<String> response) {
									if (response.errorMessage != null) {
										Toast.makeText(SurveyActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
									} else {
										ZLServices.getInstance().changeMessageStatus(message, ZLMessageType.ZLMessageTypeArchived, false, SurveyActivity.this, new ZLServiceOperationCompleted<Boolean>() {

											@Override
											public void operationCompleted(ZLServiceResponse<Boolean> response) {
												if (response.errorMessage == null)
													message.status = ZLMessageType.ZLMessageTypeRead;
											}
										});

										//check for trigger
										if (survey.trigger) {
											flComment.setVisibility(View.VISIBLE);
										} else {
											Toast.makeText(SurveyActivity.this, "Obrigado por participar da pesquisa do Zolkin.", Toast.LENGTH_LONG).show();
											finish();
										}
									}
								}
							});
						}
					});
					//lvSurvey.setAdapter(adapter);
					for (int i = 0; i < adapter.getCount(); i++)
					{
						View vr = adapter.getView(i,null,llSurvey);
						llSurvey.addView(vr);

					}
				}
			}
		});
		
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
	} //end onCreate
	
	
}
