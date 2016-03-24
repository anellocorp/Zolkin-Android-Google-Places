package com.imd.zolkin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CriptoUtils {
	private static final String hexDigits = "0123456789abcdef";

	public static byte[] digest(byte[] input, String algoritmo)
		throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algoritmo);
		md.reset();
		return md.digest(input);
	}
  
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer buf = new StringBuffer();
    
		for (int i = 0; i < b.length; i++) {
			int j = ((int) b[i]) & 0xFF; 
			buf.append(hexDigits.charAt(j / 16)); 
			buf.append(hexDigits.charAt(j % 16)); 
		}
	    
		return buf.toString();
	}
  
	public static byte[] hexStringToByteArray(String hexa)
		throws IllegalArgumentException {
      
		//verifica se a String possui uma quantidade par de elementos
		if (hexa.length() % 2 != 0) {
			throw new IllegalArgumentException("String hexa inválida");  
		}
      
		byte[] b = new byte[hexa.length() / 2];
      
		for (int i = 0; i < hexa.length(); i+=2) {
			b[i / 2] = (byte) ((hexDigits.indexOf(hexa.charAt(i)) << 4) |
				(hexDigits.indexOf(hexa.charAt(i + 1))));          
		}
		return b;
	}
	
	public static String stringToMd5(String texto) throws NoSuchAlgorithmException
	{
		byte[] b;
	    b = CriptoUtils.digest(texto.getBytes(), "md5");  
		String senhaCriptografada = CriptoUtils.byteArrayToHexString(b);
		return senhaCriptografada;
	}

	public static String stringToSHA256(String texto) throws NoSuchAlgorithmException
	{
		byte[] b;
	    b = CriptoUtils.digest(texto.getBytes(), "SHA-256");  
		String senhaCriptografada = CriptoUtils.byteArrayToHexString(b);
		return senhaCriptografada;
	}
	
}
