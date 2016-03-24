package com.imd.zolkin.activity;

//Tela de detalhe do estabelecimento

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.imd.zolkin.adapter.DetailPaymentMethodAdapter;
import com.imd.zolkin.adapter.StoresPagerAdapter;
import com.imd.zolkin.model.ZLAvailableService;
import com.imd.zolkin.model.ZLDiscountForecast;
import com.imd.zolkin.model.ZLPaymentOption;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.services.ZLLocationService;
import com.imd.zolkin.services.ZLServiceOperationCompleted;
import com.imd.zolkin.services.ZLServiceResponse;
import com.imd.zolkin.services.ZLServices;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.MyImageDownloader;
import com.imd.zolkin.util.MyImageDownloader.GetImageResult;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import br.com.zolkin.R;

public class DetailActivity extends BaseZolkinActivity
{
	// estabelecimento que esta sendo mostrado
	public static int storeID;

	public ZLStore store;
	public static double distance;

	ImageView btVoltar = null;
	com.innovattic.font.FontTextView tvAtualizado = null;
	android.support.v4.view.ViewPager vpImages = null;
	com.imd.zolkin.custom.PageControlView pcStoreImages = null;
	ImageView imgvBtFavorite = null;
	com.innovattic.font.FontTextView tvName = null;
	com.innovattic.font.FontTextView tvCatName = null;
	FrameLayout flEconomyBar = null;
	com.innovattic.font.FontTextView tvEconomyUntil = null;
	ImageView imgvBadge = null;
	com.innovattic.font.FontTextView tvPercentage = null;
	ImageView imgvBtLigar = null;
	ImageView imgvBtComoChegar = null;
	ImageView imgvBtVerSite = null;
	ImageView imgvBtEconomiaHora = null;
	com.innovattic.font.FontTextView tvHorarioStart = null;
	com.innovattic.font.FontTextView tvHorarioEnd = null;
	com.innovattic.font.FontTextView tvTelefone = null;
	com.innovattic.font.FontTextView tvEmail = null;
	com.innovattic.font.FontTextView tvSite = null;
	com.google.android.gms.maps.MapView mapView = null;
	com.innovattic.font.FontTextView tvDistAddr = null;
	com.innovattic.font.FontTextView tvDesc = null;
	com.innovattic.font.FontTextView tvBtSaibaMais = null;
	LinearLayout llFormasDePagamento = null;
	LinearLayout llServicosOferecidos = null;
	FrameLayout llEconometro = null;
	LinearLayout llListaEconomias = null;
	ImageView imgvBtCloseEcon = null;
	LinearLayout llDesc = null;
	com.innovattic.font.FontTextView tvDescPopup = null;
	ImageView imgvBtCloseDesc = null;
	ImageView imgvBtHideCoach = null;
	View vMapClick = null;
	TextView tvCartoesAceitos = null;
	TextView tvOEstabOferece = null;
	ScrollView scDetailMain = null;
	WebView wvStoreSite = null;
	
	FrameLayout flDetailRoot = null;

	StoresPagerAdapter storesAdapter;
	
