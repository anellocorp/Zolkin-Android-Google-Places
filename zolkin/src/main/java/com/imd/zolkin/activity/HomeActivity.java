package com.imd.zolkin.activity;

//Tela de Home do app

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.AppEventsConstants;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.imd.zolkin.adapter.HomeCachedTermsAdapter;
import com.imd.zolkin.adapter.HomeCategoriesAdapter;
import com.imd.zolkin.adapter.PlacesAutocompleteAdapter;
import com.imd.zolkin.custom.CircleDrawable;
import com.imd.zolkin.custom.ZolkinApplication;
import com.imd.zolkin.model.ZLCategory;
import com.imd.zolkin.model.ZLCity;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLListCache;
import com.imd.zolkin.services.ZLListCache.ZLTerm;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.GCMUtils;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.zolkin.R;

import static com.imd.zolkin.services.ZLLocationService.getLatitude;
import static com.imd.zolkin.services.ZLLocationService.getLongitude;
import static com.imd.zolkin.services.ZLLocationService.isCustomAddressSet;
import static com.imd.zolkin.services.ZLLocationService.setCustomAddress;

/*
 * Implementação do Places API Android
 */

import com.google.android.gms.location.places.Place;


public class HomeActivity extends BaseZolkinMenuActivity implements GoogleApiClient.OnConnectionFailedListener
{
	FrameLayout btMenu = null, flCachedTermsBorder = null;
	ImageView btLocation = null;
	ImageView imgvBtHideCoach = null, btClearText = null;
	EditText etBusca = null;
	ListView lvCategories = null;
	ListView lvCachedTerms = null;
	View root = null;
	ImageView btSpeech = null;
	TextView tvNewMessagesHome = null;

	GoogleApiClient mGoogleApiClient;
	AutoCompleteTextView mAutocompleteView;
	PlacesAutocompleteAdapter mAdapter;
	AutocompleteFilter mFilter;
	private static final String TAG = HomeActivity.class.getSimpleName();

	static boolean isCheckLocation = false;

	List<ZLCategory> categories;

	HomeCategoriesAdapter adapter = null;
	HomeCachedTermsAdapter termsAdapter = null;

	private static final int REQUEST_CODE = 123455;
	TextView Speech;
	Dialog match_text_dialog;
	ListView textlist;
	ArrayList<String> matches_text;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		btMenu = (FrameLayout) findViewById(R.id.btMenu);
		btLocation = (ImageView) findViewById(R.id.btLocation);
		imgvBtHideCoach = (ImageView) findViewById(R.id.imgvBtHideCoach);
		btClearText = (ImageView) findViewById(R.id.btClearText);
		etBusca = (EditText) findViewById(R.id.etBusca);
		lvCategories = (ListView) findViewById(R.id.lvCategories);
		lvCachedTerms = (ListView) findViewById(R.id.lvCachedTerms);
		flCachedTermsBorder = (FrameLayout) findViewById(R.id.flCachedTermsBorder);
		btSpeech = (ImageView) findViewById(R.id.btSpeech);
		tvNewMessagesHome = (TextView) findViewById(R.id.tvNewMessagesHome);

		tvNewMessagesHome.setBackground(new CircleDrawable(Color.argb(255, 255, 0, 0), 1));
		tvNewMessagesHome.setVisibility(View.GONE);


		// Analytics Tracker
		Tracker t = ((ZolkinApplication) getApplication()).getDefaultTracker();
		t.send(new HitBuilders.EventBuilder()
				.setCategory("App Lauched")
				.setAction("Abriu o app")
				.setLabel("Zolkin")
				.build());

		//AdWords Campain Tracker
		// App Lauched
		AdWordsConversionReporter.reportWithConversionId(
				this.getApplicationContext(),
				"932901513",
				"i3zZCPWo2mIQieXrvAM",
				"0.00",
				false);

