package com.linxonline.mallet.renderer ;

public sealed interface ICompatibleBuffer permits
	IManageCompatible,
	ABuffer
{
	public  int getOrder() ;

	public IMeta setMeta( final IMeta _meta ) ;
	public IMeta getMeta() ;

	public int index() ;
}
