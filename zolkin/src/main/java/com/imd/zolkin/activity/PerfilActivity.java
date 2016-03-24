package com.imd.zolkin.activity;

//Tela de Perfil (Dados Cadastrais)

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.imd.zolkin.custom.PerfilTriangleDrawable;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.TextFormattingUtils;
import com.imd.zolkin.util.Util;

import java.util.Calendar;

import br.com.zolkin.R;

public class PerfilActivity extends BaseZolkinMenuActivity
{
	FrameLayout btMenu = null;
	View vArrowUp = null;
	com.innovattic.font.FontEditText etNome = null;
	com.innovattic.font.FontEditText etSobrenome = null;
	com.innovattic.font.FontTextView etCPF = null;
	com.innovattic.font.FontEditText etCelular = null;
	com.innovattic.font.FontTextView tvDataNascimento = null;
	Button btMale = null;
	Button btFemale = null;
	com.innovattic.font.FontEditText etEmail = null;
	com.innovattic.font.FontEditText etSenha = null;
	com.innovattic.font.FontEditText etConfirmarSenha = null;
	com.innovattic.font.FontEditText etCEP = null;
	ImageView imgvBtConfirmar = null;
	
	boolean maleSelected = true;

	// private DatePicker datePicker;
	private Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perfil);

		btMenu = (FrameLayout) findViewById(R.id.btMenu);
		vArrowUp = (View) findViewById(R.id.vArrowUp);
		etNome = (com.innovattic.font.FontEditText) findViewById(R.id.etNome);
		etSobrenome = (com.innovattic.font.FontEditText) findViewById(R.id.etSobrenome);
		etCPF = (com.innovattic.font.FontTextView) findViewById(R.id.etCPF);
		etCelular = (com.innovattic.font.FontEditText) findViewById(R.id.etCelular);
		tvDataNascimento = (com.innovattic.font.FontTextView) findViewById(R.id.tvDataNascimento);
		btMale = (Button) findViewById(R.id.btMale);
		btFemale = (Button) findViewById(R.id.btFemale);
		etEmail = (com.innovattic.font.FontEditText) findViewById(R.id.etEmail);
		etSenha = (com.innovattic.font.FontEditText) findViewById(R.id.etSenha);
		etConfirmarSenha = (com.innovattic.font.FontEditText) findViewById(R.id.etConfirmarSenha);
		etCEP = (com.innovattic.font.FontEditText) findViewById(R.id.etCEP);
		imgvBtConfirmar = (ImageView) findViewById(R.id.imgvBtConfirmar);

		//mostra o retângulo no alto a direita do layout
		vArrowUp.setBackgroundDrawable(new PerfilTriangleDrawable());
		
