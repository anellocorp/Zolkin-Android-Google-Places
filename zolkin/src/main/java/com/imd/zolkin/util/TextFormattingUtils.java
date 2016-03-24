package com.imd.zolkin.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.InputMismatchException;

public class TextFormattingUtils
{
	static TextWatcher twCpf;
	private static TextWatcher twTelCelular;
	private static TextWatcher twCep;

	public static void setCpfFormatter(final EditText et)
	{
		//if (twCpf == null)
		//{
			twCpf = new TextWatcher()
			{

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					et.removeTextChangedListener(twCpf);
					// TODO Auto-generated method stub
					String str = s.toString();
					str = str.replaceAll("[^\\d]", "");
					if (str.length() > 11)
						str = str.substring(0, 11);
					String dst = "";
					for (int i = 0; i < str.length(); i++)
					{
						if (i == 3 || i == 6)
						{
							dst += ".";
						}
						if (i == 9)
						{
							dst += "-";
						}
						dst += str.charAt(i);

					}
					et.setText(dst);
					et.setSelection(dst.length());
					et.addTextChangedListener(twCpf);
				}

			};
		//}
		et.addTextChangedListener(twCpf);
	}
	
	public static String formatCpf(String cpf)
	{
		String str = cpf;
		str = str.replaceAll("[^\\d]", "");
		if (str.length() > 11)
			str = str.substring(0, 11);
		String dst = "";
		for (int i = 0; i < str.length(); i++)
		{
			if (i == 3 || i == 6)
			{
				dst += ".";
			}
			if (i == 9)
			{
				dst += "-";
			}
			dst += str.charAt(i);

		}
		return dst;
	}

	public static String formatCep(String cep)
	{
		String str = cep;
		str = str.replaceAll("[^\\d]", "");
		if (str.length() > 8)
			str = str.substring(0, 8);
		String dst = "";
		for (int i = 0; i < str.length(); i++)
		{
			if (i == 5)
			{
				dst += "-";
			}
			dst += str.charAt(i);
		}
		
		return dst;
	}
	public static void setCepFormatter(final EditText et)
	{
		//if (twCep == null)
		//{
			twCep = new TextWatcher()
			{

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					et.removeTextChangedListener(twCep);
					// TODO Auto-generated method stub
					String str = s.toString();
					str = str.replaceAll("[^\\d]", "");
					if (str.length() > 8)
						str = str.substring(0, 8);
					String dst = "";
					for (int i = 0; i < str.length(); i++)
					{
						if (i == 5)
						{
							dst += "-";
						}
						dst += str.charAt(i);
					}
					et.setText(dst);
					et.setSelection(dst.length());
					et.addTextChangedListener(twCep);
				}

			};
		//}
		et.addTextChangedListener(twCep);
	}
	
	public static String formatCelular(String cell)
	{
		String str = cell;
		str = str.replaceAll("[^\\d.]", "");
		if (str.length() > 11)
			str = str.substring(0, 11);
		String dst = str.length() > 0 ? "(" : "";
		for (int i = 0; i < str.length(); i++)
		{
			if (i == 2)
			{
				dst += ") ";
			}
			if (i == 7)
			{
				dst += "-";
			}
			dst += str.charAt(i);

		}
		return dst;
	}

	public static void setCelularFormatter(final EditText et)
	{
//		if (twTelCelular == null)
//		{
			twTelCelular = new TextWatcher()
			{

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					et.removeTextChangedListener(twTelCelular);
					// TODO Auto-generated method stub
					String str = s.toString();
					str = str.replaceAll("[^\\d.]", "");
					if (str.length() > 11)
						str = str.substring(0, 11);
					String dst = str.length() > 0 ? "(" : "";
					for (int i = 0; i < str.length(); i++)
					{
						if (i == 2)
						{
							dst += ") ";
						}
						if (i == 7)
						{
							dst += "-";
						}
						dst += str.charAt(i);

					}
					et.setText(dst);
					et.setSelection(dst.length());
					et.addTextChangedListener(twTelCelular);
				}
			};
		//}
		et.addTextChangedListener(twTelCelular);
	} // end setCelularFormatter()

	public static boolean isCPF(String CPF)
	{
		CPF = CPF.replace("-", "").replace(".", "").replace(" ", "");
		// considera-se erro CPF's formados por uma sequencia de numeros iguais
		if (CPF.equals("00000000000") || CPF.equals("11111111111") || CPF.equals("22222222222") || CPF.equals("33333333333") || CPF.equals("44444444444") || CPF.equals("55555555555")
				|| CPF.equals("66666666666") || CPF.equals("77777777777") || CPF.equals("88888888888") || CPF.equals("99999999999") || (CPF.length() != 11))
			return (false);

		char dig10, dig11;
		int sm, i, r, num, peso;

		// "try" - protege o codigo para eventuais erros de conversao de tipo
		// (int)
		try
		{
			// Calculo do 1o. Digito Verificador
			sm = 0;
			peso = 10;
			for (i = 0; i < 9; i++)
			{
				// converte o i-esimo caractere do CPF em um numero:
				// por exemplo, transforma o caractere '0' no inteiro 0
				// (48 eh a posicao de '0' na tabela ASCII)
				num = (int) (CPF.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso - 1;
			}

			r = 11 - (sm % 11);
			if ((r == 10) || (r == 11))
				dig10 = '0';
			else
				dig10 = (char) (r + 48); // converte no respectivo caractere
											// numerico

			// Calculo do 2o. Digito Verificador
			sm = 0;
			peso = 11;
			for (i = 0; i < 10; i++)
			{
				num = (int) (CPF.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso - 1;
			}

			r = 11 - (sm % 11);
			if ((r == 10) || (r == 11))
				dig11 = '0';
			else
				dig11 = (char) (r + 48);

			// Verifica se os digitos calculados conferem com os digitos
			// informados.
			if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
				return (true);
			else
				return (false);
		}
		catch (InputMismatchException erro)
		{
			return (false);
		}
	}
}
