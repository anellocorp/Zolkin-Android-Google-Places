package com.imd.zolkin.activity;

//Tela de login

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.internal.Logger;
import com.facebook.model.GraphUser;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import br.com.zolkin.R;

public class LoginActivity extends BaseZolkinActivity
{
	ImageView imgvBtFacebook = null;
	com.innovattic.font.FontEditText etEmail = null;
	com.innovattic.font.FontEditText etPass = null;
	TextView tvBtEsqueciSenha = null;
	ImageView imgvBtCadastrar = null;
	TextView tvBtAcessarSemConta = null;

	// facebook
	Session mSession;

	boolean firstTime = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);


		imgvBtFacebook = (ImageView) findViewById(R.id.imgvBtFacebook);
		etEmail = (com.innovattic.font.FontEditText) findViewById(R.id.etEmail);
		etPass = (com.innovattic.font.FontEditText) findViewById(R.id.etPass);
		tvBtEsqueciSenha = (TextView) findViewById(R.id.tvBtEsqueciSenha);
		imgvBtCadastrar = (ImageView) findViewById(R.id.imgvBtCadastrar);
		tvBtAcessarSemConta = (TextView) findViewById(R.id.tvBtAcessarSemConta);

		//AdWords Campain Tracker
		//Lead
		AdWordsConversionReporter.reportWithConversionId(
				this.getApplicationContext(),
				"932901513",
				"bc72CMuu2mIQieXrvAM",
				"0.00",
				true);

		// para debugging, loga o hash da chave do pacote.
		try
		{
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures)
			{
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

			}
		}
		catch (NameNotFoundException e)
		{

		}
		catch (NoSuchAlgorithmException e)
		{

		}

		// mostra a tela de cadastrar
		imgvBtCadastrar.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
			}
		});

		// vai para a tela de esqueci senha
		tvBtEsqueciSenha.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				startActivity(new Intent(LoginActivity.this, EsqueciSenhaActivity.class));
			}
		});

		// vai para a home sem logar
		tvBtAcessarSemConta.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(homeIntent);
				finish();
			}
		});

		// ao clicar em login pelo facebook
		imgvBtFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0)
			{
				// cria a callback de abertura da sessão do facebook (login)
				StatusCallback callback = new StatusCallback()
				{
					@Override
					public void call(Session session, SessionState state, Exception exception)
					{
						// se a sessão foi aberta
						if (session.isOpened())
						{
							// se mudou algo na sessão
							if (mSession == null || isSessionChanged(session))
							{
								// salva a nova sessão
								mSession = session;
							}
							else
							{
								// não faz nada
								return;
							}

							// pega o token
							final String accessToken = session.getAccessToken();

							// pede os dados do usuário (email, aniversário etc)
							Request r = Request.newMeRequest(session, new Request.GraphUserCallback()
							{

								@Override
								public void onCompleted(final GraphUser user, Response response)
								{
									//se recebemos os dados
									if (user != null)
									{
										//se não veio o email
										if (user.asMap().get("email") == null)
										{

											//mostra o alerta de que precisa de email para o login
											new AlertDialog.Builder(LoginActivity.this)
													.setTitle("Atenção")
													.setMessage(
															"O e-mail da sua conta Facebook não esta acessível, e portanto não é possível fazer login pelo Facebook. Por favor, faça o cadastro e crie uma conta normalmente.")
													.setPositiveButton("OK", new DialogInterface.OnClickListener()
													{

														@Override
														public void onClick(DialogInterface dialog, int which)
														{
															ZLUser.logout();
															Intent i = new Intent(LoginActivity.this, LoginActivity.class);
															i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
															startActivity(i);
															finish();
														}
													}).setIcon(android.R.drawable.ic_dialog_alert).show();
											return;
										}

										// tenta logar com esse user, se não
										// tiver vai pro cadastro
										ZLServices.getInstance().login(user.asMap().get("email").toString(), null, accessToken, true, LoginActivity.this, new ZLServiceOperationCompleted<Boolean>()
										{
											@Override
											public void operationCompleted(ZLServiceResponse<Boolean> response)
											{
												if (response.errorMessage != null)
												{
													//este user não existia, enviar para a tela de cadastro
													CadastroActivity.facebookToken = accessToken;
													CadastroActivity.fbUser = user;
													startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
												}
												else
												{
													//logou, ir direto para a home
													Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
													homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
													startActivity(homeIntent);
													finish();
												}
											}
										});

									}
								}
							});
							r.executeAsync();
						}
						else if (session.isClosed())
						{

						}
						else if (exception != null)
						{

						}
					}
				};

				//pede as permissões de email e aniversário
				List<String> permissions = new ArrayList<String>();
				permissions.add("email");
				permissions.add("user_birthday");

				//tenta abrir a sessão do Facebook
				Session.OpenRequest openRequest = new Session.OpenRequest(LoginActivity.this).setPermissions(permissions).setCallback(callback);
				Session session = new Session.Builder(LoginActivity.this).build();
				if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || true)
				{

					Session.setActiveSession(session);
					session.openForRead(openRequest);

				}
			}
		});

		//configura o teclado para os dois campos de texto
		//etEmail.setImeActionLabel("", KeyEvent.KEYCODE_ENTER);
		//etPass.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);

		//ao clicar next no email
		etEmail.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2)
			{
				//vai para a senha
				etPass.requestFocus();
				return false;
			}
		});

		etPass.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event)
			{
				//cada aparelho trata isso de um jeito.
				/*
				 * if (event == null) return true; if (event.getAction() !=
				 * KeyEvent.ACTION_DOWN) return true;
				 * 
				 * if (arg0 == etEmail) return false;
				 * 
				 * if (firstTime &&
				 * Util.stringIsNullEmptyOrWhiteSpace(etPass.getText
				 * ().toString())) { firstTime = false; return true; }
				 * 
				 * firstTime = false;
				 */

				//hacks vários para evitar que faça o login antecipado
				//BUG: em alguns aparelhos, ele perde o foco do teclado sozinho
				if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT)
				{
					Log.d("Zolkin", "ActionID = " + actionId);
					return false;
				}

				//se não digitou nada, avisar e não fazer o login
				if (Util.stringIsNullEmptyOrWhiteSpace(etEmail.getText().toString()) || Util.stringIsNullEmptyOrWhiteSpace(etPass.getText().toString()))
				{
					Toast.makeText(LoginActivity.this, "Por favor, preencher email e senha para fazer o login", Toast.LENGTH_LONG).show();
					return false;
				}


				//chama o serviço de login
				ZLServices.getInstance().login(etEmail.getText().toString(), etPass.getText().toString(), null, true, LoginActivity.this, new ZLServiceOperationCompleted<Boolean>()
				{
					@Override
					public void operationCompleted(ZLServiceResponse<Boolean> response)
					{
						if (response.errorMessage != null)
						{
							Toast.makeText(LoginActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
							homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(homeIntent);
							finish();
						}
					}
				});
				return false;
			}
		});

		// se primeira vez, mostrar o tutorial
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (!prefs.contains("show_tutorial"))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("show_tutorial", "show_tutorial");
			editor.apply();
			startActivity(new Intent(this, ComoFuncionaActivity.class));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		//trata login pelo facebook
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	private boolean isSessionChanged(Session session)
	{
		//Facebook
		// Check if session state changed
		if (mSession.getState() != session.getState())
			return true;

		// Check if accessToken changed
		if (mSession.getAccessToken() != null)
		{
			if (!mSession.getAccessToken().equals(session.getAccessToken()))
				return true;
		}
		else if (session.getAccessToken() != null)
		{
			return true;
		}

		// Nothing changed
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}