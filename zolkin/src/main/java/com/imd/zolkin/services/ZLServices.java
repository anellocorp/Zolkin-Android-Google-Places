package com.imd.zolkin.services;

//webservices do Zolkin
//todas as chamadas ao webservice do Zolkin estão nesta classe
//para maiores informações, consultar o documento da API do Zolkin,
// disponível na pasta Docs na raiz do projeto Zolkin

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.imd.zolkin.model.ZLCategory;
import com.imd.zolkin.model.ZLCity;
import com.imd.zolkin.model.ZLFaq;
import com.imd.zolkin.model.ZLMessage;
import com.imd.zolkin.model.ZLMessage.ZLMessageType;
import com.imd.zolkin.model.ZLStore;
import com.imd.zolkin.model.ZLSubCategory;
import com.imd.zolkin.model.ZLSurvey;
import com.imd.zolkin.model.ZLUser;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.CriptoUtils;
import com.imd.zolkin.util.GCMUtils;
import com.imd.zolkin.util.ReadAndWriteObjectToFile;
import com.imd.zolkin.util.Util;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ZLServices
{
	public static final String APP_VERSION = "1.0";
	private static final String TAG = ZLServices.class.getSimpleName();

	public static final String BASE_URL_PROD = "http://ws.zolkin.com.br/apimobile/";
	public static final String BASE_URL_DESENV = "http://172.17.0.175:8080/apimobile/";
	public static final String BASE_URL_HOMOLOG_NEW = "http://wshomolog.zolkin.com.br/apimobile/";
	//public static final String BASE_URL_HOMOLOG_OLD = "http://homologmobile.zolkin.com.br/apimobile/";

	public static String BASE_URL = BASE_URL_PROD;

	static ZLServices sharedInstance = null;

	private long timeDiff = 0;

	public static ZLServices getInstance()
	{
		if (sharedInstance == null)
		{
			sharedInstance = new ZLServices();
			sharedInstance.init();
		}

		return sharedInstance;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes)
	{
		try
		{
			char[] hexChars = new char[bytes.length * 2];
			for (int j = 0; j < bytes.length; j++)
			{
				int v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}
		catch (Exception e)
		{
			Log.d("Zolkin", e.toString());
			return null;
		}
	}

	public static String getSha256(String s)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(s.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return bytesToHex(digest);
		}
		catch (Exception e)
		{
			Log.d("Zolkin", e.toString());
			String asd = e.toString();
			Log.d("Zolkin", asd);
			return e.toString();
		}
	}

	public static String getMD5(String s)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return bytesToHex(digest);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static void addAuthParamsDebug(String email, String pass, String timestamp)
	{
		String token = "";

		Log.d("Zolkin-Token", "Pass sha = " + getSha256(pass).toLowerCase());

		token = email + getSha256(pass) + timestamp;
		Log.d("Zolkin-Token", "Concat = " + token);

		String tokenHash = getMD5(token).toLowerCase();
		Log.d("Zolkin-Token", "Concat MD5 = " + tokenHash);
	}

	private void addAuthParams(HashMap<String, Object> params)
	{
		ZLUser u = ZLUser.getLoggedUser();
		addAuthParams(params, u);
	}

	private void addAuthParams(HashMap<String, Object> params, ZLUser u)
	{
		if (u == null)
		{
			u = new ZLUser(null);
			u.email = ZLUser.defaultUser;
			u.password = ZLUser.defaultPassword;
		}

		params.put("userAPI", u.email);

		// Date d = new Date();
		// long unixTime = d.getTime();
		long unixTime = System.currentTimeMillis();
		String timestamp = String.valueOf(unixTime + timeDiff);
		params.put("timestamp", timestamp);

		String token = "";

		if (u.facebookToken != null)
		{
			params.put("facebookToken", u.facebookToken);
		}

		// String sha256pass = getSha256(u.facebookToken != null ?
		// ZLUser.defaultPassword : u.password);
		String sha256pass;
		try
		{
			sha256pass = CriptoUtils.stringToSHA256(u.facebookToken != null ? ZLUser.defaultPassword : u.password);
		}
		catch (NoSuchAlgorithmException e)
		{
			sha256pass = "";
			Log.d("Zolkin", "Erro no SHA256: " + e.toString());
		}
		token = u.email + sha256pass.toLowerCase() + timestamp;

		String md5 = "";
		try
		{
			md5 = CriptoUtils.stringToMd5(token);
		}
		catch (NoSuchAlgorithmException e)
		{
			md5 = "";
			Log.d("Zolkin", "Erro no SHA256: " + e.toString());
		}

		// String tokenHash = getMD5(token).toLowerCase();
		String tokenHash = md5.toLowerCase();

		params.put("token", tokenHash);

		Log.d("ZolkinAuth", "Auth:\nTimestamp = " + timestamp);
	}

	public void init()
	{
		String response;
		try
		{
			HashMap<String, Object> params = new HashMap<String, Object>();

			HashMap<String, String> headers = new HashMap<String, String>();

			response = Http.doPostString(BASE_URL + "init", params, headers, Http.CHARSET);

			Log.d("ZolkinAuth", "init() returned " + response);

			JSONObject jo = new JSONObject(response);

			long serverTime = jo.getLong("timestamp");
			// Date d = new Date();
			// long localTime = d.getTime();
			long localTime = System.currentTimeMillis();

			timeDiff = (serverTime - localTime) / 1000;

			Log.d("ZolkinAuth", "Calculated timediff is " + timeDiff);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void login(final String userEmail, final String password, final String facebookToken, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					ZLUser u = new ZLUser(null);
					u.email = userEmail;
					u.password = password;
					u.facebookToken = facebookToken;

					addAuthParams(params, u);

					params.put("mail", userEmail);
					if (facebookToken != null)
					{
						params.put("facebookToken", facebookToken);
					}
					else
					{
						params.put("password", password);
					}

					params.put("systemName", "Android");
					params.put("systemVersion", android.os.Build.VERSION.CODENAME + " - " + android.os.Build.VERSION.RELEASE + " - API: " + android.os.Build.VERSION.SDK_INT);
					params.put("applicationVersion", Constants.appVersion);
					params.put("deviceModel", android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL);
					params.put("deviceToken", "Android" + Secure.getString(c.getContentResolver(),Secure.ANDROID_ID));

					// this already gets put in by addAuthParams()
					// if (u.facebookToken != null)
					// {
					// params.put("facebookToken", facebookToken);
					// }

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "authenticate", params, headers, Http.CHARSET);

					try
					{
						JSONObject joUser = new JSONObject(response);

						if (joUser.has("code"))
						{
							if (joUser.getInt("code") == 100)
							{
								result.errorMessage = "Usuário ou senha inválidos, por favor verifique e tente novamente.";
								return result;
							}
						}
						if (joUser.has("success"))
						{
							if (joUser.getBoolean("success") == false)
							{
								result.errorMessage = "Usuário ou senha inválidos, por favor verifique e tente novamente.";
								return result;
							}
						}
						ZLUser newUser = new ZLUser(joUser);
						newUser.password = password;
						newUser.facebookToken = facebookToken;
						ZLUser.setLoggedUser(newUser);
						result.serviceResponse = true;

						MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).identify(newUser.email);

						MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).getPeople().identify(userEmail);
						MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).getPeople().initPushHandling("20535895839");
						MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).getPeople().set("$email", userEmail);

						try
						{

							MixpanelAPI mixPanel = MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							props.put("UserID", "" + newUser.userId);
							Calendar cal = Calendar.getInstance();
							props.put("Hora", "" + cal.get(Calendar.HOUR_OF_DAY));
							props.put("Latitude", "" + ZLLocationService.getLatitude());
							props.put("Longitude", "" + ZLLocationService.getLongitude());
							props.put("Device", android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL);
							props.put("AppVersion", Constants.appVersion);
							props.put("OS", "Android");
							mixPanel.track("Login", null);

						}
						catch (Exception e)
						{

						}

						GCMUtils.getInstance(c);
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end login

	public void forgotPassword(final String email, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("mail", email.toLowerCase());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "login/forgot", params, headers, Http.CHARSET);

					try
					{
						if (response.startsWith("{"))
						{
							JSONObject jo = new JSONObject(response);

							if (jo.has("success"))
							{
								if (jo.getBoolean("success"))
								{
									result.serviceResponse = true;
									return result;
								}
								else
								{
									result.serviceResponse = false;
									result.errorMessage = jo.getString("message");
								}
							}
						}
						else if (response.startsWith("["))
						{
							JSONArray ja = new JSONArray(response);
							if (ja.length() > 0)
							{
								JSONObject jo = ja.getJSONObject(0);
								if (jo.has("success"))
								{
									if (jo.getBoolean("success"))
									{
										result.serviceResponse = null;
										return result;
									}

								}
								String errMsg = "";
								for (int i = 1; i < ja.length(); i++)
								{
									JSONObject joe = ja.getJSONObject(i);

									errMsg += joe.getString("error") + ", ";
								}
								result.errorMessage = errMsg;
								return result;
							}
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					if (result.errorMessage == null)
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde.";
					if (result.errorMessage.toLowerCase().contains("home.forgotpassword.mail.notfound"))
					{
						result.errorMessage = "E-mail não encontrato. Por favor, verifique e tente novamente.";
					}
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end forgotPassword

	public void show(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/show", params, headers, Http.CHARSET);

					try
					{
						JSONObject joUser = new JSONObject(response);
						ZLUser.getLoggedUser().setData(joUser);
						result.serviceResponse = true;

						try
						{
							ZLUser lu = ZLUser.getLoggedUser();

							MixpanelAPI mixPanel = MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN);
							JSONObject props = new JSONObject();
							props.put("UserID", "" + lu.userId);
							props.put("Nome", lu.name);
							props.put("Sobrenome", lu.lastName);
							if (lu.facebookToken != null)
								props.put("FacebookToken", lu.facebookToken);
							props.put("Email", lu.email);
							props.put("CPF", lu.document);
							if (lu.mobileNumber != null)
								props.put("Celular", lu.mobileNumber);
							props.put("DataNascimento", lu.birthDate);
							if (lu.birthDate != null)
								if (lu.birthDate.length() > 4)
								{
									String ano = lu.birthDate.substring(0, 4);
									props.put("AnoNascimento", ano);
								}
							props.put("CEP", lu.zipCode);
							if (ZLCity.getCurrentCity(c) != null)
								props.put("Cidade", ZLCity.getCurrentCity(c).name);
							props.put("Saldo", lu.kinBalance);
							props.put("EconomiaAcumulada", lu.accumulatedEconomy);
							props.put("OS", "Android");
							props.put("Device", android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL);
							mixPanel.registerSuperProperties(props);

							mixPanel.getPeople().set(props);

							MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).getPeople().set("$first_name", lu.name);
							MixpanelAPI.getInstance(c, Constants.MIXPANEL_TOKEN).getPeople().set("$last_name", lu.lastName);
						}
						catch (Exception ee)
						{
							Log.d("Zolkin", ee.toString());
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end show

	public void createUser(final ZLUser user, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<String> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>()
		{
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_)
			{
				ZLServiceResponse<String> result = new ZLServiceResponse<String>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("city", "");
					params.put("password", user.password);
					params.put("name", user.name);
					params.put("lastName", user.lastName);
					// dd/mm/yyyy
					params.put("birthDate", user.birthDate);
					// xxxxxxxx
					params.put("zipCode", user.zipCode);
					params.put("origin", "android");
					params.put("document", user.document);
					params.put("mail", user.email);
					params.put("geocityId", "");
					params.put("state", "");
					params.put("street", "");
					params.put("neighborhood", "");
					params.put("sexo", user.sexo);
					params.put("requestKinCard", "false");
					params.put("number", "");
					params.put("mobileNumber", user.mobileNumber);
					params.put("additionalDetails", "");
					if (user.facebookToken != null)
						params.put("facebookToken", user.facebookToken);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/create", params, headers, Http.CHARSET);

					try
					{
						if (response.startsWith("{"))
						{
							JSONObject jo = new JSONObject(response);

							if (jo.has("success"))
							{
								if (jo.getBoolean("success"))
								{
									result.serviceResponse = null;
									return result;
								}

							}
						}
						else if (response.startsWith("["))
						{
							JSONArray ja = new JSONArray(response);

							if (ja.length() > 0)
							{
								JSONObject jo = ja.getJSONObject(0);
								if (jo.has("success"))
								{
									if (jo.getBoolean("success"))
									{
										result.serviceResponse = null;
										return result;
									}

								}

								String errMsg = "";
								for (int i = 1; i < ja.length(); i++)
								{
									JSONObject joe = ja.getJSONObject(i);

									errMsg += joe.getString("error") + ", ";
								}
								result.errorMessage = errMsg;
								return result;
							}
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					if (result.errorMessage == null)
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde.";
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end createUser

	public void updateUser(final ZLUser user, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<String> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>()
		{
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_)
			{
				ZLServiceResponse<String> result = new ZLServiceResponse<String>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("id", "" + ZLUser.getLoggedUser().userId);
					params.put("password", user.password);
					params.put("name", user.name);
					params.put("lastName", user.lastName);
					params.put("sexo", user.sexo);
					// dd/mm/yyyy
					params.put("birthDate", user.birthDate);
					// xxxxxxxx
					params.put("zipCode", user.zipCode);
					params.put("document", user.document);
					params.put("mail", user.email);
					params.put("mobileNumber", user.mobileNumber);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/update", params, headers, Http.CHARSET);

					// //DEBUG ONLY/////////////////////
					// HashMap<String, Object> params2 = new HashMap<String,
					// Object>();
					// params2.put("n", user.name);
					// String response2 = Http.doPostString(BASE_URL +
					// "consumer/update", params2, headers, Http.CHARSET);
					// ////////////////////////////////////
					try
					{
						if (response.startsWith("{"))
						{
							JSONObject jo = new JSONObject(response);

							if (jo.has("success"))
							{
								if (jo.getBoolean("success"))
								{
									result.serviceResponse = null;
									return result;
								}

							}
						}
						else if (response.startsWith("["))
						{
							JSONArray ja = new JSONArray(response);
							if (ja.length() > 0)
							{
								JSONObject jo = ja.getJSONObject(0);
								if (jo.has("success"))
								{
									if (jo.getBoolean("success"))
									{
										result.serviceResponse = null;
										return result;
									}

								}

								String errMsg = "";
								for (int i = 1; i < ja.length(); i++)
								{
									JSONObject joe = ja.getJSONObject(i);

									errMsg += joe.getString("error") + ", ";
								}
								result.errorMessage = errMsg;
								return result;
							}
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					if (result.errorMessage == null)
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde.";
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end updateUser

	public void getExtract(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/viewExtract", params, headers, Http.CHARSET);

					try
					{
						JSONObject joExtract = new JSONObject(response);
						ZLUser.getLoggedUser().setExtractData(joExtract);
						result.serviceResponse = true;
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getExtract

	public void getOvercomeExtract(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/viewOvercomeExtract", params, headers, Http.CHARSET);

					try
					{
						JSONObject joExtract = new JSONObject(response);
						ZLUser.getLoggedUser().setOvercomeExtractData(joExtract);
						result.serviceResponse = true;
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getOvercomeExtract

	public void addRemoveFavorite(final int covenantId, final boolean addFavorite, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("covenantId", "" + covenantId);
					params.put("favorite", addFavorite ? "true" : "false");

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/favorite", params, headers, Http.CHARSET);

					try
					{
						if (response.startsWith("{"))
						{
							JSONObject jo = new JSONObject(response);

							if (jo.has("success"))
							{
								if (jo.getBoolean("success"))
								{
									result.serviceResponse = true;
									return result;
								}

							}
						}
						else if (response.startsWith("["))
						{
							JSONArray ja = new JSONArray(response);
							if (ja.length() > 1)
							{
								String errMsg = "";
								for (int i = 1; i < ja.length(); i++)
								{
									JSONObject joe = ja.getJSONObject(i);

									errMsg += joe.getString("error") + ", ";
								}
								result.errorMessage = errMsg;
								return result;
							}
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					if (result.errorMessage == null)
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde.";
					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end addRemoveFavorite

	public void listFavorites(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLStore>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLStore>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLStore>> result = new ZLServiceResponse<List<ZLStore>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/favoriteList", params, headers, Http.CHARSET);

					try
					{
						JSONArray ja = new JSONArray(response);

						result.serviceResponse = new ArrayList<ZLStore>();

						for (int i = 0; i < ja.length(); i++)
						{
							JSONObject jof = ja.getJSONObject(i);
							ZLStore cov = new ZLStore(jof);
							result.serviceResponse.add(cov);
						}

						return result;

					}
					catch (Exception e)
					{
						result.errorMessage = response;

					}

					if (result.errorMessage == null)
					{
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde. " + response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLStore>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end listFavorites

	public void retrieveCity(final Location loc, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					if (loc != null)
					{
						params.put("latitude", "" + loc.getLatitude());
						params.put("longitude", "" + loc.getLongitude());
					}

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveCity", params, headers, Http.CHARSET);

					try
					{
						JSONObject joCity = new JSONObject(response);
						ZLCity.setCurrentCity(new ZLCity(joCity), c);
						result.serviceResponse = true;
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end retrieveCity

	public void availableCities(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLCity>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLCity>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLCity>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLCity>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLCity>> result = new ZLServiceResponse<List<ZLCity>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/availablecities", params, headers, Http.CHARSET);

					try
					{
						JSONArray jac = new JSONArray(response);

						result.serviceResponse = new ArrayList<ZLCity>();

						for (int i = 0; i < jac.length(); i++)
						{
							JSONObject joc = jac.getJSONObject(i);
							ZLCity city = new ZLCity(joc);
							result.serviceResponse.add(city);
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLCity>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end availableCities

	public void getNeighborhoodFilters(final ZLCity city, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<String>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<String>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<String>>>()
		{
			@Override
			protected ZLServiceResponse<List<String>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<String>> result = new ZLServiceResponse<List<String>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("cityId", "" + city.id);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveNeighborhoodFilters", params, headers, Http.CHARSET);

					try
					{
						JSONObject jo = new JSONObject(response);
						JSONArray jac = jo.getJSONArray("data");

						result.serviceResponse = new ArrayList<String>();

						for (int i = 0; i < jac.length(); i++)
						{
							String n = jac.getString(i);
							result.serviceResponse.add(n);
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<String>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getNeighborhoodFilters

	public void getCategoryFilters(final ZLCity city, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLCategory>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLCategory>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLCategory>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLCategory>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLCategory>> result = new ZLServiceResponse<List<ZLCategory>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("cityId", "" + city.id);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveCategoryFilters", params, headers, Http.CHARSET);

					try
					{
						JSONObject jo = new JSONObject(response);
						JSONArray jac = jo.getJSONArray("data");

						result.serviceResponse = new ArrayList<ZLCategory>();

						for (int i = 0; i < jac.length(); i++)
						{
							JSONObject joc = jac.getJSONObject(i);
							ZLCategory cat = new ZLCategory(joc);
							result.serviceResponse.add(cat);
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLCategory>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getCategoryFilters

	public void getSubCategoryFilters(final ZLCity city, final ZLCategory category, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLSubCategory>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLSubCategory>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLSubCategory>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLSubCategory>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLSubCategory>> result = new ZLServiceResponse<List<ZLSubCategory>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("cityId", "" + city.id);
					params.put("categoryId", "" + city.id);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveSubcategoryFilters", params, headers, Http.CHARSET);

					try
					{
						JSONObject jo = new JSONObject(response);
						JSONArray jac = jo.getJSONArray("data");

						result.serviceResponse = new ArrayList<ZLSubCategory>();

						for (int i = 0; i < jac.length(); i++)
						{
							JSONObject joc = jac.getJSONObject(i);
							ZLSubCategory cat = new ZLSubCategory(joc);
							result.serviceResponse.add(cat);
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLSubCategory>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getSubCategoryFilters

	public void getCovenantDetails(final int covenantID, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<ZLStore> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<ZLStore>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<ZLStore>>()
		{
			@Override
			protected ZLServiceResponse<ZLStore> doInBackground(Void... params_)
			{
				ZLServiceResponse<ZLStore> result = new ZLServiceResponse<ZLStore>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("latitude", "" + ZLLocationService.getLatitude());
					params.put("longitude", "" + ZLLocationService.getLongitude());

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/showDetails/" + covenantID, params, headers, Http.CHARSET);

					try
					{
						JSONObject joCov = new JSONObject(response);

						if (joCov.has("id"))
						{
							result.serviceResponse = new ZLStore(joCov);
							// result.serviceResponse = covenant;
						}
						else
						{
							result.errorMessage = response;
						}
					}
					catch (Exception e)
					{
						result.errorMessage = response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<ZLStore> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end retrieveCity

	public void getCovenantsByLocation(final double lat, final double lon, final boolean paginate, final int max, final int offset, final int cityId, final boolean showLoading, final Context c,
			final ZLServiceOperationCompleted<List<ZLStore>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLStore>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLStore>> result = new ZLServiceResponse<List<ZLStore>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("latitude", "" + lat);
					params.put("longitude", "" + lon);
					params.put("paginatedResult", paginate ? "true" : "false");
					params.put("max", "" + max);
					params.put("offset", "" + offset);
					params.put("cityId", "" + cityId);
					params.put("newVersion", "true");

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveCovenantsByLocations", params, headers, Http.CHARSET);

					try
					{
						JSONObject jo = new JSONObject(response);
						JSONArray ja = jo.getJSONArray("data");

						result.serviceResponse = new ArrayList<ZLStore>();

						for (int i = 0; i < ja.length(); i++)
						{
							JSONObject jof = ja.getJSONObject(i);
							ZLStore cov = new ZLStore(jof);
							result.serviceResponse.add(cov);
						}

						return result;

					}
					catch (Exception e)
					{
						result.errorMessage = response;

					}

					if (result.errorMessage == null)
					{
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde. " + response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLStore>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getCovenantsByLocation

	public void listCovenants(final String lat, final String lon, final boolean paginate, final int max, final int offset, final int cityId, final boolean openCovenantsOnly, final String catIds,
			final String subCatIds, final String neighborhoods, final String text, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLStore>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLStore>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLStore>> result = new ZLServiceResponse<List<ZLStore>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("paginatedResult", paginate ? "true" : "false");
					params.put("max", "" + max);
					params.put("offset", "" + offset);
					// params.put("cityId", "" + cityId);
					params.put("newVersion", "true");

					if (lat != null && lon != null)
					{
						params.put("latitude", "" + lat);
						params.put("longitude", "" + lon);
					}

					params.put("openedCovenants", openCovenantsOnly ? "true" : "false");

					if (catIds != null)
					{
						params.put("category", catIds);
					}
					if (subCatIds != null)
					{
						params.put("subCategoryId", subCatIds);
					}
					if (neighborhoods != null)
					{
						params.put("neighborhood", neighborhoods);
					}
					if (text != null)
					{
						params.put("text", text);
					}

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/list", params, headers, Http.CHARSET);

					try
					{
						JSONArray ja = new JSONArray(response);

						result.serviceResponse = new ArrayList<ZLStore>();

						for (int i = 0; i < ja.length(); i++)
						{
							JSONObject jof = ja.getJSONObject(i);
							ZLStore cov = new ZLStore(jof);
							result.serviceResponse.add(cov);
						}

						if (text != null)
						{
							ZLListCache.getInstance().addTerm(text, response);
						}

						return result;

					}
					catch (Exception e)
					{
						try
						{
							JSONObject jo = new JSONObject(response);
							JSONArray ja = jo.getJSONArray("data");

							result.serviceResponse = new ArrayList<ZLStore>();
							int cnt = ja.length();
							Log.d("Zolkin", "CNT = " + cnt);
							for (int i = 0; i < cnt; i++)
							{
								JSONObject jof = ja.getJSONObject(i);
								ZLStore cov = new ZLStore(jof);
								result.serviceResponse.add(cov);
								Log.d("Zolkin", "" + i);
							}

							if (text != null)
							{
								ZLListCache.getInstance().addTerm(text, response);
							}

							return result;

						}
						catch (Exception ee)
						{
							result.errorMessage = response;
						}

						result.errorMessage = response;
					}

					if (result.errorMessage == null)
					{
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde. " + response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLStore>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end listCovenants

	public void getFaq(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "faq/faq", params, headers, Http.CHARSET);

					try
					{
						JSONArray jaFaq = new JSONArray(response);
						ZLFaq.faq = new ArrayList<ZLFaq>();
						for (int i = 0; i < jaFaq.length(); i++)
						{
							ZLFaq.faq.add(new ZLFaq(jaFaq.getJSONObject(i)));
						}
						result.serviceResponse = true;
					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getFaq

	public void getMessages(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLMessage>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLMessage>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLMessage>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLMessage>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLMessage>> result = new ZLServiceResponse<List<ZLMessage>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("consumerId", ZLUser.getLoggedUser().userId);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/messages", params, headers, Http.CHARSET);

					try
					{
						JSONArray ja = new JSONArray(response);
						result.serviceResponse = new ArrayList<ZLMessage>();

						for (int i = 0; i < ja.length(); i++)
						{
							ZLMessage m = new ZLMessage(ja.getJSONObject(i));
							result.serviceResponse.add(m);
						}

					}
					catch (Exception e)
					{
						result.errorMessage = response;
						result.serviceResponse = null;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLMessage>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getMessages

	public void changeMessageStatus(final ZLMessage m, final ZLMessageType mt, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("consumerId", ZLUser.getLoggedUser().userId);
					params.put("messageId", m.messageID);

					String status = "";

					switch (mt)
					{
						case ZLMessageTypeRead:
							status = m.ZL_MESSAGE_TYPE_READ;
							break;
						case ZLMessageTypeUnread:
							status = m.ZL_MESSAGE_TYPE_UNREAD;
							break;
						case ZLMessageTypeNew:
							status = m.ZL_MESSAGE_TYPE_NEW;
							break;
						case ZLMessageTypeArchived:
							status = m.ZL_MESSAGE_TYPE_ARCHIVED;
							break;
						default:
							break;
					}

					params.put("status", status);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/messages/changeStatus", params, headers, Http.CHARSET);

					// TODO: test me out
					if (response.length() > 0)
					{
						result.serviceResponse = true;
					}
					else
					{
						result.serviceResponse = false;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end changeMessageStatus

	public void getSurvey(final String messageId, final String surveyId, final String covenantId, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<ZLSurvey> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<ZLSurvey>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<ZLSurvey>>()
		{
			@Override
			protected ZLServiceResponse<ZLSurvey> doInBackground(Void... params_)
			{
				ZLServiceResponse<ZLSurvey> result = new ZLServiceResponse<ZLSurvey>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("consumerId", ZLUser.getLoggedUser().userId);
					params.put("surveyId", surveyId);
					params.put("covenantId", covenantId);
					params.put("messageId", messageId);

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "survey", params, headers, Http.CHARSET);

					try
					{
						JSONObject jo = new JSONObject(response);
						result.serviceResponse = new ZLSurvey(jo, surveyId, covenantId, messageId);
					}
					catch (Exception e)
					{
						result.errorMessage = e.toString();
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<ZLSurvey> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getSurvey

	public void answerSurvey(final ZLMessage m, final ZLSurvey s, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<String> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>()
		{
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_)
			{
				ZLServiceResponse<String> result = new ZLServiceResponse<String>();

				String response;
				try
				{
					HashMap<String, Object> headers = new HashMap<String, Object>();
					addAuthParams(headers);
					headers.put("Content-Type", "application/json");

					JSONObject joAnswers = s.getResult();
					response = Http.doPostStringBody(BASE_URL + "survey/answers", joAnswers.toString(), headers, Http.CHARSET);

					int val = Integer.parseInt(response);

					if (val > 0)
						result.serviceResponse = response;
					else
						result.errorMessage = "Erro - resposta inesperada do servidor: " + response;

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end answerSurvey

	public void commentSurvey(final ZLSurvey s, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("surveyId", s.surveyId);
					params.put("comment", s.commentAnswer == null ? "" : s.commentAnswer);
					params.put("contact", s.commentOther == null ? "" : s.commentOther);
					params.put("phone", s.phoneContact ? "true" : "false");
					params.put("mail", s.mailContact ? "true" : "false");

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "survey/comment", params, headers, Http.CHARSET);

					// TODO: test me

					result.serviceResponse = true;

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end commentSurvey

	public void getNumUnreadMessages(final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Integer> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<Integer>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Integer>>()
		{
			@Override
			protected ZLServiceResponse<Integer> doInBackground(Void... params_)
			{
				ZLServiceResponse<Integer> result = new ZLServiceResponse<Integer>();

				String response;
				try
				{
					HashMap<String, String> headers = new HashMap<String, String>();

					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					params.put("consumerId", "" + ZLUser.getLoggedUser().userId);

					response = Http.doPostString(BASE_URL + "consumer/messages/retrieveUnreadQuantity", params, headers, Http.CHARSET);

					result.serviceResponse = new Integer(response);

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Integer> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end getNumUnreadMessages

	public void sendPushToken(final String token, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<Boolean> callback)
	{

		AsyncTask<Void, Void, ZLServiceResponse<Boolean>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<Boolean>>()
		{
			@Override
			protected ZLServiceResponse<Boolean> doInBackground(Void... params_)
			{
				ZLServiceResponse<Boolean> result = new ZLServiceResponse<Boolean>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params);

					
					params.put("pushToken", token);
					params.put("deviceToken", "Android" + Secure.getString(c.getContentResolver(), Secure.ANDROID_ID));
					params.put("deviceModel", "Android");

					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "consumer/saveDeviceToken", params, headers, Http.CHARSET);

					result.serviceResponse = true;

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<Boolean> result) {
				if (callback != null) {
					callback.operationCompleted(result);
				}
			}
		};

		if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.HONEYCOMB) {
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} else {
			asyncTask.execute((Void[]) null);
		}
	} // end sendPushToken

	public void listCovenantsByLocationId(final String lat, final String lon, final int locationId, final boolean showLoading, final Context c, final ZLServiceOperationCompleted<List<ZLStore>> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<List<ZLStore>>>()
		{
			@Override
			protected ZLServiceResponse<List<ZLStore>> doInBackground(Void... params_)
			{
				ZLServiceResponse<List<ZLStore>> result = new ZLServiceResponse<List<ZLStore>>();

				String response;
				try
				{
					HashMap<String, Object> params = new HashMap<String, Object>();

					addAuthParams(params, ZLUser.getLoggedUser());

					params.put("paginatedResult","false");
					params.put("geoPushRegiaoId", "" + locationId);

					if (lat != null && lon != null)
					{
						params.put("latitude", "" + lat);
						params.put("longitude", "" + lon);
					}


					HashMap<String, String> headers = new HashMap<String, String>();

					response = Http.doPostString(BASE_URL + "covenant/retrieveCovenantsByRegion", params, headers, Http.CHARSET);

					try
					{
						JSONArray ja = new JSONArray(response);

						result.serviceResponse = new ArrayList<ZLStore>();

						for (int i = 0; i < ja.length(); i++)
						{
							JSONObject jof = ja.getJSONObject(i);
							ZLStore cov = new ZLStore(jof);
							result.serviceResponse.add(cov);
						}


						return result;

					}
					catch (Exception e)
					{
						try
						{
							JSONObject jo = new JSONObject(response);
							JSONArray ja = jo.getJSONArray("data");

							result.serviceResponse = new ArrayList<ZLStore>();
							int cnt = ja.length();
							Log.d("Zolkin", "CNT = " + cnt);
							for (int i = 0; i < cnt; i++)
							{
								JSONObject jof = ja.getJSONObject(i);
								ZLStore cov = new ZLStore(jof);
								result.serviceResponse.add(cov);
								Log.d("Zolkin", "" + i);
							}

							return result;

						}
						catch (Exception ee)
						{
							result.errorMessage = response;
						}

						result.errorMessage = response;
					}

					if (result.errorMessage == null)
					{
						result.errorMessage = "Erro no servidor. Por favor, tente novamente mais tarde. " + response;
					}

					return result;

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<List<ZLStore>> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	} // end listCovenantsByLocationId

	public static void sendDataToServer(Context context) {
		final Context c = context;
		final SharedPreferences settings;

		settings = c.getSharedPreferences("Zolkin-Location", Context.MODE_PRIVATE);
		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>() {
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_) {

				ZLServiceResponse<String> result = new ZLServiceResponse<String>();


				try {
					HashMap<String, Object> headers = new HashMap<String, Object>();
					headers.put("Content-Type", "application/json");

					if (settings.contains("activeUser")) {
						headers.put("userapi", settings.getString("activeUser", null));
					}

					JSONObject jo = new JSONObject();
					try {
						jo.put("deviceToken", "Android" + Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID));
						jo.put("latitude", settings.getString("latitude", ""));
						jo.put("longitude", settings.getString("longitude", ""));
						jo.put("pushToken", settings.getString("pushToken", ""));
						jo.put("deviceModel", "ANDROID");
					}
					catch (JSONException e) {
						e.printStackTrace();
					}

					try {
						int responseCode = Http.doPostBodyGetResponseCode(BASE_URL + "geopush/send", jo.toString(), headers);
						Log.d(TAG, "Atualizada a localização codigo HTTP: " + responseCode);

						if (responseCode >= 200 && responseCode < 400) {
							try {
								Log.i(TAG, "Enviando localização e salvando as coordenadas.");
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							Log.d(TAG, "Erro ao enviar as localização " + responseCode);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					result.errorMessage = e.toString();
					e.printStackTrace();
				}
				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
				String Header = ("\nEnviado\n");
				String Content = "Data: " + dateFormat.format(new Date()) + "\n" +
						"Ultima localização salva - Latitude: " +
						settings.getString("latitude", "") + " Longitude: " + settings.getString("longitude", "") + "\n";


				//ZLServices.InsertDataOnFile(Header + Content, c);
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} else {
			asyncTask.execute((Void[]) null);
		}
	}

	public void sendLocation(final boolean showLoading,final Location location ,final Context c, final ZLServiceOperationCompleted<String> callback)
	{
		final ProgressDialog progress;
		if (showLoading)
			progress = ProgressDialog.show(c, Constants.APP_NAME, "Loading...");
		else
			progress = null;

		AsyncTask<Void, Void, ZLServiceResponse<String>> asyncTask = new AsyncTask<Void, Void, ZLServiceResponse<String>>()
		{
			@Override
			protected ZLServiceResponse<String> doInBackground(Void... params_)
			{
				ZLServiceResponse<String> result = new ZLServiceResponse<String>();

				String response;
				try
				{
					HashMap<String, Object> headers = new HashMap<String, Object>();

					headers.put("Content-Type", "application/json");

					if (ZLUser.getLoggedUser().isAuthenticated())
					{
						headers.put("userAPI", ZLUser.getLoggedUser().email);
					}

					JSONObject jo = new JSONObject();
					try
					{
						jo.put("deviceToken", "Android" + Secure.getString(c.getContentResolver(),Secure.ANDROID_ID));
						String pt = GCMUtils.getInstance(c).getRegistrationId();
						if (!Util.stringIsNullEmptyOrWhiteSpace(pt))
							jo.put("pushToken",pt) ;
						else
						{
							File tf = new File(c.getFilesDir(), "zolkinpushtoken.txt");
							if (tf.exists())
							{
								String regid = (String) ReadAndWriteObjectToFile.readObjectFromFile(tf);
								jo.put("pushToken",regid) ;
							}
						}
						jo.put("latitude", location.getLatitude());
						jo.put("longitude", location.getLongitude());
						jo.put("deviceModel", "ANDROID");

					}
					catch (JSONException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						int responseCode = Http.doPostBodyGetResponseCode(ZLServices.BASE_URL + "geopush/send", jo.toString(), headers);

						Log.d("Zolkin", "Update location status code: " + responseCode);


						if (responseCode >= 200 && responseCode < 400)
						{
							//worked, save location
							try
							{
								SharedPreferences universalPreferences = c.getSharedPreferences("universal", Context.MODE_PRIVATE);
								universalPreferences.edit().putString("latitude", String.valueOf(location.getLatitude())).commit();
								universalPreferences.edit().putString("longitude", String.valueOf(location.getLongitude())).commit();

								Toast.makeText(c, "Send position and saved location", Toast.LENGTH_LONG).show();

							}
							catch (Exception e)
							{

							}
						}
						else
						{
							Toast.makeText(c, "Send position error " + responseCode, Toast.LENGTH_LONG).show();
						}
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				catch (Exception e)
				{
					result.errorMessage = e.toString();
					e.printStackTrace();
				}

				return result;
			}

			@Override
			protected void onPostExecute(ZLServiceResponse<String> result)
			{
				if (showLoading)
				{
					try
					{
						progress.cancel();
					}
					catch (Exception e)
					{
					}
				}
				if (callback != null)
				{
					callback.operationCompleted(result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */11)
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		}
		else
		{
			asyncTask.execute((Void[]) null);
		}
	}

	public boolean isAnsweredSurvey(final ZLMessage message) {
		final boolean[] answered = {false};
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();

			addAuthParams(params);

			params.put("consumerId", ZLUser.getLoggedUser().userId);
			params.put("surveyId", message.idPesquisa);
			params.put("covenantId", message.covenantID);
			params.put("messageId", message.messageID);

			HashMap<String, String> headers = new HashMap<String, String>();

			String response = Http.doPostString(BASE_URL + "/survey", params, headers, Http.CHARSET);
			try {
				JSONObject jsonObject = new JSONObject(response);
				answered[0] = jsonObject.getBoolean("answered");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answered[0];
	}

	public boolean asFavorities() {
		final boolean[] have = {false};
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			HashMap<String, String> headers = new HashMap<String, String>();

			addAuthParams(params, ZLUser.getLoggedUser());

			String response = Http.doPostString(BASE_URL + "/consumer/favoriteList", params, headers, Http.CHARSET);

			try {
				JSONArray ja = new JSONArray(response);
				if (ja.length() > 0)
					have[0] = true;


			} catch (JSONException je) {
				je.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return have[0];

	}

	/*private static void InsertDataOnFile(String data, Context context) {
		File file = new File(context.getExternalFilesDir(null), "logFileZolkin.txt");
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.append(data);
			fw.close();
		} catch (IOException e) {
			Log.w("ExternalStorage", "Error writing " + file, e);
		}
	}*/
}

