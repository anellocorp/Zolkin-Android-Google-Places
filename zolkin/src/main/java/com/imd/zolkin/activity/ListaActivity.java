package com.imd.zolkin.activity;

//Tela de lista de estabelecimentos

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.imd.zolkin.adapter.ListMapCalloutAdapter;
import com.imd.zolkin.adapter.ListStoresAdapter;
import com.imd.zolkin.model.ZLCategory;
import com.imd.zolkin.model.ZLCity;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLSubCategory;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import br.com.zolkin.R;

public class ListaActivity extends BaseZolkinActivity
{
	private static final int REQUEST_CODE = 123455;
	
	// parâmetros externos recebidos da home e da tela de filtro de
	// subcategorias
	public static ZLCategory category;
	public static String searchText;
	public static List<ZLStore> list, filteredList;
	public static HashMap<Integer, ZLSubCategory> subCatsFilter;

	// UI
	TextView tvCatName = null;
	ImageView btVoltar = null;
	ImageView btLocation = null, btClearText = null;
	EditText etSearch = null;
	ImageView imgvBtFilter = null;
	ImageView imgvBtSortType = null;
	Button btMapList = null;
	ListView lvStores = null;
	MapView mapView = null;
	ImageView imgvBtHideCoach = null;
	LinearLayout llNoResults = null;
	ImageView btSpeech = null;
	FrameLayout flRoot = null;

	// indica se é necessário atualizar os resultados a partir do serviço.
	// isso é necessário, por exemplo, quando mudamos a location, o que invalida
	// todos os resultados.
	public static boolean needReload = true;
	boolean firstMapLayout = true;

	enum SortType
	{
		SortTypeEconomy, SortTypeDistance
	};

	// tipo de ordenação da lista
	SortType sortType;

	ListStoresAdapter adapter = null;

