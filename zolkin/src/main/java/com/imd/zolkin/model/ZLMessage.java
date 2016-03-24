package com.imd.zolkin.model;

import org.json.JSONObject;

public class ZLMessage
{
	public enum ZLMessageType
	{
		ZLMessageTypeRead,
		ZLMessageTypeUnread,
		ZLMessageTypeArchived,
		ZLMessageTypeNew
	};
	
	public final String ZL_MESSAGE_TYPE_NEW = "NEW";
	public final String ZL_MESSAGE_TYPE_READ = "READ";
	public final String ZL_MESSAGE_TYPE_UNREAD = "UNREAD";
	public final String ZL_MESSAGE_TYPE_ARCHIVED = "STORED";
	
	
	public String messageID, subject, desc, dateString, message, statusString, attachment, attachmentValue, covenantID;
	public String nomePesquisa, idPesquisa;
	public boolean answered;
	public ZLMessageType status;

	public ZLMessage(JSONObject jo)
	{
		try
		{
			answered = jo.getBoolean("answered");
		}
		catch (Exception e)
		{
			answered = false;
		}

		try
		{
			messageID = jo.getString("id");
		}
		catch (Exception e)
		{
			messageID = "";
		}
		try
		{
			subject = jo.getString("subject");
		}
		catch (Exception e)
		{
			subject = "";
		}
		try
		{
			desc = jo.getString("description");
		}
		catch (Exception e)
		{
			desc = "";
		}
		try
		{
			dateString = jo.getString("date");
		}
		catch (Exception e)
		{
			dateString = "";
		}
		try
		{
			message = jo.getString("message");
		}
		catch (Exception e)
		{
			message = "";
		}
		try
		{
			statusString = jo.getString("status");
		}
		catch (Exception e)
		{
			statusString = "";
		}
		try
		{
			attachment = jo.getString("attachment");
		}
		catch (Exception e)
		{
			attachment = "";
		}
		try
		{
			attachmentValue = jo.getString("attachmentValue");
		}
		catch (Exception e)
		{
			attachmentValue = "";
		}
		try
		{
			covenantID = jo.getString("covenantId");
		}
		catch (Exception e)
		{
			covenantID = "";
		}
		
		if (statusString.equals(ZL_MESSAGE_TYPE_ARCHIVED))
	    {
	        status = ZLMessageType.ZLMessageTypeArchived;
	    }
	    else if (statusString.equals(ZL_MESSAGE_TYPE_UNREAD))
	    {
	        status = ZLMessageType.ZLMessageTypeUnread;
	    }
	    else if (statusString.equals(ZL_MESSAGE_TYPE_NEW))
	    {
	        status = ZLMessageType.ZLMessageTypeNew;
	    }
	    else 
	    {
	        status = ZLMessageType.ZLMessageTypeRead;
	    }	
	    	

	    if (attachment.equals("SURVEY"))
	    {
	    	String str = attachmentValue.replace("Pesquisa [", "").replace("]",	"");
	    	String[] comps1 = str.split(",");
	    	idPesquisa = comps1[0].replace("id=", "");
	    	nomePesquisa = comps1[1].replace("nome=", "");
	    }
	    else
	    {
	        idPesquisa = null;
	        nomePesquisa = null;
	    }
	}
}
