package com.imd.zolkin.activity;

/*
Todas as activities que possuem menu lateral herdam desta.
Todas as funções do menu lateral estão implementadas aqui.
*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.imd.zolkin.custom.CircleDrawable;
import com.imd.zolkin.model.ZLPendingAction;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.text.DecimalFormat;

import br.com.zolkin.R;

public class BaseZolkinMenuActivity extends BaseZolkinActivity
{
	protected android.support.v4.widget.DrawerLayout drawerLayout = null;
	FrameLayout main_frame = null;
	FrameLayout flMenuContent = null;
	ImageView btCloseMenu = null;
	TextView tvUserName = null;
	TextView tvSaldo = null;
	TextView tvMenuPerfil = null;
	TextView tvNewMessages = null;
	LinearLayout llBtHome = null;
	LinearLayout llBtPerfil = null;
	LinearLayout llBtExtrato = null;
	LinearLayout llBtMensagens = null;
	LinearLayout llBtFavoritos = null;
	LinearLayout llBtConvidarAmigos = null;
	LinearLayout llBtFaleConosco = null;
	LinearLayout llBtFaq = null;
	LinearLayout llBtComoFunciona = null;
	LinearLayout llBtTermos = null;
	LinearLayout llBtAbout = null;
	LinearLayout llBtLogout = null;
	//private LinearLayout llBtCompartilhar;

	private UiLifecycleHelper uiHelper;
	private ScrollView svMenuContent;
	
	public static ZLPendingAction pendingAction = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);
	}

	//Pega o layout da Activity que esta sendo mostrada, e insere dentro do Layout com o menu lateral.
	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(R.layout.activity_menu_base);

		FrameLayout flContentFrame = (FrameLayout) findViewById(R.id.main_frame);

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		//carrega o conteudo
		View contentView = inflater.inflate(layoutResID, flContentFrame, false);
		//insere dentro desta activity com o menu lateral
		flContentFrame.addView(contentView);

		//pega a referencia para o menu lateral
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

		
		//se a view da Activty interna tem elementos com ID btMenu ou btClose, eles são tratados automaticamente por esta activity.
		//o btMenu faz abrir o menu lateral, e o btClose faz fechar
		View vOpen = contentView.findViewById(R.id.btMenu);

		View vClose = findViewById(R.id.btCloseMenu);

		if (vOpen != null)
		{
			vOpen.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					drawerLayout.openDrawer(Gravity.LEFT);
				}
			});
		}

		if (vClose != null)
		{
			vClose.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					drawerLayout.closeDrawer(Gravity.LEFT);
				}
			});
		}

		drawerLayout = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawerLayout);
		svMenuContent = (ScrollView) findViewById(R.id.svMenuContent);
		main_frame = (FrameLayout) findViewById(R.id.main_frame);
		btCloseMenu = (ImageView) findViewById(R.id.btCloseMenu);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvSaldo = (TextView) findViewById(R.id.tvSaldo);
		tvMenuPerfil = (TextView) findViewById(R.id.tvMenuPerfil);
		tvNewMessages = (TextView) findViewById(R.id.tvNewMessages);
		llBtHome = (LinearLayout) findViewById(R.id.llBtHome);
		llBtPerfil = (LinearLayout) findViewById(R.id.llBtPerfil);
		llBtExtrato = (LinearLayout) findViewById(R.id.llBtExtrato);
		llBtMensagens = (LinearLayout) findViewById(R.id.llBtMensagens);
		llBtFavoritos = (LinearLayout) findViewById(R.id.llBtFavoritos);
		llBtConvidarAmigos = (LinearLayout) findViewById(R.id.llBtConvidarAmigos);
		//llBtCompartilhar = (LinearLayout) findViewById(R.id.llBtCompartilhar);
		llBtFaleConosco = (LinearLayout) findViewById(R.id.llBtFaleConosco);
		llBtFaq = (LinearLayout) findViewById(R.id.llBtFaq);
		llBtComoFunciona = (LinearLayout) findViewById(R.id.llBtComoFunciona);
		llBtTermos = (LinearLayout) findViewById(R.id.llBtTermos);
		llBtAbout = (LinearLayout) findViewById(R.id.llBtAbout);
		llBtLogout = (LinearLayout) findViewById(R.id.llBtLogout);
		
		tvNewMessages.setBackground(new CircleDrawable(Color.argb(255, 255, 0, 0), 1));
		
		flMenuContent = (FrameLayout) findViewById(R.id.flMenuContent);
		flMenuContent.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//prevent touches from passing through
				return true;
			}
		});

		drawerLayout.setDrawerListener(new DrawerListener()
		{
			
			@Override
			public void onDrawerStateChanged(int arg0)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDrawerSlide(View arg0, float arg1)
			{
				if (arg1 >= 0 && arg1 < 0.2)
				{
					svMenuContent.scrollTo(0, 0);
				}
			}
			
			@Override
			public void onDrawerOpened(View arg0)
			{
				
			}
			
			@Override
			public void onDrawerClosed(View arg0)
			{
				// TODO Auto-generated method stub
				
			}
		});
		
		//atualiza os dados do usuário visiveis no menu lateral, como nome e saldo.
		updateUserInfo();

		//mostra a home
		llBtHome.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//if (BaseZolkinMenuActivity.this.getClass() != HomeActivity.class)
				//{
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					mixPanel.track("MenuHome", props);
					Intent homeIntent = new Intent(BaseZolkinMenuActivity.this, HomeActivity.class);
					homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(homeIntent);
					finish();
				//}
			}
		});
		

		llBtPerfil.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//se o usuário esta logado
				if (ZLUser.getLoggedUser().isAuthenticated())
				{
					//mostra o perfil
					if (BaseZolkinMenuActivity.this.getClass() != PerfilActivity.class)
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
						JSONObject props = new JSONObject();
						mixPanel.track("MenuPerfil", props);
						startActivity(new Intent(BaseZolkinMenuActivity.this, PerfilActivity.class));
						//finish();
					}
					else
					{
						startActivity(new Intent(BaseZolkinMenuActivity.this, PerfilActivity.class));
						finish();
					}
				}
				else
				{
					//se não esta logado, mostra o login
					startActivity(new Intent(BaseZolkinMenuActivity.this, CadastroActivity.class));
					finish();
				}
			}
		});
		llBtExtrato.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//se o usuário esta logado
				if (ZLUser.getLoggedUser().isAuthenticated())
				{
					if (BaseZolkinMenuActivity.this.getClass() != ExtratoActivity.class)
					{
						//mostra o Extrato
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuExtrato", props);
						}
						catch (Exception e)
						{

						}
						startActivity(new Intent(BaseZolkinMenuActivity.this, ExtratoActivity.class));
						finish();
					}
					else
					{
						startActivity(new Intent(BaseZolkinMenuActivity.this, ExtratoActivity.class));
						finish();
					}
				}
				else
				{
					//se não esta logado, mostra o login
					startActivity(new Intent(BaseZolkinMenuActivity.this, LoginActivity.class));
					finish();
				}
			}
		});
		llBtMensagens.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//se o usuário esta logado
				if (ZLUser.getLoggedUser().isAuthenticated())
				{
					if (BaseZolkinMenuActivity.this.getClass() != MessagesActivity.class)
					{
						//mostra o Extrato
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuMensagens", props);
						}
						catch (Exception e)
						{

						}
						startActivity(new Intent(BaseZolkinMenuActivity.this, MessagesActivity.class));
						//finish();
					}
					else
					{
						startActivity(new Intent(BaseZolkinMenuActivity.this, MessagesActivity.class));
						finish();
					}
				}
				else
				{
					//se não esta logado, mostra o login
					startActivity(new Intent(BaseZolkinMenuActivity.this, LoginActivity.class));
					finish();
				}
			}
		});
		llBtFavoritos.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//se o usuário esta logado
				if (ZLUser.getLoggedUser().isAuthenticated())
				{
					//mostra os favoritos
					if (BaseZolkinMenuActivity.this.getClass() != FavoritosActivity.class)
					{
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuFavoritos", props);
						}
						catch (Exception e)
						{

						}
						startActivity(new Intent(BaseZolkinMenuActivity.this, FavoritosActivity.class));
						//finish();
					}
					else
					{
						startActivity(new Intent(BaseZolkinMenuActivity.this, FavoritosActivity.class));
						finish();
					}
				}
				else
				{
					//se não esta logado, mostra o login
					startActivity(new Intent(BaseZolkinMenuActivity.this, LoginActivity.class));
					finish();
				}
			}
		});
		
		//convidar amigos
		llBtConvidarAmigos.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				//mostra um alerta com as opções de convidar, SMS, Email ou Facebook
				new AlertDialog.Builder(BaseZolkinMenuActivity.this).setTitle("Convidar").setMessage("Convidar amigos via:").setPositiveButton("SMS", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						//manda SMS
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuConvidouSMS", props);
						}
						catch (Exception e)
						{

						}
						Intent sendIntent = new Intent(Intent.ACTION_VIEW);
						sendIntent.setData(Uri.parse("sms:"));
						sendIntent.putExtra("sms_body", "Baixe já o Zolkin, o app inovador que aumenta seu poder de compra. Com Zolkin você economiza até 50% em lojas, restaurantes e cinemas! Aqui no http://www.zolkin.com.br");
						startActivity(sendIntent);
					}
				}).setNeutralButton("E-mail", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						//manda e-mail
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuConvidouEmail", props);
						}
						catch (Exception e)
						{

						}
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("message/rfc822");
						intent.putExtra(Intent.EXTRA_EMAIL, "contato@zolkin.com.br");
						intent.putExtra(Intent.EXTRA_SUBJECT, "Zolkin");
						intent.putExtra(Intent.EXTRA_TEXT, "Baixe já o Zolkin, o app inovador que aumenta seu poder de compra. Com Zolkin você economiza até 50% em lojas, restaurantes e cinemas! Aqui no http://www.zolkin.com.br");

						startActivity(Intent.createChooser(intent, "Enviar Email"));
					}
				}).setNegativeButton("Facebook", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						//convida pelo facebook
						try
						{
							MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							mixPanel.track("MenuConvidouFacebook", props);
						}
						catch (Exception e)
						{

						}
						
						Bundle params = new Bundle();
						params.putString("message", "Baixe já o Zolkin, o app inovador que aumenta seu poder de compra. Com Zolkin você economiza até 50% em lojas, restaurantes e cinemas! Aqui no http://www.zolkin.com.br");

						//dispara o convite
						WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(BaseZolkinMenuActivity.this,  BaseZolkinMenuActivity.this.getResources().getString(R.string.app_id_0), params)).setOnCompleteListener(
								new OnCompleteListener()
								{

									@Override
									public void onComplete(Bundle values, FacebookException error)
									{
										//mostra feedback sobre o convite
										if (error != null)
										{
											if (error instanceof FacebookOperationCanceledException)
											{
												Toast.makeText(BaseZolkinMenuActivity.this.getApplicationContext(), "Convite cancelado", Toast.LENGTH_SHORT).show();
											}
											else
											{
												Toast.makeText(BaseZolkinMenuActivity.this.getApplicationContext(), "Erro na rede", Toast.LENGTH_SHORT).show();
											}
										}
										else
										{
											final String requestId = values.getString("request");
											if (requestId != null)
											{
												Toast.makeText(BaseZolkinMenuActivity.this.getApplicationContext(), "Convite enviado", Toast.LENGTH_SHORT).show();
											}
											else
											{
												Toast.makeText(BaseZolkinMenuActivity.this.getApplicationContext(), "Convite cancelado", Toast.LENGTH_SHORT).show();
											}
										}
									}

								}).build();
						requestsDialog.show();

					}
				}).setIcon(android.R.drawable.ic_dialog_alert).show();
			}
		});
		
		//compartilha nas redes socias, usando o Intent padrão do Android
//		llBtCompartilhar.setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				try
//				{
//					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
//					JSONObject props = new JSONObject();
//					mixPanel.track("MenuCompartilhou", props);
//				}
//				catch (Exception e)
//				{
//
//				}
//				Intent sendIntent = new Intent();
//				sendIntent.setAction(Intent.ACTION_SEND);
//				sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.zolkin.com.br/mobile/cadastro.html");
//				sendIntent.setType("text/plain");
//				startActivity(Intent.createChooser(sendIntent, "Compartilhar"));
//
//			}
//		});
		
		//envia email para o Zolkin
		llBtFaleConosco.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				try
				{
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					mixPanel.track("MenuFaleConosco", props);
				}
				catch (Exception e)
				{

				}
				
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","contato@zolkin.com.br", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Fale Conosco");
				startActivity(Intent.createChooser(emailIntent, "Enviar Email"));

				
			}
		});
		
		//mostra a tela de FAQ
		llBtFaq.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if (BaseZolkinMenuActivity.this.getClass() != FAQActivity.class)
				{
					try
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
						JSONObject props = new JSONObject();
						mixPanel.track("MenuFAQ", props);
					}
					catch (Exception e)
					{

					}
					startActivity(new Intent(BaseZolkinMenuActivity.this, FAQActivity.class));
				}
			}
		});
		
		//mostra a tela de Tutorial
		llBtComoFunciona.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				try
				{
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					mixPanel.track("MenuComoFunciona", props);
				}
				catch (Exception e)
				{

				}
				startActivity(new Intent(BaseZolkinMenuActivity.this, ComoFuncionaActivity.class));
			}
		});
		
		//mostra a tela com os Termos de Serviço
		llBtTermos.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				try
				{
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					mixPanel.track("MenuTermos", props);
				}
				catch (Exception e)
				{

				}
				startActivity(new Intent(BaseZolkinMenuActivity.this, TermosActivity.class));
				drawerLayout.closeDrawer(Gravity.LEFT);
			}
		});
		
		llBtAbout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				try
				{
					MixpanelAPI mixPanel = MixpanelAPI.getInstance(BaseZolkinMenuActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					mixPanel.track("MenuSobre", props);
				}
				catch (Exception e)
				{

				}
				startActivity(new Intent(BaseZolkinMenuActivity.this, AboutActivity.class));
				drawerLayout.closeDrawer(Gravity.LEFT);
			}
		});
		
		//se o usuário não esta logado
		if (!ZLUser.getLoggedUser().isAuthenticated())
		{
			//esconde o item de "Sair" do menu
			llBtLogout.setVisibility(View.GONE);
		}
		else
		{
			//se esta logado, mostra o item de sair
			llBtLogout.setVisibility(View.VISIBLE);
			llBtLogout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//confirma se quer sair
					new AlertDialog.Builder(BaseZolkinMenuActivity.this).setTitle("Atenção").setMessage("Deseja mesmo desconectar a sua conta?")
							.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									//faz o logout no app
									ZLUser.logout();
									stopService(new Intent(BaseZolkinMenuActivity.this, ZLLocationService.class));
									Intent i = new Intent(BaseZolkinMenuActivity.this, LoginActivity.class);
									i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(i);
									finish();
								}
							})
							.setNegativeButton("Não", null).setIcon(android.R.drawable.ic_dialog_alert).show();


				}
			});

		}
	}

	protected void updateUserInfo()
	{
		//atualiza os dados do usuário
		ZLUser u = ZLUser.getLoggedUser();
		DecimalFormat df = new DecimalFormat("0.00");
		if (u.isAuthenticated())
		{
			tvUserName.setText(u.name + " " + u.lastName);
			tvSaldo.setText(df.format( u.kinBalance ));
		}
		else
		{
			tvUserName.setText("Convidado");
			tvSaldo.setText(df.format(0.0));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback()
		{
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data)
			{
				Log.e("Activity", String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data)
			{
				Log.i("Activity", "Success!");
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		uiHelper.onResume();
		
		//se esta logado
		if (ZLUser.getLoggedUser().isAuthenticated())
		{
			//load user data
			ZLServices.getInstance().show(false, this, new ZLServiceOperationCompleted<Boolean>()
			{
				
				@Override
				public void operationCompleted(ZLServiceResponse<Boolean> response)
				{
					//update menu info
					updateUserInfo();
				}
			});
			tvMenuPerfil.setText("Dados Cadastrais");
			
			llBtMensagens.setVisibility(View.VISIBLE);
			
			ZLServices.getInstance().getNumUnreadMessages(false, this, new ZLServiceOperationCompleted<Integer>()
			{
				
				@Override
				public void operationCompleted(ZLServiceResponse<Integer> response)
				{
					if (response.errorMessage == null)
					{
						int n = response.serviceResponse.intValue();
						tvNewMessages.setText(""+n);
						tvNewMessages.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
					}
					else
					{
						tvNewMessages.setVisibility(View.VISIBLE);
					}
				}
			});
			
			
		}
		else
		{
			llBtMensagens.setVisibility(View.GONE);
			tvMenuPerfil.setText("Cadastre-se");
			updateUserInfo();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		uiHelper.onPause();


	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		uiHelper.onDestroy();

	}
}
