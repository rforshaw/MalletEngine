package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.Event ;

public abstract class BaseListener
{
	private UIElement parent ;

	public void setParent( final UIElement _parent )
	{
		parent = _parent ;
	}

	public UIElement getParent()
	{
		return parent ;
	}

	public void sendEvent( final Event<?> _event )
	{
		if( parent != null )
		{
			parent.addEvent( _event ) ;
		}
	}

	/**
		Called when parent UIElement is refreshing itself.
	*/
	public abstract void refresh() ;

	/**
		Called when parent UIElement has been flagged for shutdown.
		Clean up any resources you may have allocated.
	*/
	public abstract void shutdown() ;
}
