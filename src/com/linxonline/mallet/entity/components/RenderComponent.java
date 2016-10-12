package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.event.Event ;

public class RenderComponent extends Component
{
	private final ArrayList<Draw> toAddBasic = new ArrayList<Draw>() ;
	private final ArrayList<Draw> toAddText = new ArrayList<Draw>() ;

	private DrawDelegate drawDelegate = null ;
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
			return ;
		}

		if( drawDelegate == null )
		{
			toAddBasic.add( _draw ) ;
			return ;
		}

		drawDelegate.addBasicDraw( _draw, null ) ;
	}

	public void addTextDraw( final Draw _draw )
	{
		if( toDestroy != null )
		{
			return ;
		}

		if( drawDelegate == null )
		{
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
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		_events.add( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate _delegate )
			{
				drawDelegate = _delegate ;
				for( final Draw draw : toAddBasic )
				{
					addBasicDraw( draw ) ;
				}
				toAddBasic.clear() ;

				for( final Draw draw : toAddText )
				{
					addTextDraw( draw ) ;
				}
				toAddText.clear() ;
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