	public static void saveIDs(Context c)
	{
		SharedPreferences prefs = c.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("StoreDetailID", storeID);
		editor.putString ("StoreDetailDistance",  String.valueOf(distance));
		editor.apply();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		storeID = prefs.getInt("StoreDetailID", 0);
		distance = Double.parseDouble(prefs.getString("StoreDetailDistance", "0"));
		

		wvStoreSite = (WebView) findViewById(R.id.wvStoreSite);
		btVoltar = (ImageView) findViewById(R.id.btVoltar);
		tvAtualizado = (com.innovattic.font.FontTextView) findViewById(R.id.tvAtualizado);
		vpImages = (android.support.v4.view.ViewPager) findViewById(R.id.vpImages);
		pcStoreImages = (com.imd.zolkin.custom.PageControlView) findViewById(R.id.pcStoreImages);
		imgvBtFavorite = (ImageView) findViewById(R.id.imgvBtFavorite);
		tvName = (com.innovattic.font.FontTextView) findViewById(R.id.tvName);
		tvCatName = (com.innovattic.font.FontTextView) findViewById(R.id.tvCatName);
		flEconomyBar = (FrameLayout) findViewById(R.id.flEconomyBar);
		flDetailRoot = (FrameLayout) findViewById(R.id.flDetailRoot);
		tvEconomyUntil = (com.innovattic.font.FontTextView) findViewById(R.id.tvEconomyUntil);
		imgvBadge = (ImageView) findViewById(R.id.imgvBadge);
		tvPercentage = (com.innovattic.font.FontTextView) findViewById(R.id.tvPercentage);
		imgvBtLigar = (ImageView) findViewById(R.id.imgvBtLigar);
		imgvBtComoChegar = (ImageView) findViewById(R.id.imgvBtComoChegar);
		imgvBtVerSite = (ImageView) findViewById(R.id.imgvBtVerSite);
		imgvBtEconomiaHora = (ImageView) findViewById(R.id.imgvBtEconomiaHora);
		tvHorarioStart = (com.innovattic.font.FontTextView) findViewById(R.id.tvHorarioStart);
		tvHorarioEnd = (com.innovattic.font.FontTextView) findViewById(R.id.tvHorarioEnd);
		tvTelefone = (com.innovattic.font.FontTextView) findViewById(R.id.tvTelefone);
		tvEmail = (com.innovattic.font.FontTextView) findViewById(R.id.tvEmail);
		tvSite = (com.innovattic.font.FontTextView) findViewById(R.id.tvSite);
		mapView = (com.google.android.gms.maps.MapView) findViewById(R.id.mapView);
		tvDistAddr = (com.innovattic.font.FontTextView) findViewById(R.id.tvDistAddr);
		tvDesc = (com.innovattic.font.FontTextView) findViewById(R.id.tvDesc);
		tvBtSaibaMais = (com.innovattic.font.FontTextView) findViewById(R.id.tvBtSaibaMais);
		llFormasDePagamento = (LinearLayout) findViewById(R.id.llFormasDePagamento);
		llServicosOferecidos = (LinearLayout) findViewById(R.id.llServicosOferecidos);
		llEconometro = (FrameLayout) findViewById(R.id.llEconometro);
		llListaEconomias = (LinearLayout) findViewById(R.id.llListaEconomias);
		imgvBtCloseEcon = (ImageView) findViewById(R.id.imgvBtCloseEcon);
		llDesc = (LinearLayout) findViewById(R.id.llDesc);
		tvDescPopup = (com.innovattic.font.FontTextView) findViewById(R.id.tvDescPopup);
		imgvBtCloseDesc = (ImageView) findViewById(R.id.imgvBtCloseDesc);
		imgvBtHideCoach = (ImageView) findViewById(R.id.imgvBtHideCoach);
		vMapClick = findViewById(R.id.vMapClick);
		tvCartoesAceitos = (TextView) findViewById(R.id.tvCartoesAceitos);
		tvOEstabOferece = (TextView) findViewById(R.id.tvOEstabOferece);
		scDetailMain = (ScrollView) findViewById(R.id.scDetailMain);

		mapView.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());

