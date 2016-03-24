package com.imd.zolkin.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by gabrielmackoz on 24/09/13.
 */
public class ReadAndWriteObjectToFile
{
	public static Boolean writeObjectToFile(File f, Object o)
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(o);
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			Log.d("writeObjectToFile", e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static Object readObjectFromFile(File f)
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			Object o = in.readObject();
			in.close();
			return o;
		}
		catch (Exception i)
		{
			i.printStackTrace();
			return null;
		}
	}
	/*
	 * public static UnidadesArray readUnidadesArrayFromFile(File f){ try{
	 * ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
	 * UnidadesArray o = (UnidadesArray) in.readObject();
	 * Log.d("readUnidadesArrayFromFile", " " + o.getArrayUnidades());
	 * in.close(); return o; }catch(Exception e){ Log.d("writeObjectToFile",
	 * e.toString()); return null; } }
	 */
}
