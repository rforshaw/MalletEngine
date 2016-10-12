package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;

/**
	Makes a request to receive a DrawDelegate from the 
	active Rendering System. addDraws() will be called 
	once delegate has been provided.
*/
public abstract class UIListener extends BaseListener
{
	private DrawDelegate delegate = null ;

	@Override
	public void setParent( final UIElement _parent )
	{
		super.setParent( _parent ) ;
		_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate _delegate )
			{
				if( delegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					delegate.shutdown() ;
				}

				delegate = _delegate ;
				addDraws( delegate ) ;
			}
		} ) ) ;

		constructDraws() ;
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	public abstract void constructDraws() ;

	/**
		Called when listener receives a valid DrawDelegate.
	*/
	public abstract void addDraws( final DrawDelegate _delegate ) ;

	/**
		Returns valid DrawDelegate, return null if no 
		Render System has yet to respond to DrawDelgate request.
	*/
	public DrawDelegate getDrawDelegate()
	{
		return delegate ;
	}

	/**
		Clean up any resources allocated to the listener.
	*/
	@Override
	public void shutdown()
	{
		if( delegate != null )
		{
			delegate.shutdown() ;
		}
	}
}