		// se é a primeira vez, mostra o guia da tela
		prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs.contains("coach_detail"))
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
					editor.putString("coach_detail", "coach_detail");
					editor.apply();
				}
			});
		}

		btVoltar.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});

		// chama o serviço para pegar os detalhes do estabelecimento
		ZLServices.getInstance().getCovenantDetails(storeID, true, this, new ZLServiceOperationCompleted<ZLStore>()
		{
			@Override
			public void operationCompleted(ZLServiceResponse<ZLStore> response)
			{
				if (response.errorMessage != null)
				{
					Toast.makeText(DetailActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
					finish();
				}
				else
				{
					// salva os dados do estabalecimento
					store = response.serviceResponse;

					distance = createDistance(store.latitude, store.longitude);
					// distance = (store.distance / 1000);

					Calendar c = Calendar.getInstance();
					// mostra a hora de atualização
					tvAtualizado.setText(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));

					// fill data

					// se não esta logado, esconde o botão de favoritas
					if (!ZLUser.getLoggedUser().isAuthenticated())
					{
						imgvBtFavorite.setVisibility(View.GONE);
					}
					// senão mostra se é favorito ou não
					else if (store.isFavorite)
					{
						imgvBtFavorite.setImageResource(R.drawable.bt_fav_h);
					}
					else
					{
						imgvBtFavorite.setImageResource(R.drawable.bt_fav_n);
					}
					// se clicar em favorito
					imgvBtFavorite.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// chama o serviço para adicionar ou remover
							// favoritos
							ZLServices.getInstance().addRemoveFavorite(store.covenantId, !store.isFavorite, true, DetailActivity.this, new ZLServiceOperationCompleted<Boolean>()
							{

								@Override
								public void operationCompleted(ZLServiceResponse<Boolean> response)
								{
									if (response.errorMessage != null)
									{
										Toast.makeText(DetailActivity.this, response.errorMessage, Toast.LENGTH_LONG).show();
									}
									else
									{
										// ou mudar o status de favorito, mostra
										// um toast e loga no MixPanel
										store.isFavorite = !store.isFavorite;
										if (store.isFavorite)
										{
											Toast.makeText(DetailActivity.this, store.name + " adicionado aos favoritos", Toast.LENGTH_LONG).show();
											imgvBtFavorite.setImageResource(R.drawable.bt_fav_h);

											try
											{
												MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
												JSONObject props = new JSONObject();
												props.put("IdEstabelecimento", "" + store.covenantId);
												props.put("IdCategoria", "" + store.category.catId);
												props.put("NomeCategoria", "" + store.category.name);
												props.put("Cidade", "" + store.address.city);
												props.put("Bairro", "" + store.address.neighborhood);
												props.put("Nome", "" + store.name);
												props.put("Distancia", String.valueOf(distance));
												mixPanel.track("AdicionouFavorito", props);
											}
											catch (Exception e)
											{

											}
										}
										else
										{
											Toast.makeText(DetailActivity.this, store.name + " removido dos favoritos", Toast.LENGTH_LONG).show();
											imgvBtFavorite.setImageResource(R.drawable.bt_fav_n);

											try
											{
												MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
												JSONObject props = new JSONObject();
												props.put("IdEstabelecimento", "" + store.covenantId);
												props.put("IdCategoria", "" + store.category.catId);
												props.put("NomeCategoria", "" + store.category.name);
												props.put("Cidade", "" + store.address.city);
												props.put("Bairro", "" + store.address.neighborhood);
												props.put("Nome", "" + store.name);
												props.put("Distancia", "" + store.distance);
												mixPanel.track("RemoveuFavorito", props);
											}
											catch (Exception e)
											{

											}
										}
									}
								}
							});
						}
					});

					// se tem pelo menos uma imagem
					if (store.mobileImageList.size() > 0)
					{
						// baixa a imagem
						MyImageDownloader.getInstance(DetailActivity.this).getImageNow(store.mobileImageList.get(0), null, new GetImageResult()
						{

							@Override
							public void imageReceived(String url, String filename, Bitmap bm)
							{
								// se conseguiu baixar a imagem
								if (bm != null)
								{
									// calcula a proportação
									double imgRatio = ((double) bm.getHeight()) / ((double) bm.getWidth());
									// inicializa o PagerAdapter com a lista de
									// imagens
									storesAdapter = new StoresPagerAdapter(DetailActivity.this, store.mobileImageList);

									// calcula a altura correta para o ViewPager
									// para que as imagens fiquem da largura da
									// tela e altura proporcional à imagem, sem
									// distorção
									int w = vpImages.getWidth();
									int hs = (int) (w * imgRatio);
									android.view.ViewGroup.LayoutParams vg = vpImages.getLayoutParams();
									vg.height = hs;

									// atualiza o PageControl (pontinhos)
									pcStoreImages.numDots = store.mobileImageList.size();
									pcStoreImages.dotRadiusSp = (int) Util.convertDpToPixel(3, DetailActivity.this);
									pcStoreImages.dotColorSelected = 0xFF592d7f;
									pcStoreImages.dotColorUnselected = 0xA0BBBBBB;
									pcStoreImages.invalidate();
									vpImages.setLayoutParams(vg);

									// passa o Adapter para o ViewPager, para
									// mostrar as imagens
									vpImages.setAdapter(storesAdapter);
									// quando muda a imagem visivel
									vpImages.setOnPageChangeListener(new OnPageChangeListener()
									{

										@Override
										public void onPageSelected(int arg0)
										{
											// atualiza o PageControl
											pcStoreImages.setSeletedDot(arg0);
										}

										@Override
										public void onPageScrolled(int arg0, float arg1, int arg2)
										{

										}

										@Override
										public void onPageScrollStateChanged(int arg0)
										{

										}
									});
								}
							}
						});
					}

					try
					{
						GoogleMap map = mapView.getMap();

						if (map != null)
						{
							// se o mapa já carregou:

							// limpa
							map.clear();

							// deshabilita os controles de zoom
							map.getUiSettings().setZoomControlsEnabled(false);

							// se clicar em cima do mapa
							vMapClick.setOnClickListener(new OnClickListener()
							{

								@Override
								public void onClick(View arg0)
								{
									// mostra a rota e loga no MixPanel
									try
									{
										MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
										JSONObject props = new JSONObject();
										props.put("IdEstabelecimento", "" + store.covenantId);
										props.put("IdCategoria", "" + store.category.catId);
										props.put("NomeCategoria", "" + store.category.name);
										props.put("Cidade", "" + store.address.city);
										props.put("Bairro", "" + store.address.neighborhood);
										props.put("Nome", "" + store.name);
										props.put("Distancia", "" + distance);
										mixPanel.track("ComoChegar", props);
									}
									catch (Exception e)
									{

									}
									NumberFormat df = NumberFormat.getNumberInstance(new Locale("en", "us"));
									String url = String.format("http://maps.google.com/maps?saddr=&daddr=%s,%s", df.format(store.latitude), df.format(store.longitude));
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
									startActivity(intent);
								}
							});
							// o mesmo se clicar no pino
							map.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
							{

								@Override
								public void onInfoWindowClick(Marker arg0)
								{
									try
									{
										MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
										JSONObject props = new JSONObject();
										props.put("IdEstabelecimento", "" + store.covenantId);
										props.put("IdCategoria", "" + store.category.catId);
										props.put("NomeCategoria", "" + store.category.name);
										props.put("Cidade", "" + store.address.city);
										props.put("Bairro", "" + store.address.neighborhood);
										props.put("Nome", "" + store.name);
										props.put("Distancia", "" + distance);
										mixPanel.track("ComoChegar", props);
									}
									catch (Exception e)
									{

									}
									NumberFormat df = NumberFormat.getNumberInstance(new Locale("en", "us"));
									String url = String.format("http://maps.google.com/maps?saddr=&daddr=%s,%s", df.format(store.latitude), df.format(store.longitude));
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
									startActivity(intent);
								}
							});

							// adiciona o pino no mapa marcando a posição do
							// estabelecimento
							Marker m = null;
							m = map.addMarker(new MarkerOptions().position(new LatLng(store.latitude, store.longitude)).title(store.name)
									.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pin_zolkin))));

							CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15);
							map.animateCamera(cu);

						}
					}
					catch (Exception e)
					{
						if (Util.LOG)
							Log.d("ChatLocal", "mpa icons error: " + e.toString());
					}

					// preenche alguns dados de texto
					tvName.setText(store.name);
					tvCatName.setText(store.category.name);
					tvEconomyUntil.setText("economia válida até as " + store.currentEconomyStopTime);
					tvPercentage.setText("" + store.percentageEconomy + "%");

					// se esta fechado, altera o layout de acordo
					if (store.businessHours.isClosed)
					{

						tvEconomyUntil.setTextColor(0xFFFFFFFF);
						flEconomyBar.setBackgroundColor(0xFFA1A1A1);
						imgvBadge.setImageResource(R.drawable.badge_cinza);

						if (store.newEconomy != null)
						{
							tvPercentage.setText("" + store.newEconomy.discountPercentage + "%");
							tvEconomyUntil.setText("Economia válida a partir das " + store.newEconomy.startTime);
						}
						else
						{
							tvEconomyUntil.setText("Fechado no momento");
							tvPercentage.setText("--");
						}

					}

					// ligar para o estabelecimento usando o Intent padrão do
					// Android
					imgvBtLigar.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							try
							{
								MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("IdEstabelecimento", "" + store.covenantId);
								props.put("IdCategoria", "" + store.category.catId);
								props.put("NomeCategoria", "" + store.category.name);
								props.put("Cidade", "" + store.address.city);
								props.put("Bairro", "" + store.address.neighborhood);
								props.put("Nome", "" + store.name);
								props.put("Distancia", "" + distance);
								mixPanel.track("Ligou", props);
							}
							catch (Exception e)
							{

							}
							startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + store.phoneNumber)));
						}
					});

					// mostra a rota
					imgvBtComoChegar.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							try
							{
								MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("IdEstabelecimento", "" + store.covenantId);
								props.put("IdCategoria", "" + store.category.catId);
								props.put("NomeCategoria", "" + store.category.name);
								props.put("Cidade", "" + store.address.city);
								props.put("Bairro", "" + store.address.neighborhood);
								props.put("Nome", "" + store.name);
								props.put("Distancia", "" + distance);
								mixPanel.track("ComoChegar", props);
							}
							catch (Exception e)
							{

							}
							NumberFormat df = NumberFormat.getNumberInstance(new Locale("en", "us"));
							String url = String.format("http://maps.google.com/maps?saddr=&daddr=%s,%s", df.format(store.latitude), df.format(store.longitude));
							Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
							startActivity(intent);
						}
					});

					// vai para o site
					imgvBtVerSite.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							try
							{
								MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("IdEstabelecimento", "" + store.covenantId);
								props.put("IdCategoria", "" + store.category.catId);
								props.put("NomeCategoria", "" + store.category.name);
								props.put("Cidade", "" + store.address.city);
								props.put("Bairro", "" + store.address.neighborhood);
								props.put("Nome", "" + store.name);
								props.put("Distancia", "" + DetailActivity.distance);
								mixPanel.track("ViuSite", props);
							}
							catch (Exception e)
							{

							}
