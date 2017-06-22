package com.linxonline.mallet.ui ;

/**
	Makes a request to receive a DrawDelegate from the 
	active Rendering System. addDraws() will be called 
	once delegate has been provided and the parent is 
	visible, if the parent is invisible removeDraws is 
	called instead.
*/
public abstract class UIListener<T extends UIElement> extends BaseListener<T>
{
	/*@Override
	public void setParent( final T _parent )
	{
		super.setParent( _parent ) ;
		_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				if( delegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					delegate.shutdown() ;
				}

				delegate = _delegate ;
				visible = _parent.isVisible() ;
				if( visible == true )
				{
					addDraws( delegate ) ;
				}
			}
		} ) ) ;

		constructDraws() ;
	}*/



	/*@Override
	public void refresh()
	{
		final T parent = getParent() ;
		if( visible != parent.isVisible() )
		{
			visible = parent.isVisible() ;
			final DrawDelegate<World, Draw> delegate = getDrawDelegate() ;

			if( delegate != null )
			{
				if( visible == true )
				{
					addDraws( delegate ) ;
				}
				else
				{
					removeDraws( delegate ) ;
				}
			}
		}
	}*/
}
