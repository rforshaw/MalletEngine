package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.* ;

/**
	Makes a request to receive a DrawDelegate from the 
	active Rendering System. addDraws() will be called 
	once delegate has been provided and the parent is 
	visible, if the parent is invisible removeDraws is 
	called instead.
*/
public abstract class GUIBase<T extends UIElement> extends ABase<T>
{
	private DrawDelegate<World, Draw> delegate = null ;
	private World world = null ;
	private Camera camera = null ;

	private boolean visible = true ;

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	public abstract void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world ) ;

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	public abstract void removeDraws( final DrawDelegate<World, Draw> _delegate ) ;

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		delegate = _delegate ;
		world = _world ;

		super.passDrawDelegate( _delegate, _world ) ;
		if( visible == true )
		{
			addDraws( delegate, _world ) ;
		}
	}

	@Override
	public void setParent( final T _parent )
	{
		super.setParent( _parent ) ;
		visible = _parent.isVisible() ;
	}

	@Override
	public void refresh()
	{
		final T parent = getParent() ;
		if( visible != parent.isVisible() )
		{
			visible = parent.isVisible() ;
			if( delegate != null )
			{
				if( visible == true )
				{
					addDraws( delegate, world ) ;
				}
				else
				{
					removeDraws( delegate ) ;
				}
			}
		}
	}

	@Override
	public void shutdown()
	{
		delegate = null ;
		world = null ;
	}

	public DrawDelegate<World, Draw> getDrawDelegate()
	{
		return delegate ;
	}

	public World getWorld()
	{
		return world ;
	}
}