//		if (Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.HONEYCOMB*/11)
//		{
//			vArrowUp.setBackground(new PerfilTriangleDrawable());
//		}
//		else
//		{
//			vArrowUp.setBackgroundDrawable(new PerfilTriangleDrawable());
//		}

		
		//pega o usuário logado
		ZLUser lu = ZLUser.getLoggedUser();
		
		//ao clicar em sexo
		OnClickListener toggle = new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				//muda o sexo do usuário
				maleSelected = !maleSelected;
				setSex(maleSelected);
			}
		};
		btMale.setOnClickListener(toggle);
		btFemale.setOnClickListener(toggle);

		
		//preenche o form com os dados do usuário
		etNome.setText(lu.name);
		etSobrenome.setText(lu.lastName);
		etCPF.setText(TextFormattingUtils.formatCpf(lu.document));
		if (lu.mobileNumber.equals("null"))
		{
			etCelular.setText("");
		}
		else
		{
			etCelular.setText(TextFormattingUtils.formatCelular( lu.mobileNumber ));
		}
		etEmail.setText(lu.email);
		etSenha.setText(lu.password);
		etConfirmarSenha.setText(lu.password);
		etCEP.setText(TextFormattingUtils.formatCep( lu.zipCode) );
		try
		{
			String year = lu.birthDate.substring(0, 4);
			String month = lu.birthDate.substring(5, 7);
			String day = lu.birthDate.substring(8, 10);
			tvDataNascimento.setText(day + "/" + month + "/" + year);
		}
		catch (Exception e)
		{
			tvDataNascimento.setText(lu.birthDate);
		}
		
		setSex(lu.sexo.toUpperCase().equals("M"));

		//ao clicar em data de nascimento
		calendar = Calendar.getInstance();
		tvDataNascimento.setOnClickListener(new OnClickListener()
		{

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v)
			{
				//mostra o picker de data
				showDialog(999);
			}
		});

		//ao clicar em atualizar o cadastro
		imgvBtConfirmar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// validate fields
				if (Util.stringIsNullEmptyOrWhiteSpace(etNome.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etSobrenome.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etCelular.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etEmail.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etSenha.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(etConfirmarSenha.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etCEP.getText().toString())
						|| Util.stringIsNullEmptyOrWhiteSpace(tvDataNascimento.getText().toString()))
				{
					Toast.makeText(PerfilActivity.this, "Atenção: todos os campos são obrigatorios.", Toast.LENGTH_LONG).show();
					return;
				}

				if (!etSenha.getText().toString().equals(etConfirmarSenha.getText().toString()))
				{
					Toast.makeText(PerfilActivity.this, "Atenção: as senhas não conferem.", Toast.LENGTH_LONG).show();
					return;
				}
				
				if (etSenha.getText().toString().length() != 4)
				{
					Toast.makeText(PerfilActivity.this, "Atenção: a senha deve ter exatamente 4 digitos numéricos.", Toast.LENGTH_LONG).show();
					return;
				}

				//preenche um objeto ZLUser com os dados atualizados
				ZLUser zlu = new ZLUser(null);
				zlu.name = etNome.getText().toString();
				zlu.lastName = etSobrenome.getText().toString();
				zlu.document = etCPF.getText().toString().replace(".", "").replace("-", "");
				zlu.mobileNumber = etCelular.getText().toString();
				zlu.email = etEmail.getText().toString();
				zlu.password = etSenha.getText().toString();
				zlu.zipCode = etCEP.getText().toString().replace(".", "").replace("-", "");
				zlu.birthDate = tvDataNascimento.getText().toString();
				zlu.sexo = maleSelected ? "M" : "F";

				//chama o serviço de atualizar perfil
				ZLServices.getInstance().updateUser(zlu, true, PerfilActivity.this, new ZLServiceOperationCompleted<String>()
				{
					@Override
					public void operationCompleted(ZLServiceResponse<String> response)
					{
						if (response.errorMessage != null)
						{
							Toast.makeText(PerfilActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						}
						else
						{
							//se atualizou, atualiza o usuário logado
							ZLUser lun = ZLUser.getLoggedUser();
							lun.name = etNome.getText().toString();
							lun.lastName = etSobrenome.getText().toString();
							lun.document = etCPF.getText().toString();
							lun.mobileNumber = etCelular.getText().toString();
							lun.email = etEmail.getText().toString();
							lun.password = etSenha.getText().toString();
							lun.zipCode = etCEP.getText().toString();
							lun.birthDate = tvDataNascimento.getText().toString();
							lun.sexo = maleSelected ? "M" : "F";
							
							
							new AlertDialog.Builder(PerfilActivity.this).setTitle("Sair").setMessage("Perfil atualizado com sucesso.")
							.setPositiveButton("Home", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									//voltar p home
									finish();
								}
							}).setNegativeButton("Cancelar", null).setIcon(android.R.drawable.ic_dialog_alert).show();
							//Toast.makeText(PerfilActivity.this, "Perfil atualizado com sucesso.", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		
		//adiciona as máscaras para os campos
		TextFormattingUtils.setCelularFormatter(etCelular);
		//TextFormattingUtils.setCpfFormatter(etCPF);
		TextFormattingUtils.setCepFormatter(etCEP);
	}
	
	//atualiza os botões de sexo de acordo com a seleção
	void setSex(boolean maleSelected)
	{
		if (maleSelected)
		{
			btMale.setBackgroundResource(R.drawable.segmented_left_selected);
			btMale.setTextColor(Color.argb(255, 255, 255, 255));
			btFemale.setBackgroundResource(R.drawable.segmented_right_unselected);
			btFemale.setTextColor(getResources().getColor(R.color.zl_purple));
		}
		else
		{
			btMale.setBackgroundResource(R.drawable.segmented_left_unselected);
			btMale.setTextColor(getResources().getColor(R.color.zl_purple));
			btFemale.setBackgroundResource(R.drawable.segmented_right_selected);
			btFemale.setTextColor(Color.argb(255, 255, 255, 255));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		//cria o picker de data para o aniversário
		if (id == 999)
		{
			String dateString = tvDataNascimento.getText().toString();
			int year = calendar.get(Calendar.YEAR) - 30;
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			
			if (dateString.length() >= 10)
			{
				day = Integer.parseInt( dateString.substring(0, 2) );
				//-1 porque o java conta Janeiro como mês 0!!!
				month = Integer.parseInt( dateString.substring(3, 5) ) - 1;
				year = Integer.parseInt( dateString.substring(6, 10) );
			}
			
			DatePickerDialog dpd = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, new OnDateSetListener()
			{

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
					//+1 porque o java conta Janeiro como mês 0!!!
					tvDataNascimento.setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear+1, year));

				}
			}, year, month, day);

			dpd.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
			
			return dpd;
		}
		return null;
	}
}
