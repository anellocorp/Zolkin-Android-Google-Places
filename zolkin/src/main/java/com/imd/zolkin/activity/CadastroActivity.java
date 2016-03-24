package com.imd.zolkin.activity;

//Tela de cadastro do app

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.AppEventsConstants;
import com.facebook.model.GraphUser;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.TextFormattingUtils;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.util.Calendar;

import br.com.zolkin.R;

public class CadastroActivity extends BaseZolkinActivity
{
	View vArrowUp = null;
	com.innovattic.font.FontEditText etNome = null;
	com.innovattic.font.FontEditText etSobrenome = null;
	com.innovattic.font.FontEditText etCPF = null;
	com.innovattic.font.FontEditText etCelular = null;
	com.innovattic.font.FontTextView tvDataNascimento = null;
	Button btMale = null;
	Button btFemale = null;
	com.innovattic.font.FontEditText etEmail = null;
	com.innovattic.font.FontEditText etSenha = null;
	com.innovattic.font.FontEditText etConfirmarSenha = null;
	com.innovattic.font.FontEditText etCEP = null;
	View vBtCheckBox = null;
	com.innovattic.font.FontTextView tvBtTermos = null;
	ImageView imgvBtCadastrar = null;
	ImageView imgvBtCancelar = null;

	ScrollView registerFields = null;

	// private DatePicker datePicker;
	private Calendar calendar;

	public static GraphUser fbUser = null;
	public static String facebookToken = null;

	boolean checkBoxChecked;
	boolean maleSelected = true;
	boolean sexSelected = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro2);
		//setContentView(R.layout.activity_register_new_user);

		vArrowUp = (View) findViewById(R.id.vArrowUp);
		etNome = (com.innovattic.font.FontEditText) findViewById(R.id.etNome);
		etSobrenome = (com.innovattic.font.FontEditText) findViewById(R.id.etSobrenome);
		etCPF = (com.innovattic.font.FontEditText) findViewById(R.id.etCPF);
		etCelular = (com.innovattic.font.FontEditText) findViewById(R.id.etCelular);
		tvDataNascimento = (com.innovattic.font.FontTextView) findViewById(R.id.tvDataNascimento);
		btMale = (Button) findViewById(R.id.btMale);
		btFemale = (Button) findViewById(R.id.btFemale);
		etEmail = (com.innovattic.font.FontEditText) findViewById(R.id.etEmail);
		etSenha = (com.innovattic.font.FontEditText) findViewById(R.id.etSenha);
		etConfirmarSenha = (com.innovattic.font.FontEditText) findViewById(R.id.etConfirmarSenha);
		etCEP = (com.innovattic.font.FontEditText) findViewById(R.id.etCEP);
		vBtCheckBox = (View) findViewById(R.id.vBtCheckBox);
		tvBtTermos = (com.innovattic.font.FontTextView) findViewById(R.id.tvBtTermos);
		imgvBtCadastrar = (ImageView) findViewById(R.id.imgvBtCadastrar);
		imgvBtCancelar = (ImageView) findViewById(R.id.imgvBtCancelar);

		registerFields = (ScrollView) findViewById(R.id.RegisterFields);

		//AdWords Campain Tracker
		//Singin App
		AdWordsConversionReporter.reportWithConversionId(
				this.getApplicationContext(),
				"932901513",
				"hXGHCMOs2mIQieXrvAM",
				"0.00",
				true);

		//trata o selecionador do sexo
		OnClickListener toggle = new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				maleSelected = v == btMale;
				//maleSelected = !maleSelected;
				setSex(maleSelected);
			}
		};
		
		btMale.setOnClickListener(toggle);
		btFemale.setOnClickListener(toggle);
		setNoSex();
		//setSex(true);


        etNome.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				//código atalho secreto para preencher automaticamente a tela de cadastro. Usado para testes
				if (etNome.getText().toString().equals("o127")) {
					// fill data
					etNome.setText("Omar");
					etSobrenome.setText("Jardim");
					etCelular.setText("(11) 99896-3081");
					etEmail.setText("omar078@gmail.com");
					etSenha.setText("1111");
					etConfirmarSenha.setText("1111");
					etCEP.setText("01424-001");
					tvDataNascimento.setText("04/11/1978");

				}
				return false;
			}
		});

		checkBoxChecked = false;

		//mostra o triangulo no alto a direita do layout
		//vArrowUp.setBackgroundDrawable(new CadastroTriangleDrawable());
		