//							wvStoreSite.setVisibility(View.VISIBLE);
//							wvStoreSite.loadUrl(store.officialWebsite);
							
							
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(store.officialWebsite));
							if (store.officialWebsite.isEmpty())
								Toast.makeText(getApplicationContext(), "Não existe website para esse estabelecimento", Toast.LENGTH_SHORT).show();
							else
								startActivity(i);
						}
					});

					// mostra a lista com Economia por Hora se clicar no botão
					imgvBtEconomiaHora.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							try
							{
								MixpanelAPI mixPanel = MixpanelAPI.getInstance(DetailActivity.this, Constants.MIXPANEL_TOKEN);
								JSONObject props = new JSONObject();
								props.put("IdEstabelecimento", "" + store.covenantId);
								props.put("IdCategoria", "" + store.category.catId);
								props.put("NomeCategoria", "" + store.category.name);
								props.put("Cidade", "" + store.address.city);
								props.put("Bairro", "" + store.address.neighborhood);
								props.put("Nome", "" + store.name);
								props.put("Distancia", "" + DetailActivity.distance);
								mixPanel.track("EconomiaHora", props);
							}
							catch (Exception e)
							{

							}
							llEconometro.setVisibility(View.VISIBLE);
						}
					});

					// preenche o LinearLayout com as economias por hora
					LayoutInflater li = LayoutInflater.from(DetailActivity.this);

					for (int i = 0; i < store.discountForecast.size(); i++)
					{
						ZLDiscountForecast df = store.discountForecast.get(i);
						View v = li.inflate(R.layout.item_detail_economia, llListaEconomias, false);
						TextView tvHorarioEconomia = (TextView) v.findViewById(R.id.tvHorarioEconomia);
						TextView tvPercentage = (TextView) v.findViewById(R.id.tvPercentage);
						tvHorarioEconomia.setText(df.day + " das " + df.startTime + " às " + df.stopTime);
						tvPercentage.setText("" + df.discountPercentage + "%");
						llListaEconomias.addView(v);
					}

					// esconde as economias por hora quando clicar no X
					imgvBtCloseEcon.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							llEconometro.setVisibility(View.GONE);
						}
					});

					// coloca mais algumas informações de texto
					tvHorarioStart.setText(store.openingTimes);
					tvTelefone.setText(store.phoneNumber);
					tvEmail.setText(store.contactMail);
					tvSite.setText(store.officialWebsite);

					tvDistAddr.setText(String.format("%.2f km - %s, %s", distance, store.address.street, store.address.number));

					tvDesc.setText(android.text.Html.fromHtml(store.detail));
					// se não tem descrição, esconde o botão de Saiba Mais
					if (Util.stringIsNullEmptyOrWhiteSpace(store.detail))
					{
						tvBtSaibaMais.setVisibility(View.GONE);
					}
					else
					{
						// se clicar em Saiba Mais, mostra o texto
						tvBtSaibaMais.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								llDesc.setVisibility(View.VISIBLE);
							}
						});
					}
					tvDescPopup.setText(android.text.Html.fromHtml(store.detail));

					// se clicar em fechar, esconde o texto de Saiba Mais
					imgvBtCloseDesc.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							llDesc.setVisibility(View.GONE);
							
