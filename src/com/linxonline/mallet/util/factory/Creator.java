package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

public abstract class Creator implements CreatorInterface
{
	protected String type = null ;

	@Override
	public abstract Object create( final Settings _setting ) ;

	public final void setType( final String _type )
	{
		type = _type ;
	}

	public final String getType()
	{
		return type ;
	}
}

