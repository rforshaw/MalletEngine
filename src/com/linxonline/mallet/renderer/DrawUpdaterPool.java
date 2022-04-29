package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.Logger ;

public class DrawUpdaterPool
{
	private final List<WeakReference<DrawUpdater>> pool = new ArrayList<WeakReference<DrawUpdater>>() ;

	public DrawUpdaterPool() {}

	/**
		Remove DrawUpdaters and associated resources that are
		used by the passed in World.

		Note: Any GeometryBuffers added to the DrawUpdater after it is created
		will also be removed too. Do not call if your GeometryBuffers are shared
		with other DrawBuffers.
	*/
	public void clean( final World _world )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final Iterator<WeakReference<DrawUpdater>> iterator = pool.iterator(); iterator.hasNext(); )
			{
				final WeakReference<DrawUpdater> weak = iterator.next() ;

				final DrawUpdater updater = weak.get() ;
				if( updater == null )
				{
					Logger.println( "DrawUpdater no longer exists, but has not been removed from the pool first.", Logger.Verbosity.MAJOR ) ;
					iterator.remove() ;
					continue ;
				}

				final DrawBuffer buffer = updater.drawBuffer ;
				if( worldBuffers.contains( buffer ) == false )
				{
					continue ;
				}

				_world.removeBuffers( buffer ) ;
				WorldAssist.update( _world ) ;

				iterator.remove() ;
				DrawAssist.remove( updater ) ;
				DrawAssist.remove( buffer ) ;
				for( final GeometryBuffer geometry : buffer.getBuffers() )
				{
					DrawAssist.remove( geometry ) ;
				}
			}
		}
	}

	/**
		Remove DrawUpdaters and associated resources that are used
		by the passed in program and world.

		Note: Any GeometryBuffers added to the DrawUpdater after it is created
		will also be removed too. Do not call if your GeometryBuffers are shared
		with other DrawBuffers.
	*/
	public void clean( final World _world, final Program _program )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final Iterator<WeakReference<DrawUpdater>> iterator = pool.iterator(); iterator.hasNext(); )
			{
				final WeakReference<DrawUpdater> weak = iterator.next() ;

				final DrawUpdater updater = weak.get() ;
				if( updater == null )
				{
					Logger.println( "DrawUpdater no longer exists, but has not been removed from the pool first.", Logger.Verbosity.MAJOR ) ;
					iterator.remove() ;
					continue ;
				}

				final DrawBuffer buffer = updater.drawBuffer ;
				if( _program.equals( buffer.getProgram() ) == false )
				{
					continue ;
				}

				if( worldBuffers.contains( buffer ) == false )
				{
					continue ;
				}

				_world.removeBuffers( buffer ) ;
				WorldAssist.update( _world ) ;

				iterator.remove() ;
				DrawAssist.remove( updater ) ;
				DrawAssist.remove( buffer ) ;
				for( final GeometryBuffer geometry : buffer.getBuffers() )
				{
					DrawAssist.remove( geometry ) ;
				}
			}
		}
	}

	/**
		Create a DrawUpdater with a DrawBuffer and GeometryBuffer
		precreated with the passed in parameters.

		If a DrawUpdater already exists with the same parameters 
		it will return it instead.
	*/
	public DrawUpdater getOrCreate( final World _world,
									final Program _program,
									final IShape _shape,
									final boolean _ui,
									final int _order )
	{
		return getOrCreate( _world, _program, _shape.getSwivel(), _shape.getStyle(), _ui, _order ) ;
	}

	/**
		Find if a GeometryBuffer has the same signature as passed in.
		If no buffer exists create a new buffer and add it to 
		the passed in world.
	*/
	public DrawUpdater getOrCreate( final World _world,
									final Program _program,
									final IShape.Swivel[] _swivel,
									final IShape.Style _style,
									final boolean _ui,
									final int _order )
	{
		DrawUpdater updater = get( _world, _program, _swivel, _style, _ui, _order ) ;
		if( updater == null )
		{
			final DrawBuffer buffer = DrawAssist.add( new DrawBuffer( _program, _swivel, _style, _ui, _order ) ) ;
			final GeometryBuffer geom = DrawAssist.add( new GeometryBuffer( _swivel, _style, _ui, _order ) ) ;

			updater = DrawAssist.add( new DrawUpdater( buffer ) ) ;

			_world.addBuffers( buffer ) ;
			WorldAssist.update( _world ) ;

			buffer.addBuffers( geom ) ;
			DrawAssist.update( buffer ) ;

			synchronized( pool )
			{
				pool.add( new WeakReference( updater ) ) ;
			}
		}

		return updater ;
	}

	/**
		When a DrawUpdater is created it is added to the global 
		pool of available DrawUpdaters.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public DrawUpdater get( final World _world,
							final Program _program,
							final IShape _shape,
							final boolean _ui,
							final int _order )
	{
		return get( _world, _program, _shape.getSwivel(), _shape.getStyle(), _ui, _order ) ;
	}

	/**
		When a DrawUpdater is created it is added to the global 
		pool of available DrawBuffers.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public DrawUpdater get( final World _world,
							final Program _program,
							final IShape.Swivel[] _swivel,
							final IShape.Style _style,
							final boolean _ui,
							final int _order )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final WeakReference<DrawUpdater> weak : pool )
			{
				final DrawUpdater updater = weak.get() ;
				if( updater == null )
				{
					continue ;
				}

				final DrawBuffer buffer = updater.drawBuffer ;
				if( buffer == null )
				{
					// Will need to remove buffer from global pool.
					continue ;
				}

				if( buffer.isUI() != _ui )
				{
					continue ;
				}

				if( buffer.getOrder() != _order )
				{
					continue ;
				}

				// Lets check the cheapest value first
				if( buffer.getStyle().equals( _style ) == false )
				{
					continue ;
				}

				if( isCompatibleSwivel( buffer.getSwivel(), _swivel ) == false )
				{
					continue ;
				}

				if( _program.equals( buffer.getProgram() ) == false )
				{
					continue ;
				}

				if( worldBuffers.contains( buffer ) == false )
				{
					continue ;
				}

				return updater ;
			}
		}

		return null ;
	}

	protected static boolean isCompatibleSwivel( final IShape.Swivel[] _a, final IShape.Swivel[] _b )
	{
		if( _a.length != _b.length )
		{
			return false ;
		}

		for( int i = 0; i < _a.length; ++i )
		{
			if( _a[i] != _b[i] )
			{
				return false ;
			}
		}

		return true ;
	}
}
