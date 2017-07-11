package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.MalletList ;

public class RenderComponent extends Component
{
	private final List<Draw> toAddBasic = MalletList.<Draw>newList() ;
	private final List<Draw> toAddText = MalletList.<Draw>newList() ;

	private DrawDelegate<World, Draw> drawDelegate = null ;
	private Component.ReadyCallback toDestroy = null ;

	public RenderComponent()
	{
		this( "RENDER" ) ;
	}

	public RenderComponent( final String _name )
	{
		this( _name, "RENDER_COMPONENT" ) ;
	}

	public RenderComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	public void addBasicDraw( final Draw _draw )
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
			toAddBasic.add( _draw ) ;
			return ;
		}

		drawDelegate.addBasicDraw( _draw, null ) ;
	}

	public void addTextDraw( final Draw _draw )
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
			toAddText.add( _draw ) ;
			return ;
		}

		drawDelegate.addTextDraw( _draw, null ) ;
	}

	public void remove( final Draw _draw )
	{
		if( drawDelegate != null )
		{
			drawDelegate.removeDraw( _draw ) ;
		}
	}

	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
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
						final Draw draw = toAddBasic.get( i ) ;
						addBasicDraw( draw ) ;
					}
					toAddBasic.clear() ;
				}

				if( toAddText.isEmpty() == false )
				{
					final int size = toAddText.size() ;
					for( int i = 0; i < size; i++ )
					{
						final Draw draw = toAddText.get( i ) ;
						addTextDraw( draw ) ;
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