	HashMap<Marker, ZLStore> markersMap;
	ArrayList<Marker> markers;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista);

		tvCatName = (TextView) findViewById(R.id.tvCatName);
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		btLocation = (ImageView) findViewById(R.id.btLocation);
		btClearText = (ImageView) findViewById(R.id.btClearText);
		etSearch = (EditText) findViewById(R.id.etSearch);
		imgvBtFilter = (ImageView) findViewById(R.id.imgvBtFilter);
		imgvBtSortType = (ImageView) findViewById(R.id.imgvBtSortType);
		btMapList = (Button) findViewById(R.id.btMapList);
		mapView = (com.google.android.gms.maps.MapView) findViewById(R.id.mapView);
		lvStores = (ListView) findViewById(R.id.lvStores);
		imgvBtHideCoach = (ImageView) findViewById(R.id.imgvBtHideCoach);
		llNoResults = (LinearLayout) findViewById(R.id.llNoResults);
		btSpeech = (ImageView) findViewById(R.id.btSpeech);
		flRoot = (FrameLayout) findViewById(R.id.flRoot);

		hideSoftKeyboard();


		// inicializa o mapa
		mapView.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());
		mapView.setClickable(true);

		etSearch.setText("");

		if (searchText != null)
			etSearch.setText(searchText);
		else

		
		btClearText.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				etSearch.setText("");
			}
		});
		
		flRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				Rect r = new Rect();
				// r will be populated with the coordinates of your view that
				// area still visible.
				flRoot.getWindowVisibleDisplayFrame(r);

				int heightDiff = flRoot.getRootView().getHeight() - (r.bottom - r.top);
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
					btClearText.setVisibility(View.GONE);
					btSpeech.setVisibility(View.VISIBLE);
				}
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

		// quando o mapa é carregado, listar os estabelecimentos
		if (Build.VERSION.SDK_INT >= 11)
		{
			mapView.addOnLayoutChangeListener(new OnLayoutChangeListener()
			{

				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
				{
					if (firstMapLayout)
					{
						firstMapLayout = false;
						rebuildMap();
					}
				}
			});
		}

		// inicialmente, ordenamos por economia
		sortType = SortType.SortTypeDistance;

		// configura o titulo da tela
		if (category == null)
		{
			// lista, se foi uma busca por palavra
			tvCatName.setText("Lista");
		}
		else
		{
			// nome da categoria, se foi uma busca por categoria
			tvCatName.setText(category.name);
		}

		// se é o primeiro acesso, mostrar o tutorial da tela
		SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("coach_lista"))
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
					editor.putString("coach_lista", "coach_lista");
					editor.apply();
				}
			});
		}

		// mudar a ordem
		imgvBtSortType.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if (sortType == SortType.SortTypeEconomy)
				{
					sortType = SortType.SortTypeDistance;
					imgvBtSortType.setImageResource(R.drawable.segmented_maisperto);

					try
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(ListaActivity.this, Constants.MIXPANEL_TOKEN);
						JSONObject props = new JSONObject();
						props.put("Tipo", "Distancia");
						mixPanel.track("MudouOrdem", props);
					}
					catch (Exception e)
					{

					}
				}
				else
				{
					sortType = SortType.SortTypeEconomy;
					imgvBtSortType.setImageResource(R.drawable.segmented_maioreconomia);

					try
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(ListaActivity.this, Constants.MIXPANEL_TOKEN);
						JSONObject props = new JSONObject();
						props.put("Tipo", "Economia");
						mixPanel.track("MudouOrdem", props);
					}
					catch (Exception e)
					{

					}
				}
				// re-ordena a lista
				sortList();
			}
		});

		// ao voltar, limpa o filtro de subcategorias
		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				subCatsFilter = null;
				finish();
			}
		});

		// ao clicar em filtrar
		imgvBtFilter.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				// cria um mapa <idsubcategoria,subcategoria>
				subCatsFilter = new HashMap<Integer, ZLSubCategory>();

				// para cada estabelecimento da lista
				for (int i = 0; i < list.size(); i++)
				{
					ZLStore st = list.get(i);

					for (int j = 0; j < st.subCategories.size(); j++)
					{
						ZLSubCategory sc = st.subCategories.get(j);
						
						// coloca a subcategoria no mapa
						subCatsFilter.put(Integer.valueOf(sc.subCatId), sc);
						// se já tínhamos um filtro ativo
						if (FilterActivity.dSubCategories != null)
						{
							// mantem o mesmo status de seleção das subCategorias
							if (FilterActivity.dSubCategories.containsKey(Integer.valueOf(sc.subCatId)))
							{
								sc.selected = FilterActivity.dSubCategories.get(Integer.valueOf(sc.subCatId)).selected;
							}
							else
							{
								sc.selected = true;
							}
						}
						else
						{
							// senão, todas as categorias vem inicialmente
							// deselecionadas
							sc.selected = false;
						}
					}
				}
				// passa as subcategorias para a tela de filtro
				FilterActivity.dSubCategories = subCatsFilter;
				// mostra a tela de filtro
				startActivity(new Intent(ListaActivity.this, FilterActivity.class));
			}
		});

		// ao clicar me buscar no teclado
		etSearch.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event)
			{
				// verifica se foi o buscar mesmo
				if (event == null)
					return false;
				if (event.getAction() != KeyEvent.ACTION_DOWN)
					return false;

				return search();
			}
		});

		// ao clicar o botão que muda entre mapa e lista
		btMapList.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mapView.getVisibility() == View.GONE)
				{
					// show map
					mapView.setVisibility(View.VISIBLE);
					lvStores.setVisibility(View.GONE);
					rebuildMap();

					btMapList.setText("LISTA");

					try
					{
						MixpanelAPI mixPanel = MixpanelAPI.getInstance(ListaActivity.this, Constants.MIXPANEL_TOKEN);
						mixPanel.track("AbriuMapa", null);

					}
					catch (Exception e)
					{

					}
				}
				else
				{
					// hide map
					mapView.setVisibility(View.GONE);
					lvStores.setVisibility(View.VISIBLE);
					btMapList.setText("MAPA");

					// btMapList.setBackgroundResource(R.drawable.bt_mapa_border);
				}

			}
		});

		// se clicar em um estabelecimento
		lvStores.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
			{
				// passa o estabelecimento e sua distância para a tela de
				// detalhe
				DetailActivity.storeID = adapter.getItem(pos).covenantId;
				DetailActivity.distance =  createDistance(adapter.getItem(pos).latitude,
						adapter.getItem(pos).longitude);
				DetailActivity.saveIDs(ListaActivity.this);
				
				try
				{

					MixpanelAPI mixPanel = MixpanelAPI.getInstance(ListaActivity.this, Constants.MIXPANEL_TOKEN);
					JSONObject props = new JSONObject();
					props.put("IdEstabelecimento", "" + adapter.getItem(pos).covenantId);
					props.put("IdCategoria", "" + adapter.getItem(pos).category.catId);
					props.put("NomeCategoria", "" + adapter.getItem(pos).category.name);
					props.put("Cidade", "" + adapter.getItem(pos).address.city);
					props.put("Bairro", "" + adapter.getItem(pos).address.neighborhood);
					props.put("Nome", "" + adapter.getItem(pos).name);
					mixPanel.track("SelecionouEstabelecimento", props);

				}
				catch (Exception e)
				{

				}
				
				// mostra o detalhe do estabelecimento
				startActivity(new Intent(ListaActivity.this, DetailActivity.class));
			}
		});

		// mostra a tela de mudar location
		btLocation.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (ZLLocationService.isCustomAddressSet())
				{
					btLocation.setImageResource(R.drawable.bt_change_location_n);
					ZLLocationService.setCustomAddress(null);
					needReload = true;
					onResume();
				}
				else
				{
					startActivity(new Intent(ListaActivity.this, ChangeLocationActivity.class));
				}
			}
		});
	} //end onCreate()

	protected boolean search()
	{
		// verifica se foram digitados pelo menos 3 caractéres
		if (Util.stringIsNullEmptyOrWhiteSpace(etSearch.getText().toString()) || etSearch.getText().toString().length() < 3)
		{
			Toast.makeText(ListaActivity.this, "Atenção: digite pelo menos tres caracteres para fazer a busca", Toast.LENGTH_LONG).show();
			return false;
		}

		// esconde o teclado
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

		// loga no mixpanel as palavras buscadas
		try
		{
			MixpanelAPI mixPanel = MixpanelAPI.getInstance(ListaActivity.this, Constants.MIXPANEL_TOKEN);
			JSONObject props = new JSONObject();
			props.put("Termo", etSearch.getText().toString());
			mixPanel.track("ListaBusca", props);

			String[] words = etSearch.getText().toString().split(" ");

			for (int i = 0; i < words.length; i++)
			{
				String word = words[i];
				JSONObject props2 = new JSONObject();
				props2.put("Palavra", word);
				mixPanel.track("ListaBuscaPalavra", props2);
			}
		}
		catch (Exception e)
		{

		}

		// chama o serviço de busca
		ZLServices.getInstance().listCovenants("" + ZLLocationService.getLatitude(), "" + ZLLocationService.getLongitude(), false, 1000, 0, ZLCity.getCurrentCity(ListaActivity.this).id,
				false, null, null, null, etSearch.getText().toString(), true, ListaActivity.this, new ZLServiceOperationCompleted<List<ZLStore>>()
				{

					@Override
					public void operationCompleted(ZLServiceResponse<List<ZLStore>> response)
					{
						if (response.errorMessage != null)
						{
							Toast.makeText(ListaActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
						}
						else
						{
							if (response.serviceResponse.size() > 0)
							{
								// filtra o resultado
								list = response.serviceResponse;
								filterList();
								llNoResults.setVisibility(View.GONE);
							}
							else
							{
								// se não tem resultado, mostra a tela
								// de nenhum resultado encontrado
								llNoResults.setVisibility(View.VISIBLE);
							}
						}

					}
				});
		return false;
	} //end search()

	@Override
	protected void onResume()
	{
		super.onResume();
		mapView.onResume();

		// mostra no botão se tem um filtro de subcategorias ativo ou não
		if (subCatsFilter != null)
		{
			imgvBtFilter.setImageResource(R.drawable.bt_filtros_s);
		}
		else
		{
			imgvBtFilter.setImageResource(R.drawable.bt_filtros);
		}

		// only needs to reload when user's location has been changed, through
		// ChangeLocationActivity
		if (needReload)
		{
			needReload = false;
			// atualiza os dados quando a location mudou
			ZLServices.getInstance().listCovenants("" + ZLLocationService.getLatitude(), "" + ZLLocationService.getLongitude(), false, 1000, 0, ZLCity.getCurrentCity(ListaActivity.this).id, false,
					null, null, null, etSearch.getText().toString(), true, ListaActivity.this, new ZLServiceOperationCompleted<List<ZLStore>>()
					{

						@Override
						public void operationCompleted(ZLServiceResponse<List<ZLStore>> response)
						{
							if (response.errorMessage != null)
							{
								Toast.makeText(ListaActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
							}
							else
							{
								if (response.serviceResponse.size() > 0)
								{
									list = response.serviceResponse;
									// filtra a lista
									filterList();
									llNoResults.setVisibility(View.GONE);
								}
								else
								{
									Toast.makeText(ListaActivity.this, "Nenhum estabelecimento encontrato para o termo buscado.", Toast.LENGTH_LONG).show();
									llNoResults.setVisibility(View.VISIBLE);
								}
							}

						}
					});
		}
		else
		{
			// filtra a lista
			filterList();
		}
		if (ZLLocationService.isCustomAddressSet())
		{
			btLocation.setImageResource(R.drawable.bt_change_location_s);
		}
		else
		{
			btLocation.setImageResource(R.drawable.bt_change_location_n);
		}
	}

	protected double createDistance(double latStore, double lonStore) {
		double currentLat = ZLLocationService.getLatitude();
		double currentLon = ZLLocationService.getLongitude();

		final int R = 6371; // Raio da Terra em KM

		Double latDistance = Math.toRadians(currentLat - latStore);
		Double lonDistance = Math.toRadians(currentLon - lonStore);

		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(latStore)) * Math.cos(Math.toRadians(currentLat))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // conversão para metros

		distance = Math.pow(distance, 2);

		return (Math.sqrt(distance) / 1000);
	}
	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mapView.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		mapView.onSaveInstanceState(outState);
		
		//needReload = true;
	}

	public void onLowMemory()
	{
		super.onLowMemory();

		mapView.onLowMemory();
	}

	// este método filtra a lista de resultados com base no filtro de
	// subcategorias ativo, se tem
	void filterList()
	{
		if (list == null)
			return;
		filteredList = new ArrayList<ZLStore>();
		for (int i = 0; i < list.size(); i++)
		{
			ZLStore st = list.get(i);

			// no momento, não filtra por categoria principal porque pode ter
			// mais que uma para o mesmo estabelecimento
			/*
			 * if (category != null) { if (st.category.catId != category.catId)
			 * { continue; } }
			 */
			// filtra por subCategoria
			if (subCatsFilter != null)
			{
				for (int j = 0; j < st.subCategories.size(); j++)
				{
					ZLSubCategory sc1 = st.subCategories.get(j);
					Integer it = Integer.valueOf(sc1.subCatId);
//					if (!subCatsFilter.containsKey(it))
//					{
//						continue;
//					}
//					else
//					{
//						ZLSubCategory sc = subCatsFilter.get(it);
//						if (!sc.selected)
//						{
//							continue;
//						}
//					}
					if (subCatsFilter.containsKey(it))
					{
						ZLSubCategory sc = subCatsFilter.get(it);
						if (sc.selected)
						{
							ZLSubCategory sc2 = subCatsFilter.get(it);
							filteredList.add(st);
							break;
						}
						
					}
					
				}
			}
			else
			{
				filteredList.add(st);
			}
			
		}

		// atualizada o mapa
		rebuildMap();
		// re-ordena a lista
		sortList();
	}

	// ordena a lista de acordo com o critério selecionado
	void sortList()
	{
		if (sortType == SortType.SortTypeDistance)
		{
			Collections.sort(filteredList, new Comparator<ZLStore>()
			{
				@Override
				public int compare(ZLStore lhs, ZLStore rhs)
				{
					// estabelecimentos fechados vem por últimos
					if (lhs.businessHours.isClosed != rhs.businessHours.isClosed)
					{
						if (lhs.businessHours.isClosed)
							return 1;
						if (rhs.businessHours.isClosed)
							return -1;
					}

					// ordena por distância
					if (lhs.distance < rhs.distance)
						return -1;
					else if (lhs.distance > rhs.distance)
						return 1;
					else
						return 0;
				}
			});
		}
		else
		{
			Collections.sort(filteredList, new Comparator<ZLStore>()
			{
				@Override
				public int compare(ZLStore lhs, ZLStore rhs)
				{
					// estabelecimentos fechados vem por últimos
					if (lhs.businessHours.isClosed != rhs.businessHours.isClosed)
					{
						if (lhs.businessHours.isClosed)
							return 1;
						if (rhs.businessHours.isClosed)
							return -1;
					}

					// ordena por economia.
					// Dependendo do estabelecimento estar fechado ou não, pega
					// a economia válida em campos diferentes do model
					int economy1 = lhs.percentageEconomy;
					int economy2 = rhs.percentageEconomy;
					if (lhs.businessHours.isClosed)
					{
						if (lhs.newEconomy != null)
						{
							economy1 = lhs.newEconomy.discountPercentage;
						}
						else
						{
							economy1 = 0;
						}
					}
					if (rhs.businessHours.isClosed)
					{
						if (rhs.newEconomy != null)
						{
							economy2 = rhs.newEconomy.discountPercentage;
						}
						else
						{
							economy2 = 0;
						}
					}

					if (economy1 < economy2)
						return 1;
					else if (economy1 > economy2)
						return -1;
					else
						return 0;
				}
			});
		}

		// se não sobrou nada, mostra a tela de sem resultados
		if (filteredList.size() > 0)
		{
			llNoResults.setVisibility(View.GONE);
		}
		else
		{
			llNoResults.setVisibility(View.VISIBLE);
		}

		// mostra a lista filtrada na tela
		adapter = new ListStoresAdapter(this, filteredList);
		lvStores.setAdapter(adapter);
	} //emd sortList()

	public void hideSoftKeyboard()
	{
		if (getCurrentFocus() != null)
		{
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	@Override
	public void onBackPressed()
	{
		subCatsFilter = null;
		super.onBackPressed();
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

			ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches_text.size() > 0)
			{
				etSearch.setText(matches_text.get(0));
				search();
			}
			else
			{
				Toast.makeText(this, "Não entendi. Por favor, fale de novo.", Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	//limpa o mapa e re-adiciona todos os pinos
	void rebuildMap()
	{
		try
		{
			GoogleMap map = mapView.getMap();

			if (map != null)
			{
				map.clear();

				markersMap = new HashMap<Marker, ZLStore>();

				// https://developers.google.com/maps/documentation/android/infowindows#custom_info_windows
				//customiza o popup que aparece ao clicar em um pino (infoWindow)
				map.setInfoWindowAdapter(new ListMapCalloutAdapter(this, markersMap));
				
				//ao clicar em um popup
				map.setOnInfoWindowClickListener(null);
				map.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
				{

					@Override
					public void onInfoWindowClick(Marker m)
					{
						if (markersMap.containsKey(m))
						{
							//mostra os detalhes do estabelecimento
							final ZLStore r = markersMap.get(m);

							DetailActivity.storeID = r.covenantId;
							DetailActivity.saveIDs(ListaActivity.this);
							startActivity(new Intent(ListaActivity.this, DetailActivity.class));
						}
					}
				});

				
				//popula o mapa com os pinos
				markers = new ArrayList<Marker>();

				for (int i = 0; i < filteredList.size(); i++)
				{
					ZLStore store = filteredList.get(i);

					Marker m = null;
					m = map.addMarker(new MarkerOptions().position(new LatLng(store.latitude, store.longitude)).title(store.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_zolkin)));

					markersMap.put(m, store);
					markers.add(m);
				}

				//da zoom no mapa para mostrar todos os itens adicionados
				if (filteredList.size() > 0)
				{
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Marker marker : markers)
					{
						builder.include(marker.getPosition());
					}
					LatLngBounds bounds = builder.build();
					int padding = 50; // offset from edges of the map in pixels
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
					map.animateCamera(cu);
				}
			}
		}
		catch (Exception e)
		{
			if (Util.LOG)
				Log.d("ChatLocal", "mpa icons error: " + e.toString());
		}
	}
}
