package com.linxonline.mallet.event ;

public interface IIntercept
{
	public boolean allow( final Event<?> _event ) ;
}
