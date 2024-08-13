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
		used by the passed in IManageBuffers.

		Note: Any GeometryBuffers added to the DrawUpdater after it is created
		will also be removed too. Do not call if your GeometryBuffers are shared
		with other DrawBuffers.
	*/
	public void clean( final IManageBuffers _anchor )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _anchor.getBuffers() ;

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

				_anchor.removeBuffers( buffer ) ;
				_anchor.requestUpdate() ;

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
	public void clean( final IManageBuffers _anchor, final Program _program )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _anchor.getBuffers() ;

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

				_anchor.removeBuffers( buffer ) ;
				_anchor.requestUpdate() ;

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

	public DrawUpdater create( final IManageBuffers _anchor,
							   final Program _program,
							   final IShape _shape,
							   final boolean _ui,
							   final int _order )
	{
		return create( _anchor, _program, _shape.getAttribute(), _ui, _order ) ;
	}

	public DrawUpdater create( final IManageBuffers _anchor,
							   final Program _program,
							   final IShape.Attribute[] _swivel,
							   final boolean _ui,
							   final int _order )
	{
		final DrawBuffer buffer = DrawAssist.add( new DrawBuffer( _program, _ui, _order ) ) ;
		final GeometryBuffer geom = DrawAssist.add( new GeometryBuffer( _swivel ) ) ;

		final DrawUpdater updater = DrawAssist.add( new DrawUpdater( buffer ) ) ;

		_anchor.addBuffers( buffer ) ;
		_anchor.requestUpdate() ;

		buffer.addBuffers( geom ) ;
		buffer.requestUpdate() ;

		synchronized( pool )
		{
			pool.add( new WeakReference<DrawUpdater>( updater ) ) ;
		}

		return updater ;
	}

	/**
		Create a DrawUpdater with a DrawBuffer and GeometryBuffer
		precreated with the passed in parameters.

		If a DrawUpdater already exists with the same parameters 
		it will return it instead.
	*/
	public DrawUpdater getOrCreate( final IManageBuffers _anchor,
									final Program _program,
									final IShape _shape,
									final boolean _ui,
									final int _order )
	{
		return getOrCreate( _anchor, _program, _shape.getAttribute(), _ui, _order ) ;
	}

	/**
		Find if a GeometryBuffer has the same signature as passed in.
		If no buffer exists create a new buffer and add it to 
		the passed in world.
	*/
	public DrawUpdater getOrCreate( final IManageBuffers _anchor,
									final Program _program,
									final IShape.Attribute[] _swivel,
									final boolean _ui,
									final int _order )
	{
		final DrawUpdater updater = get( _anchor, _program, _ui, _order ) ;
		return ( updater != null ) ? updater : create( _anchor, _program, _swivel, _ui, _order ) ;
	}

	/**
		When a DrawUpdater is created it is added to the global 
		pool of available DrawBuffers.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public DrawUpdater get( final IManageBuffers _anchor,
							final Program _program,
							final boolean _ui,
							final int _order )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _anchor.getBuffers() ;

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
}
