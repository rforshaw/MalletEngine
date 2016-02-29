package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.* ;

public abstract class BaseListener
{
	private UIElement parent ;

	public void setParent( final UIElement _parent )
	{
		parent = _parent ;
	}

	public void sendEvent( final Event<?> _event )
	{
		if( parent != null )
		{
			parent.addEvent( _event ) ;
		}
	}
}
