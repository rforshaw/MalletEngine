package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

/**
	Implements setType and getType.
*/
public abstract class Creator<T> implements CreatorInterface<T>
{
	protected String type = null ;

	@Override
	public abstract T create( final Settings _setting ) ;

	public final void setType( final String _type )
	{
		type = _type ;
	}

	public final String getType()
	{
		return type ;
	}
}

