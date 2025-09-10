package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.lang.ref.WeakReference ;

public class DrawInstancedUpdaterPool
{
	private final List<WeakReference<DrawInstancedUpdater>> pool = new ArrayList<WeakReference<DrawInstancedUpdater>>() ;

	public DrawInstancedUpdaterPool() {}

	public DrawInstancedUpdater getOrCreate( final IManageBuffers _anchor,
											 final Program _program,
											 final IShape _shape,
											 final boolean _ui,
											 final int _order )
	{
		return getOrCreate( _anchor, _program, _shape, _ui, _order, false ) ;
	}

	/**
		Create a DrawInstancedUpdater with a DrawInstancedBuffer 
		and GeometryBuffer precreated with the passed in parameters.

		If a DrawInstancedUpdater already exists with the same 
		parameters it will return it instead.
	*/
	public DrawInstancedUpdater getOrCreate( final IManageBuffers _anchor,
											 final Program _program,
											 final IShape _shape,
											 final boolean _ui,
											 final int _order,
											 final boolean _static )
	{
		final IShape.Attribute[] attributes = _shape.getAttribute() ;
	
		DrawInstancedUpdater updater = get( _anchor, _program, _ui, _order, _static ) ;
		if( updater == null )
		{
			final DrawInstancedBuffer buffer = DrawAssist.add( new DrawInstancedBuffer( _program, _shape, _ui, _order, _static ) ) ;
			final GeometryBuffer geom = DrawAssist.add( new GeometryBuffer( attributes ) ) ;

			updater = DrawAssist.add( new DrawInstancedUpdater( buffer ) ) ;

			_anchor.addBuffer( buffer ) ;
			_anchor.requestUpdate() ;

			buffer.addBuffer( geom ) ;
			DrawAssist.update( buffer ) ;

			synchronized( pool )
			{
				pool.add( new WeakReference<DrawInstancedUpdater>( updater ) ) ;
			}
		}

		return updater ;
	}

	/**
		When a DrawUpdater is created it is added to the global 
		pool of available DrawBuffers.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public DrawInstancedUpdater get( final IManageBuffers _anchor,
									 final Program _program,
									 final boolean _ui,
									 final int _order,
									 final boolean _static )
	{
		synchronized( pool )
		{
			final List<ICompatibleBuffer> worldBuffers = _anchor.getBuffers() ;

			for( final WeakReference<DrawInstancedUpdater> weak : pool )
			{
				final DrawInstancedUpdater updater = weak.get() ;
				if( updater == null )
				{
					continue ;
				}

				final DrawInstancedBuffer buffer = updater.drawBuffer ;
				if( buffer == null )
				{
					// Will need to remove buffer from global pool.
					continue ;
				}

				if( buffer.isStatic() != _static )
				{
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
