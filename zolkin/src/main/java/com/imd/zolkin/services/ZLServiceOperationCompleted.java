package com.imd.zolkin.services;

//interface helper para os serviços do zolkin

public interface ZLServiceOperationCompleted<K>
{
	public void operationCompleted(ZLServiceResponse<K> response);
}
