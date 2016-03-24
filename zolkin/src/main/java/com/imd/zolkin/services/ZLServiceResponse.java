package com.imd.zolkin.services;

//resposta padrão de um serviço do Zolkin
//se o errorMessage não for nulo, houve um erro
//se for nulo, o serviço terminou com sucesso e o resultado esta em serviceResponse

public class ZLServiceResponse<K>
{
	public String errorMessage;
	public K serviceResponse;
	
	public ZLServiceResponse()
	{
		serviceResponse = null;
		errorMessage = null;
	}
	
	public ZLServiceResponse(K srvResponse, String errMsg)
	{
		serviceResponse = srvResponse;
		errorMessage = errMsg;
	}
}