//		if (Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.HONEYCOMB*/11)
//		{
//			vArrowUp.setBackground(new CadastroTriangleDrawable());
//		}
//		else
//		{
//			vArrowUp.setBackgroundDrawable(new CadastroTriangleDrawable());
//		}

		
		//trata o CheckBox de aceitar os termos
		vBtCheckBox.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				checkBoxChecked = !checkBoxChecked;

				if (checkBoxChecked)
				{
					vBtCheckBox.setBackgroundResource(R.drawable.bt_cadastro_checked);
				}
				else
				{
					vBtCheckBox.setBackgroundResource(R.drawable.bt_cadastro_unchecked);
				}
			}
		});

		
		//mostra os termos
		tvBtTermos.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				startActivity(new Intent(CadastroActivity.this, TermosActivity.class));
			}
		});

		//ao cancelar, sai da tela
		imgvBtCancelar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});

		//faz o cadastro
		imgvBtCadastrar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				// validate fields
				if (Util.stringIsNullEmptyOrWhiteSpace(etNome.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etSobrenome.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etCPF.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etCelular.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etEmail.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etSenha.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etConfirmarSenha.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etCEP.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(tvDataNascimento.getText().toString()))
				{
					Toast.makeText(CadastroActivity.this, "Atenção: todos os campos são obrigatorios.", Toast.LENGTH_LONG).show();
					return;
				}

				//verifica se as duas senhas são iguais
				if (!etSenha.getText().toString().equals(etConfirmarSenha.getText().toString()))
				{
					Toast.makeText(CadastroActivity.this, "Atenção: as senhas não conferem.", Toast.LENGTH_LONG).show();
					return;
				}
				
				//verifica se forama aceitos os termos
				if (!checkBoxChecked)
				{
					Toast.makeText(CadastroActivity.this, "Atenção: você precisa aceitar os termos de uso.", Toast.LENGTH_LONG).show();
					return;
				}

				//valida o CPF
				if (!TextFormattingUtils.isCPF(etCPF.getText().toString()))
				{
					Toast.makeText(CadastroActivity.this, "Atenção: você precisa inserir um CPF válido.", Toast.LENGTH_LONG).show();
					return;
				}
				
				//verifica o comprimento da senha
				if (etSenha.getText().toString().length() != 4)
				{
					Toast.makeText(CadastroActivity.this, "Atenção: a senha deve ter exatamente 4 digitos numéricos.", Toast.LENGTH_LONG).show();
					return;
				}
				
				//valida o sexo
				if (!sexSelected)
				{
					Toast.makeText(CadastroActivity.this, "Atenção: você precisa selecionaro o sexo.", Toast.LENGTH_LONG).show();
					return;
				}

				// validação ok, faz o cadastro
				//salva os dados em um objeto ZLUser
				final ZLUser zlu = new ZLUser(null);
				zlu.name = etNome.getText().toString();
				zlu.lastName = etSobrenome.getText().toString();
				zlu.document = etCPF.getText().toString().replace(".", "").replace("-", "");
				zlu.mobileNumber = etCelular.getText().toString();
				zlu.email = etEmail.getText().toString();
				zlu.password = etSenha.getText().toString();
				zlu.zipCode = etCEP.getText().toString().replace(".", "").replace("-", "");
				zlu.birthDate = tvDataNascimento.getText().toString();
				zlu.facebookToken = facebookToken;
				zlu.sexo = maleSelected ? "M" : "F";
				
				//chama o serviço de cadastro
				ZLServices.getInstance().createUser(zlu, true, CadastroActivity.this, new ZLServiceOperationCompleted<String>()
				{
					@Override
					public void operationCompleted(ZLServiceResponse<String> response)
					{
						//se ocorreu un erro,
						if (response.errorMessage != null)
						{
							//mostra o erro
							Toast.makeText(CadastroActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						}
						else
						{
							//se o cadastro funcionou, mostra mensagem de parabéns
							BaseZolkinActivity.logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION);
							BaseZolkinActivity.logger.flush();
							new AlertDialog.Builder(CadastroActivity.this).setTitle("Parabéns!")
							.setMessage("Você acaba de ganhar 100 Zolkins e pode utilizá-los em nossos estabelecimentos por meio da digitação do seu CPF e senha no terminal Zolkin.")
							.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									//loga o cadastro no MixPanel
									try
									{
										MixpanelAPI mixPanel = MixpanelAPI.getInstance(CadastroActivity.this, Constants.MIXPANEL_TOKEN);

										JSONObject props = new JSONObject();
										props.put("Email", zlu.email);

										mixPanel.track("SignUp", props);

									}
									catch (Exception e)
									{

									}

									//Tenta fazer o login
									ZLServices.getInstance().login(etEmail.getText().toString(), etSenha.getText().toString(), facebookToken, true, CadastroActivity.this,
											new ZLServiceOperationCompleted<Boolean>()
											{

												@Override
												public void operationCompleted(ZLServiceResponse<Boolean> response)
												{
													if (response.errorMessage != null)
													{
														Toast.makeText(CadastroActivity.this, "Erro no login.", Toast.LENGTH_LONG).show();
													}
													else
													{
														//se logou, vai para a Home
														Intent intent = new Intent(CadastroActivity.this, HomeActivity.class);
														intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
														startActivity(intent);
														finish();
													}
												}
											});
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();

							

						}
					}
				});
			}
		});

		//se chegou aqui de um login via facebook, preenche os dados disponíveis
		if (fbUser != null)
		{
			etNome.setText(fbUser.getFirstName());
			etSobrenome.setText(fbUser.getLastName());
			tvDataNascimento.setText(fbUser.getBirthday());
			etEmail.setText(fbUser.asMap().get("email").toString());
		}

		//ao clicar "Next" no campo celular, mostra o picker de data
		etCelular.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				showDialog(999);
				return false;
			}
		});

		//ao clicar em Data de Nascimento, mostra o picker de data
		calendar = Calendar.getInstance();
		tvDataNascimento.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				showDialog(999);
			}
		});
	}
	
	void setNoSex()
	{
		sexSelected = false;
		btFemale.setBackgroundResource(R.drawable.segmented_right_unselected_green);
		btFemale.setTextColor(getResources().getColor(R.color.zl_purple));
		btMale.setBackgroundResource(R.drawable.segmented_left_unselected_green);
		btMale.setTextColor(getResources().getColor(R.color.zl_purple));
	}

	//escolhe o sexo no controle
	void setSex(boolean maleSelected)
	{
		sexSelected = true;
		if (maleSelected)
		{
			btMale.setBackgroundResource(R.drawable.segmented_left_selected_green);
			btMale.setTextColor(Color.argb(255, 255, 255, 255));
			btFemale.setBackgroundResource(R.drawable.segmented_right_unselected_green);
			btFemale.setTextColor(getResources().getColor(R.color.zl_purple));
		}
		else
		{
			btMale.setBackgroundResource(R.drawable.segmented_left_unselected_green);
			btMale.setTextColor(getResources().getColor(R.color.zl_purple));
			btFemale.setBackgroundResource(R.drawable.segmented_right_selected_green);
			btFemale.setTextColor(Color.argb(255, 255, 255, 255));
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();

        //adiciona formatação nos campos
        TextFormattingUtils.setCelularFormatter(etCelular);
		TextFormattingUtils.setCpfFormatter(etCPF);
		TextFormattingUtils.setCepFormatter(etCEP);
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		//Mostra o picker de data de nascimento
		if (id == 999)
		{
			DatePickerDialog dpd = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,new OnDateSetListener()
			{

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
					boolean invalid = false;
					if (year > calendar.get(Calendar.YEAR))
						invalid = true;
					else if (year == calendar.get(Calendar.YEAR))
					{
						if (monthOfYear > calendar.get(Calendar.MONTH))
							invalid = true;
						else if (monthOfYear == calendar.get(Calendar.MONTH))
						{
							if (dayOfMonth >= calendar.get(Calendar.DAY_OF_MONTH))
								invalid = true;
						}
					}
					if (invalid)
					{
						Toast.makeText(CadastroActivity.this, "A sua data de nascimento não pode ser no futuro.", Toast.LENGTH_LONG).show();
						return;
					}
					tvDataNascimento.setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year));

				}
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
			
			
			
			//dpd.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
			
			return dpd;
		}
		return null;
	}
}
