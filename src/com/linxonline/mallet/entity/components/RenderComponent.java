package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.Entity.Component ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

public class RenderComponent extends Entity.Component
{
	private final List<Tuple<Draw, World>> toAddBasic = MalletList.<Tuple<Draw, World>>newList() ;
	private final List<Tuple<Draw, World>> toAddText = MalletList.<Tuple<Draw, World>>newList() ;

	private DrawDelegate<World, Draw> drawDelegate = null ;
	private Entity.ReadyCallback toDestroy = null ;

	public RenderComponent( final Entity _parent )
	{
		this( _parent, "RENDER" ) ;
	}

	public RenderComponent( final Entity _parent, final String _name )
	{
		this( _parent, _name, "RENDER_COMPONENT" ) ;
	}

	public RenderComponent( final Entity _parent, final String _name, final String _group )
	{
		_parent.super( _name, _group ) ;
	}

	public void addBasicDraw( final Draw _draw )
	{
		addBasicDraw( _draw, null ) ;
	}

	public void addBasicDraw( final Draw _draw, final World _world )
	{
		if( toDestroy != null )
		{
			// toDestroy will only be set if the entity 
			// has been flagged for destruction, if that's 
			// the case there is no point add a draw object 
			// that will need to be removed.
			return ;
		}

		if( drawDelegate == null )
		{
			// If the renderer has yet to give a drawDelegate
			toAddBasic.add( new Tuple<Draw, World>( _draw, _world ) ) ;
			return ;
		}

		drawDelegate.addBasicDraw( _draw, _world ) ;
	}

	public void addTextDraw( final Draw _draw )
	{
		addTextDraw( _draw, null ) ;
	}

	public void addTextDraw( final Draw _draw, final World _world )
	{
		if( toDestroy != null )
		{
			// toDestroy will only be set if the entity 
			// has been flagged for destruction, if that's 
			// the case there is no point add a draw object 
			// that will need to be removed.
			return ;
		}

		if( drawDelegate == null )
		{
			// If the renderer has yet to give a drawDelegate
			toAddText.add( new Tuple<Draw, World>( _draw, _world ) ) ;
			return ;
		}

		drawDelegate.addTextDraw( _draw, _world ) ;
	}

	public void remove( final Draw _draw )
	{
		if( drawDelegate != null )
		{
			drawDelegate.removeDraw( _draw ) ;
		}
	}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		if( drawDelegate != null )
		{
			drawDelegate.shutdown() ;
			drawDelegate = null ;
		}

		toDestroy = _callback ;
		super.readyToDestroy( _callback ) ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		_events.add( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				drawDelegate = _delegate ;
				if( toAddBasic.isEmpty() == false )
				{
					final int size = toAddBasic.size() ;
					for( int i = 0; i < size; i++ )
					{
						final Tuple<Draw, World> tuple = toAddBasic.get( i ) ;
						addBasicDraw( tuple.getLeft(), tuple.getRight() ) ;
					}
					toAddBasic.clear() ;
				}

				if( toAddText.isEmpty() == false )
				{
					final int size = toAddText.size() ;
					for( int i = 0; i < size; i++ )
					{
						final Tuple<Draw, World> tuple = toAddText.get( i ) ;
						addTextDraw( tuple.getLeft(), tuple.getRight() ) ;
					}
					toAddText.clear() ;
				}
			}
		} ) ) ;
	}

	public void clear()
	{
		if( drawDelegate != null )
		{
			drawDelegate.shutdown() ;
		}
	}
}
