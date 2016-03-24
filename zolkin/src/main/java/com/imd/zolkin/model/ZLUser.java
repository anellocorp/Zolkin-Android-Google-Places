package com.imd.zolkin.model;

//um usuário do Zolkin

//esta classe também mantém se e qual usuário esta logado

import android.content.Context;
import android.content.SharedPreferences;

import com.imd.zolkin.activity.BaseZolkinActivity;
import com.imd.zolkin.util.Constants;
import com.imd.zolkin.util.ReadAndWriteObjectToFile;
import com.imd.zolkin.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ZLUser implements Serializable
{
    private static final long serialVersionUID = 4447304916957319112L;

    //dados do usuário
    public int userId;
    public double kinBalance, accumulatedEconomy;
    public String name, lastName, email, document, zipCode, birthDate, mobileNumber, password, sexo;
    public String facebookToken = null;
    public ZLAddress address;


    //extrato do usuário é mantido em cache aqui após entrar pela primeira vez na tela de extrato
    public ArrayList<ZLExtractEntry> extract, overcomeExtract;


    //IMPORTANTE: o usuário atualmente logado é salvo nesta variável
    private static ZLUser loggedUser = null;

    // usuário e senha padrões. Usados nas chamdas de serviço quando não tem um
    // usuário logado
    public static final String defaultUser = "apimobile@voucherplanet.com.br";
    public static final String defaultPassword = "1349c8b5cab58248222bcb32e0865e94";

    // para saber se o usuário esta autenticado, verificamos se o seu email é o
    // padrão
    public boolean isAuthenticated()
    {
        return !email.equals(defaultUser);
    }

    // faz o logout
    public static void logout()
    {
        // apaga o usuário salvo em disco
        SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("login");
        editor.remove("facebookToken");
        editor.remove("pass");
        editor.apply();

        File uf = new File(BaseZolkinActivity.latestActivity.getFilesDir(), "LoggedUser");

        if (uf.exists())
            uf.delete();

        loggedUser = null;

        clearSharedPreferences(BaseZolkinActivity.latestActivity.getBaseContext());


    }

    // adiciona os dados de extrato KINs ao usuário
    public void setExtractData(JSONObject jo)
    {
        try
        {
            kinBalance = jo.getDouble("balance");
        }
        catch (Exception e)
        {
            kinBalance = 0;
        }
        try
        {
            accumulatedEconomy = jo.getDouble("accumulatedEconomy");
        }
        catch (Exception e)
        {
            accumulatedEconomy = 0;
        }

        try
        {
            extract = new ArrayList<ZLExtractEntry>();

            JSONArray ja = jo.getJSONArray("extract");
            for (int i = 0; i < ja.length(); i++)
            {
                JSONObject joExtr = ja.getJSONObject(i);
                ZLExtractEntry entry = new ZLExtractEntry(joExtr);
                extract.add(entry);
            }

            // sort
            Collections.sort(extract, new Comparator<ZLExtractEntry>()
            {

                @Override
                public int compare(ZLExtractEntry lhs, ZLExtractEntry rhs)
                {
                    return lhs.date.compareTo(rhs.date) * -1;
                }

            });
        }
        catch (Exception e)
        {

        }
    }

    // adiciona os dados de extrato a vencer ao usuário
    public void setOvercomeExtractData(JSONObject jo)
    {
        try
        {
            kinBalance = jo.getDouble("balance");
        }
        catch (Exception e)
        {
            kinBalance = 0;
        }
        try
        {
            accumulatedEconomy = jo.getDouble("accumulatedEconomy");
        }
        catch (Exception e)
        {
            accumulatedEconomy = 0;
        }

        try
        {
            overcomeExtract = new ArrayList<ZLExtractEntry>();

            JSONArray ja = jo.getJSONArray("extract");
            for (int i = 0; i < ja.length(); i++)
            {
                JSONObject joExtr = ja.getJSONObject(i);
                ZLExtractEntry entry = new ZLExtractEntry(joExtr);
                overcomeExtract.add(entry);
            }

            // sort
            Collections.sort(overcomeExtract, new Comparator<ZLExtractEntry>()
            {

                @Override
                public int compare(ZLExtractEntry lhs, ZLExtractEntry rhs)
                {
                    if (Util.stringIsNullEmptyOrWhiteSpace(lhs.date))
                    {
                        return 1;
                    }
                    if (Util.stringIsNullEmptyOrWhiteSpace(rhs.date))
                    {
                        return -1;
                    }
                    return lhs.date.compareTo(rhs.date) /* * -1 */;
                }

            });

            double balance = (double) ZLUser.getLoggedUser().kinBalance;
            for (int i = 0; i < overcomeExtract.size(); i++)
            {
                ZLExtractEntry entry = overcomeExtract.get(i);
                balance -= entry.value;
                entry.saldo = balance;
            }
        }
        catch (Exception e)
        {

        }
    }

    // atualiza o usuário com um novo json
    public void setData(JSONObject jo)
    {
        try
        {
            userId = jo.getInt("id");
        }
        catch (Exception e)
        {
            userId = 0;
        }
        try
        {
            kinBalance = jo.getDouble("vipCoinBalance");
        }
        catch (Exception e)
        {
            kinBalance = 0;
        }
        try
        {
            accumulatedEconomy = jo.getDouble("accumulatedEconomy");
        }
        catch (Exception e)
        {
            accumulatedEconomy = 0;
        }
        try
        {
            name = jo.getString("name");
        }
        catch (Exception e)
        {
            name = "";
        }
        try
        {
            lastName = jo.getString("lastName");
        }
        catch (Exception e)
        {
            lastName = "";
        }
        try
        {
            sexo = jo.getString("sexo");
        }
        catch (Exception e)
        {
            sexo = "M";
        }
        try
        {
            email = jo.getString("mail");
        }
        catch (Exception e)
        {
            email = "";
        }
        try
        {
            document = jo.getString("document");
        }
        catch (Exception e)
        {
            document = "";
        }
        try
        {
            zipCode = jo.getString("zipCode");
        }
        catch (Exception e)
        {
            zipCode = "";
        }
        try
        {
            birthDate = jo.getString("birthDate");
        }
        catch (Exception e)
        {
            birthDate = "";
        }
        try
        {
            mobileNumber = jo.getString("mobileNumber");
        }
        catch (Exception e)
        {
            mobileNumber = "";
        }
        try
        {
            JSONObject joAddr = jo.getJSONObject("address");
            address = new ZLAddress(joAddr);
        }
        catch (Exception e)
        {
            address = new ZLAddress(null);
        }
    }

    // construtor
    public ZLUser(JSONObject jo)
    {
        extract = new ArrayList<ZLExtractEntry>();
        setData(jo);
    }

    // seta o usuário logado
    public static void setLoggedUser(ZLUser user)
    {
        // salva em disco o novo usuário logado
        loggedUser = user;
        SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("login", user.email);
        if (user.facebookToken != null)
        {
            editor.putString("facebookToken", user.facebookToken);
            editor.remove("pass");
        }
        else if (user.password != null)
        {
            editor.putString("pass", user.password);
            editor.remove("facebookToken");
        }
        editor.apply();

        File uf = new File(BaseZolkinActivity.latestActivity.getFilesDir(), "LoggedUser");

        ReadAndWriteObjectToFile.writeObjectToFile(uf, loggedUser);
    }

    // pega o usuário logado
    public static ZLUser getLoggedUser()
    {
        if (loggedUser != null && !Util.stringIsNullEmptyOrWhiteSpace(loggedUser.name))
            return loggedUser;

        try {

            File uf = new File(BaseZolkinActivity.latestActivity.getFilesDir(), "LoggedUser");
            if (uf.exists()) {
                loggedUser = (ZLUser) ReadAndWriteObjectToFile.readObjectFromFile(uf);
                if (loggedUser != null)
                    return loggedUser;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            SharedPreferences prefs = BaseZolkinActivity.latestActivity.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
            if (prefs.contains("login"))
            {
                ZLUser u = new ZLUser(null);
                u.email = prefs.getString("login", null);
                if (prefs.contains("facebookToken"))
                {
                    u.facebookToken = prefs.getString("facebookToken", null);
                }
                if (prefs.contains("pass"))
                {
                    u.password = prefs.getString("pass", null);
                }
                loggedUser = u;

                if (!Util.stringIsNullEmptyOrWhiteSpace(u.email))
                {
                    return loggedUser;
                }

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // if not user in cache, create user with default user and pass
        ZLUser u = new ZLUser(null);
        u.email = ZLUser.defaultUser;
        u.password = ZLUser.defaultPassword;
        loggedUser = u;
        return u;

    }

    public static void clearSharedPreferences(Context ctx){
        File dir = new File(ctx.getFilesDir().getParent() + "/shared_prefs/");
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            ctx.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
        }
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        for (int i = 0; i < children.length; i++) {
            new File(dir, children[i]).delete();
        }
    }
}
