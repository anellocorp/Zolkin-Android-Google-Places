package com.imd.zolkin.activity;

//Tela de esqueci senha

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;

import br.com.zolkin.R;

public class EsqueciSenhaActivity extends BaseZolkinActivity
{
	EditText etEmail = null;
	ImageView imgvBtEnviar = null;
	ImageView imgvBtCancelar = null;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_esqueci_senha);
		
		etEmail = (EditText) findViewById(R.id.etEmail);
		imgvBtEnviar = (ImageView) findViewById(R.id.imgvBtEnviar);
		imgvBtCancelar = (ImageView) findViewById(R.id.imgvBtCancelar);
		
		imgvBtCancelar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});
		
		//ao clicar em Enviar
		imgvBtEnviar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//valida o email (deve conter em alguma posição um @ e em outra posição mais adiante um .
				boolean atFound = false, dotFound = false;
				String email = etEmail.getText().toString();
				for (int i = 0; i < email.length(); i++)
				{
					if (!atFound)
					{
						if (email.charAt(i) == '@')
						{
							atFound = true;
						}
					}
					else if (!dotFound)
					{
						if (email.charAt(i) == '.')
						{
							dotFound = true;
						}
					}
					else
						break;
				}
				
				if (!dotFound || !atFound)
				{
					Toast.makeText(EsqueciSenhaActivity.this, "E-mail inválido. Por favor, verifique o email digitado e tente novamente.", Toast.LENGTH_LONG).show();
					return;
				}
				
				
				//chama o serviço de esqueci senha
				ZLServices.getInstance().forgotPassword(etEmail.getText().toString(), true, EsqueciSenhaActivity.this, new ZLServiceOperationCompleted<Boolean>()
				{
					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response)
					{
						if (response.errorMessage != null)
						{
							Toast.makeText(EsqueciSenhaActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						}
						else
						{
							Toast.makeText(EsqueciSenhaActivity.this, "Você receberá uma nova senha em breve em seu email.", Toast.LENGTH_LONG).show();
							finish();
						}
					}
				});
			}
		});
	}
}