		//Places API
		mFilter = new AutocompleteFilter.Builder()
				.setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, 0, this)
				.addApi(Places.GEO_DATA_API)
				.build();

		mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);
		mAdapter = new PlacesAutocompleteAdapter(this, mGoogleApiClient, null, mFilter);
		mAutocompleteView.setAdapter(mAdapter);
		mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

		etBusca.setText("");

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		btClearText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etBusca.setText("");
				mAutocompleteView.setText("");
			}
		});


		btSpeech.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if(isConnected())
				{
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					//intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); //pt-BR
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
					startActivityForResult(intent, REQUEST_CODE);

				}

				else
				{
					Toast.makeText(getApplicationContext(), "Atenção: você precisa estar conectado/a à Internet para usar o reconhecimento de voz.", Toast.LENGTH_LONG).show();

				}
			}
		});


		// hack para detectar se o teclado esta visível ou não
		// isto serve para saber se devo ou não mostrar a lista de pesquisas
		// recentes
		drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				Rect r = new Rect();
				// r will be populated with the coordinates of your view that
				// area still visible.
				drawerLayout.getWindowVisibleDisplayFrame(r);

				int heightDiff = drawerLayout.getRootView().getHeight() - (r.bottom - r.top);
				if (heightDiff > 100)
				{ // if more than 100 pixels, its probably a keyboard...
					// Keyboard is shown
					btClearText.setVisibility(View.VISIBLE);
					btSpeech.setVisibility(View.GONE);
				}
				if (heightDiff <= 100)
				{
					// Keyboard not shown

					//esconde a lista de termos recentes pesquisados
					flCachedTermsBorder.setVisibility(View.GONE);
					btClearText.setVisibility(View.GONE);
					btSpeech.setVisibility(View.VISIBLE);
				}
			}
		});

		//detecta quando mudou o texto do campo de busca
		etBusca.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				//se o texto mudou, mas não tem nenhum carácter digitado, esconde a lista de pesquisas recentes
				if (etBusca.getText().toString().length() == 0)
				{
					flCachedTermsBorder.setVisibility(View.GONE);
					btClearText.setVisibility(View.GONE);
					btSpeech.setVisibility(View.VISIBLE);
				}
				else
				{
					btClearText.setVisibility(View.VISIBLE);
					btSpeech.setVisibility(View.GONE);

					//se tem pelo menos um carácter, mostra a lista de pesquisas recentes
					flCachedTermsBorder.setVisibility(View.VISIBLE);
					//cria o adapter
					termsAdapter = new HomeCachedTermsAdapter(HomeActivity.this);
					//mostra na lista
					lvCachedTerms.setAdapter(termsAdapter);
					//quando clicar um termo na lista
					lvCachedTerms.setOnItemClickListener(new OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
						{
							//esconde a lista
							flCachedTermsBorder.setVisibility(View.GONE);
							//pega o termo escolhido
							ZLTerm t = termsAdapter.getItem(pos);
							//esconde o teclado
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(etBusca.getWindowToken(), 0);

							try
							{
								//loga cada palavra buscada no mixpanel
								MixpanelAPI mixPanel = MixpanelAPI.getInstance(HomeActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("Termo", etBusca.getText().toString());
								mixPanel.track("HomeBusca", props);

								String[] words = etBusca.getText().toString().split(" ");

								for (int i = 0; i < words.length; i++)
								{
									String word = words[i];
									JSONObject props2 = new JSONObject();
									props2.put("Palavra", word);
									mixPanel.track("HomeBuscaPalavra", props2);
								}

							}
							catch (Exception e)
							{

							}

							//chama o serviço de lista de estabelecimentos com o termo buscado
							ZLServices.getInstance().listCovenants("" + getLatitude(), "" + getLongitude(), false, 1000, 0,
									ZLCity.getCurrentCity(HomeActivity.this).id, false, null, null, null, t.term, true, HomeActivity.this, new ZLServiceOperationCompleted<List<ZLStore>>()
									{

										@Override
										public void operationCompleted(ZLServiceResponse<List<ZLStore>> response)
										{
											if (response.errorMessage != null)
											{
												Toast.makeText(HomeActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
											}
											else
											{
												//mostra a tela de lista, passando a lista recebida do serviço
												Bundle bundle = new Bundle();
												bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, etBusca.getText().toString());
												bundle.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, etBusca.getText().toString());
												BaseZolkinActivity.logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, bundle);

												ListaActivity.list = response.serviceResponse;
												ListaActivity.category = null;
												ListaActivity.needReload = false;
												startActivity(new Intent(HomeActivity.this, ListaActivity.class));

											}

										}
									});
						}
					});
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s)
			{
				// TODO Auto-generated method stub

			}
		});

		//usado para mostrar e esconder a lista de pesquisas recentes
		etBusca.setOnFocusChangeListener(new OnFocusChangeListener()
		{

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{

				//se o campo não tem foco, esconde a lista
				if (/* Util.CheckConnection(HomeActivity.this) || */!hasFocus)
				{
					flCachedTermsBorder.setVisibility(View.GONE);
					btClearText.setVisibility(View.GONE);
					btSpeech.setVisibility(View.VISIBLE);
				}
				else
				{
					btClearText.setVisibility(View.VISIBLE);
					btSpeech.setVisibility(View.GONE);
					//se o campo acaba de ganhar foco, mostra a lista de pesquisas recentes
					flCachedTermsBorder.setVisibility(View.VISIBLE);
					termsAdapter = new HomeCachedTermsAdapter(HomeActivity.this);
					lvCachedTerms.setAdapter(termsAdapter);
					//ao segurar apertado um item da lista
					lvCachedTerms.setOnItemLongClickListener(new OnItemLongClickListener()
					{

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
						{
							//mostra a opção de apagar o termo da lista
							final ZLTerm t = termsAdapter.getItem(pos);

							new AlertDialog.Builder(HomeActivity.this).setTitle("Aviso").setMessage("Apagar o termo \"" + t.term + "\"?")
									.setPositiveButton("Apagar", new DialogInterface.OnClickListener()
									{

										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											ZLListCache.getInstance().deleteTerm(t);
											termsAdapter.notifyDataSetChanged();
										}
									}).setNegativeButton("Cancelar", null).setIcon(android.R.drawable.ic_dialog_alert).show();
							return true;
						}
					});
					//ao clicar um termo da lista
					lvCachedTerms.setOnItemClickListener(new OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
						{
							//faz a pesquisa. Ver o código acima.

							flCachedTermsBorder.setVisibility(View.GONE);
							ZLTerm t = termsAdapter.getItem(pos);
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(etBusca.getWindowToken(), 0);

							try
							{

								MixpanelAPI mixPanel = MixpanelAPI.getInstance(HomeActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("Termo", etBusca.getText().toString());
								mixPanel.track("HomeBusca", props);

								String[] words = etBusca.getText().toString().split(" ");

								for (int i = 0; i < words.length; i++)
								{
									String word = words[i];
									JSONObject props2 = new JSONObject();
									props2.put("Palavra", word);
									mixPanel.track("HomeBuscaPalavra", props2);
								}

							}
							catch (Exception e)
							{

							}

							ZLServices.getInstance().listCovenants("" + getLatitude(), "" + getLongitude(), false, 1000, 0,
									ZLCity.getCurrentCity(HomeActivity.this).id, false, null, null, null, t.term, true, HomeActivity.this, new ZLServiceOperationCompleted<List<ZLStore>>()
									{

										@Override
										public void operationCompleted(ZLServiceResponse<List<ZLStore>> response)
										{
											if (response.errorMessage != null)
											{
												Toast.makeText(HomeActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
											}
											else
											{
												Bundle bundle = new Bundle();
												bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, etBusca.getText().toString());
												bundle.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, etBusca.getText().toString());
												BaseZolkinActivity.logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, bundle);


												etBusca.setText("");
												// show results
												ListaActivity.list = response.serviceResponse;
												ListaActivity.category = null;
												startActivity(new Intent(HomeActivity.this, ListaActivity.class));

											}

										}
									});
						}
					});
				}
			}
		});

		//verifica se esta tela esta sendo acessada pela primeira vez
		//e se sim, mostra o tutorial da tela
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("coach_home"))
		{
			imgvBtHideCoach.setVisibility(View.GONE);
		}
		else
		{
			imgvBtHideCoach.setVisibility(View.VISIBLE);
			imgvBtHideCoach.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0)
				{
					imgvBtHideCoach.setVisibility(View.GONE);
					SharedPreferences prefsInner = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefsInner.edit();
					editor.putString("coach_home", "coach_home");
					editor.apply();
				}
			});
		}

		// load categories
		ZLServices.getInstance().getCategoryFilters(ZLCity.getCurrentCity(this), true, this, new ZLServiceOperationCompleted<List<ZLCategory>>() {
			@Override
			public void operationCompleted(ZLServiceResponse<List<ZLCategory>> response) {
				if (response.errorMessage != null) {
					// show Dialog with retry
					//Toast.makeText(HomeActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
				} else {
					//cria o adapter e mostra as categorias carregadas na tela
					categories = response.serviceResponse;
					adapter = new HomeCategoriesAdapter(HomeActivity.this, categories);
					lvCategories.setAdapter(adapter);
				}
			}
		});

		//ao clicar no botão de mudar localizaação
		btLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//se já temos um endereço customizado
				if (isCustomAddressSet()) {
					//limpar e voltar para a localização real do usuário
					btLocation.setImageResource(R.drawable.bt_change_location_n);
					setCustomAddress(null);
				} else {
					//senão, ir para tela de mudar location
					startActivity(new Intent(HomeActivity.this, ChangeLocationActivity.class));
				}

			}
		});

		//ao clicar em buscar no telcado
		etBusca.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
				//veja se foi realmente clicado o buscar. Isto pode variar dependendo do aparelho.
				if (event == null)
					return false;
				if (event.getAction() != KeyEvent.ACTION_DOWN)
					return false;

				return search();

			}
		});

	}


	private boolean search()
	{
		//verifica se o campo não esta vazio
		etBusca.setText(mAutocompleteView.getText().toString());
		if (Util.stringIsNullEmptyOrWhiteSpace(etBusca.getText().toString()) || etBusca.getText().toString().length() < 3)
		{
			Toast.makeText(HomeActivity.this, "Atenção: digite pelo menos tres caracteres para fazer a busca", Toast.LENGTH_LONG).show();
			return false;
		}

		//esconde o teclado
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etBusca.getWindowToken(), 0);

		//loga as palavras buscadas no mixpanel
		try
		{

			MixpanelAPI mixPanel = MixpanelAPI.getInstance(HomeActivity.this, Constants.MIXPANEL_TOKEN);
			JSONObject props = new JSONObject();
			props.put("Termo", etBusca.getText().toString());
			mixPanel.track("HomeBusca", props);

			String[] words = etBusca.getText().toString().split(" ");

			for (int i = 0; i < words.length; i++)
			{
				String word = words[i];
				JSONObject props2 = new JSONObject();
				props2.put("Palavra", word);
				mixPanel.track("HomeBuscaPalavra", props2);
			}

		}
		catch (Exception e)
		{

		}

		//chama o serviço de lista de estabelecimentos com o termo buscado
		ZLServices.getInstance().listCovenants("" + getLatitude(), "" + getLongitude(), false, 1000, 0, ZLCity.getCurrentCity(HomeActivity.this).id, false,
				null, null, null, etBusca.getText().toString(), true, HomeActivity.this, new ZLServiceOperationCompleted<List<ZLStore>>() {

					@Override
					public void operationCompleted(ZLServiceResponse<List<ZLStore>> response) {
						if (response.errorMessage != null) {
							Toast.makeText(HomeActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						} else {
							//etBusca.setText("");
							if (response.serviceResponse.size() > 0) {
								// show results
								Bundle bundle = new Bundle();
								bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, etBusca.getText().toString());
								bundle.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, etBusca.getText().toString());
								BaseZolkinActivity.logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, bundle);

								ListaActivity.list = response.serviceResponse;
								ListaActivity.category = null;
								ListaActivity.searchText = etBusca.getText().toString();
								startActivity(new Intent(HomeActivity.this, ListaActivity.class));
							} else {
								Toast.makeText(HomeActivity.this, "Nenhum estabelecimento encontrado para o termo buscado.", Toast.LENGTH_LONG).show();
							}
						}

					}
				});
		return true;
	}

	public boolean isConnected()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = cm.getActiveNetworkInfo();
		if (net != null && net.isAvailable() && net.isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{

			matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches_text.size() > 0)
			{
				etBusca.setText(matches_text.get(0));
				search();
			}
			else
			{
				Toast.makeText(this, "Não entendi. Por favor, fale de novo.", Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//ao acessar esta tela
		//Checagem do serviço de localização do android.
		etBusca.setText("");
		lvCategories.invalidateViews();
		SharedPreferences settings;
		SharedPreferences.Editor editor;
		settings = getApplicationContext().getSharedPreferences("Zolkin-Location-Settings", Context.MODE_PRIVATE);
		String resposta = settings.getString("LocationService",null);


		if (!isLocationServiceEnabled() && !resposta.equals("visualizado"))
			displayServiceMessage();

		if (ZLUser.getLoggedUser().isAuthenticated()) {
			implementLocationService(getApplicationContext());

			// load user data
			ZLServices.getInstance().show(false, this, new ZLServiceOperationCompleted<Boolean>()
			{

				@Override
				public void operationCompleted(ZLServiceResponse<Boolean> response)
				{
					// update menu info
					updateUserInfo();
				}
			});

			ZLServices.getInstance().getNumUnreadMessages(false, this, new ZLServiceOperationCompleted<Integer>()
			{

				@Override
				public void operationCompleted(ZLServiceResponse<Integer> response)
				{
					if (response.errorMessage == null)
					{
						int n = response.serviceResponse.intValue();
						tvNewMessagesHome.setText(""+n);
						tvNewMessagesHome.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
					}
					else
					{
						tvNewMessagesHome.setVisibility(View.GONE);
					}
				}
			});

			//check for pending action
			if (pendingAction != null) {
				//process pending action
				switch (pendingAction.actionType) {
					case ZLPendingActionTypeCovenant:
						DetailActivity.storeID = Integer.parseInt(pendingAction.param);
						DetailActivity.distance = 0;
						DetailActivity.saveIDs(this);
						pendingAction = null;
						startActivity(new Intent(this, DetailActivity.class));

						break;
					case ZLPendingActionTypeSearch:
						etBusca.setText(pendingAction.param);
						pendingAction = null;
						search();
						break;
					case ZLPendingActionTypeSurvey:
						startActivity(new Intent(this, MessagesActivity.class));
						break;
					case ZLPendingActionTypeMessage:
						startActivity(new Intent(this, MessagesActivity.class));
						break;
					case ZLPendingActionTypeLocation:
						ZLServices.getInstance().listCovenantsByLocationId(String.valueOf(ZLLocationService.getLatitude()),
								String.valueOf(ZLLocationService.getLongitude()),
								Integer.parseInt(pendingAction.param), true, this, new ZLServiceOperationCompleted<List<ZLStore>>() {

									@Override
									public void operationCompleted(ZLServiceResponse<List<ZLStore>> response) {
										if (response.errorMessage == null) {
											ListaActivity.category = null;
											ListaActivity.list = response.serviceResponse;
											ListaActivity.needReload = false;
											HomeActivity.this.startActivity(new Intent(HomeActivity.this, ListaActivity.class));
											pendingAction = null;
										}
									}
								});

						break;
					case ZLPendingActionTypeExtract:
						Tracker t = ((ZolkinApplication) getApplication()).getDefaultTracker();
						t.setScreenName("Extrato");
						t.send(new HitBuilders.EventBuilder()
								.setAction("Extrato")
								.setCategory("Transação")
								.setLabel("Extrato")
								.build());
						BaseZolkinActivity.logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, 0.0);

						DecimalFormat df = new DecimalFormat("0.00");

						ExtratoActivity.extrato = df.format(ZLUser.getLoggedUser().kinBalance);
						ExtratoActivity.economia = df.format(ZLUser.getLoggedUser().accumulatedEconomy);
						startActivity(new Intent(this, ExtratoActivity.class));
						pendingAction = null;

						break;
					default:
						break;
				}
			}
		} else {

		}

		//atualizado o botão de mudar location mostrando se estamos usando uma localização customizada ou não
		if (isCustomAddressSet())
		{
			btLocation.setImageResource(R.drawable.bt_change_location_s);
		}
		else
		{
			btLocation.setImageResource(R.drawable.bt_change_location_n);
		}
	}


	private void implementLocationService(Context applicationContext) {
		SharedPreferences settings;
		SharedPreferences.Editor editor;

		if (!isLocationServiceEnabled())
			return;


		settings = applicationContext.getSharedPreferences("Zolkin-Location", Context.MODE_PRIVATE);

		editor = settings.edit();

		if (settings.contains("initialized")) {
			if (ZLUser.getLoggedUser().isAuthenticated() && !settings.contains("activeUser")) {
				editor.putString("activeUser", ZLUser.getLoggedUser().email);
				editor.putString("pushToken", GCMUtils.getInstance(applicationContext).getRegistrationId());
				editor.apply();
			}

			startService(new Intent(applicationContext.getApplicationContext(), ZLLocationService.class));
		} else {
			editor.putBoolean("initialized", true);
			if (ZLUser.getLoggedUser().isAuthenticated()) {
				editor.putString("activeUser", ZLUser.getLoggedUser().email);
				editor.putString("pushToken", GCMUtils.getInstance(applicationContext).getRegistrationId());
				editor.apply();
				startService(new Intent(applicationContext.getApplicationContext(), ZLLocationService.class));
			}
		}
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}



	//ao pressionar o back do aparelho
	@Override
	public void onBackPressed() {
		//confirma se o usuário quer realmente sair do zolkin
		lvCategories.requestFocus();
		flCachedTermsBorder.setVisibility(View.GONE);
		new AlertDialog.Builder(HomeActivity.this).setTitle("Sair").setMessage("Tem certeza que deseja sair do Zolkin? (utilize o menu lateral para ir para outras telas)")
				.setPositiveButton("Sair", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						//sair do Zolkin
						finish();
					}
				}).setNegativeButton("Cancelar", null).setIcon(android.R.drawable.ic_dialog_alert).show();
		// super.onBackPressed();
	}


	private void activeAndroidLocation() {
		SharedPreferences settings;
		SharedPreferences.Editor editor;
		settings = getApplicationContext().getSharedPreferences("Zolkin-Location-Settings", Context.MODE_PRIVATE);
		editor = settings.edit();
		editor.putString("LocationService", "visualizado");
		editor.apply();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Seu serviço de localização esta desativado, para uma melhor experiência deseja ativa lo agora?")
				.setCancelable(false)
				.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("Não", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();


	}

	private boolean isLocationServiceEnabled(){
		LocationManager locationManager = null;
		boolean gps_enabled= false, network_enabled = false;

		if(locationManager == null)
			locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

		try {
			gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception ex) {}

		try {
			network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch(Exception ex) {}

		return gps_enabled || network_enabled;

	}

	private void displayServiceMessage() {
		AsyncTask <Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
			boolean locationEnable;
			@Override
			protected Void doInBackground(Void... params) {
				locationEnable = isLocationServiceEnabled();
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				if (!locationEnable) {
					SharedPreferences settings;
					SharedPreferences.Editor editor;
					settings = getApplicationContext().getSharedPreferences("Zolkin-Location-Settings", Context.MODE_PRIVATE);
					String resposta = settings.getString("LocationService", null);
					if (!resposta.equals("visualizado"))
						activeAndroidLocation();
				}
			}
		};

		asyncTask.execute();
	}


	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}


	private AdapterView.OnItemClickListener mAutocompleteClickListener
			= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final AutocompletePrediction item = mAdapter.getItem(position);
			final String placeId = item.getPlaceId();
			final CharSequence primaryText = item.getPrimaryText(null);

			Log.i(TAG, "Autocomplete item selected: " + primaryText);

			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(mGoogleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

			Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
		}
	};

	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
			= new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
				places.release();
				return;
			}
			final Place place = places.get(0);

			etBusca.setText(place.getName());
			mAutocompleteView.setText(place.getName());
			Log.i(TAG, "Place details received: " + place.getName());

			places.release();
			search();
		}
	};

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());

		Toast.makeText(this,
				"Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
	}


}