//							scDetailMain.setVisibility(View.GONE);
//							scDetailMain.setVisibility(View.VISIBLE);
							
							flDetailRoot.setVisibility(View.GONE);
							flDetailRoot.setVisibility(View.VISIBLE);
							flDetailRoot.invalidate();
						}
					});

					// preenche manualmente as formas de pagamento
					if (store.paymentOptions.size() == 0)
					{
						tvCartoesAceitos.setVisibility(View.GONE);
					}
					else
					{
						// primeiro separa os tipos de forma de pagamento,
						// agrupando-ás por tipo em um HashMap, onde a chave é o
						// tipo,
						// e o valor é uma lista de formas de pagamento do mesmo
						// tipo
						// ao mesmo tempo preenche um array com as listas de
						// tipos
						HashMap<String, ArrayList<ZLPaymentOption>> paymentTypes = new HashMap<String, ArrayList<ZLPaymentOption>>();
						ArrayList<String> typesList = new ArrayList<String>();
						for (int i = 0; i < store.paymentOptions.size(); i++)
						{
							String tp = store.paymentOptions.get(i).poType;
							if (!paymentTypes.containsKey(tp))
							{
								ArrayList<ZLPaymentOption> opts = new ArrayList<ZLPaymentOption>();
								paymentTypes.put(tp, opts);
								opts.add(store.paymentOptions.get(i));
								typesList.add(tp);
							}
							else
							{
								paymentTypes.get(tp).add(store.paymentOptions.get(i));
							}
						}

						// para cada tipo de forma de pagamento
						for (int tpIndex = 0; tpIndex < typesList.size(); tpIndex++)
						{
							// pega o tipo e a sua lista
							String type = typesList.get(tpIndex);
							ArrayList<ZLPaymentOption> opts = paymentTypes.get(type);
							if (opts != null)
							{
								// adiciona o cabeçalho com o nome do tipo da
								// forma de pagamento
								TextView headerTv = (TextView) li.inflate(R.layout.item_detail_forma_pagamento_header, llFormasDePagamento, false);
								headerTv.setText(type);
								llFormasDePagamento.addView(headerTv);
								FrameLayout flFormasGroup = (FrameLayout) li.inflate(R.layout.item_detail_forma_pagamento, llFormasDePagamento, false);
								llFormasDePagamento.addView(flFormasGroup);

								// agora preenche com os ícones das formas de
								// pagamento
								int screenWidth = llFormasDePagamento.getWidth();
								int itemWidth = screenWidth / 6;
								// utiliza um adapter para gerar as views com as
								// imagens
								DetailPaymentMethodAdapter ad = new DetailPaymentMethodAdapter(DetailActivity.this, opts);
								for (int fpIndex = 0; fpIndex < opts.size(); fpIndex++)
								{
									View v = ad.getView(fpIndex, null, flFormasGroup);
									android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(itemWidth, itemWidth);
									int col = fpIndex % 6;
									int row = fpIndex / 6;
									params.leftMargin = col * itemWidth;
									params.topMargin = row * itemWidth;
									params.gravity = Gravity.TOP | Gravity.LEFT;
									int pad = (int) Util.convertDpToPixel(10, DetailActivity.this);
									v.setPadding(pad, pad, pad, pad);
									v.setLayoutParams(params);

									flFormasGroup.addView(v);
								}
							}
						}
					}

					if (store.availableServices.size() == 0)
					{
						tvOEstabOferece.setVisibility(View.GONE);
					}
					else
					{
						// Agora lista os serviços disponíveis
						for (int i = 0; i < store.availableServices.size(); i++)
						{
							ZLAvailableService service = store.availableServices.get(i);

							View v = li.inflate(R.layout.item_detail_available_service, llServicosOferecidos, false);

							final ImageView imgv = (ImageView) v.findViewById(R.id.imgvServiceIcon);
							TextView tvServiceName = (TextView) v.findViewById(R.id.tvServiceName);

							if (Util.stringIsNullEmptyOrWhiteSpace(service.description))
							{
								tvServiceName.setText(service.name);
							}
							else
							{
								tvServiceName.setText(service.name + ":" + service.description);
							}

							MyImageDownloader.getInstance(DetailActivity.this).getImage(service.iconUrl, "service " + service.name + ".png", new GetImageResult()
							{
								@Override
								public void imageReceived(String url, String filename, Bitmap bm)
								{
									imgv.setImageBitmap(bm);
								}
							});

							llServicosOferecidos.addView(v);
						}
					}
				}
			}
		});

		tvTelefone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone_no = tvTelefone.getText().toString().replaceAll("[^a-zA-Z0-9]+", "");
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:" + phone_no));
				callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(callIntent);
			}
		});

		tvEmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] email = new String[1];

				email[0] = tvEmail.getText().toString();

				Intent emailintent = new Intent(android.content.Intent.ACTION_SEND);
				emailintent.setType("plain/text");
				emailintent.putExtra(android.content.Intent.EXTRA_EMAIL, email);
				emailintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contato Zolkin");
				emailintent.putExtra(android.content.Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(emailintent, "Enviar email por:"));
			}
		});

		tvSite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent webintent = new Intent(android.content.Intent.ACTION_VIEW);
				webintent.setData(Uri.parse(tvSite.getText().toString()));
				startActivity(Intent.createChooser(webintent, "Abrir Site com:"));
			}
		});
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
	public void onBackPressed()
	{
		
		
		if (wvStoreSite.getVisibility() == View.VISIBLE)
		{
			wvStoreSite.setVisibility(View.GONE);
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mapView.onResume();
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
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();

		mapView.onLowMemory();
	}
}
