package com.imd.zolkin.model;

public class ZLPendingAction
{
	public String key, param;
	
	public final String ACTION_TYPE_SEARCH = "busca";
	public final String ACTION_TYPE_COVENANT = "estabelecimento";
	public final String ACTION_TYPE_MESSAGE = "mensagem";
	public final String ACTION_TYPE_SURVEY = "pesquisa";
	public final String ACTION_TYPE_LOCATION = "regiao";
	public final String ACTION_TYPE_EXTRACT = "extrato";
	
	public enum ZLPendingActionType
	{
		ZLPendingActionTypeSearch,
		ZLPendingActionTypeCovenant,
		ZLPendingActionTypeMessage,
		ZLPendingActionTypeSurvey,
		ZLPendingActionTypeLocation,
		ZLPendingActionTypeExtract
	};
	
	public ZLPendingActionType actionType;
	
	public ZLPendingAction(String k, String p)
	{
		key = k;
		param = p;
		
		if (key.equals("estabelecimento"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeCovenant;
		}
		else if (key.equals("pesquisa"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeSurvey;
		}
		else if (key.equals("busca"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeSearch;
		}
		else if (key.equals("mensagem"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeMessage;
		}
		else if (key.equals("regiao"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeLocation;
		} else if (key.equals("extrato"))
		{
			actionType = ZLPendingActionType.ZLPendingActionTypeExtract;
		}
	}
	
	
}
