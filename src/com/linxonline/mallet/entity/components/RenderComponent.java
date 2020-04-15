package com.linxonline.mallet.entity.components ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;

public class RenderComponent extends Component
{
	private Map<World, List<Draw>> toAddBasic = new HashMap<World, List<Draw>>() ;
	private Map<World, List<Draw>> toAddText = new HashMap<World, List<Draw>>() ;

	private DrawDelegate drawDelegate = null ;
	private Entity.ReadyCallback toDestroy = null ;

	public RenderComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public RenderComponent( final Entity _parent, Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
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
			if( toAddBasic.containsKey( _world ) == false )
			{
				toAddBasic.put( _world, MalletList.<Draw>newList() ) ;
			}

			final List<Draw> toAdd = toAddBasic.get( _world ) ;
			toAdd.add( _draw ) ;
			return ;
		}

		drawDelegate.addBasicDraw( _draw, _world ) ;
	}

	public void addBasicDraw( final List<Draw> _draws, final World _world )
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
			if( toAddBasic.containsKey( _world ) == false )
			{
				toAddBasic.put( _world, MalletList.<Draw>newList( _draws.size() ) ) ;
			}

			final List<Draw> toAdd = toAddBasic.get( _world ) ;
			toAdd.addAll( _draws ) ;
			return ;
		}

		drawDelegate.addBasicDraw( _draws, _world ) ;
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
			// If the renderer has yet to give a drawDelegate
			if( toAddText.containsKey( _world ) == false )
			{
				toAddText.put( _world, MalletList.<Draw>newList() ) ;
			}

			final List<Draw> toAdd = toAddText.get( _world ) ;
			toAdd.add( _draw ) ;
			return ;
		}

		drawDelegate.addTextDraw( _draw, _world ) ;
	}

	public void remove( final Draw _draw )
	{
		if( drawDelegate == null )
		{
			Logger.println( "Attempting to remove draw from render component without a draw delegate.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		drawDelegate.removeDraw( _draw ) ;
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
		_events.add( DrawAssist.constructDrawDelegate( ( final DrawDelegate _delegate ) ->
		{
			drawDelegate = _delegate ;
			if( toAddBasic.isEmpty() == false )
			{
				for( Map.Entry<World, List<Draw>> entry : toAddBasic.entrySet() )
				{
					final World world = entry.getKey() ;
					List<Draw> draws = entry.getValue() ;
					drawDelegate.addBasicDraw( draws, world ) ;
				}
				toAddBasic.clear() ;
			}

			if( toAddText.isEmpty() == false )
			{
				for( Map.Entry<World, List<Draw>> entry : toAddText.entrySet() )
				{
					final World world = entry.getKey() ;
					List<Draw> draws = entry.getValue() ;
					drawDelegate.addTextDraw( draws, world ) ;
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
