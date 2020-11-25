package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.MalletList ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawUpdater is designed to trigger the update 
	of buffers when the Draw object state is still influx.
*/
public class DrawUpdater implements IUpdater<Draw, GeometryBuffer>
{
	private final static List<WeakReference<DrawUpdater>> globals = new ArrayList<WeakReference<DrawUpdater>>() ;

	private final Interpolation mode ;
	private final DrawBuffer drawBuffer ;
	private final ArrayList<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;

	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public DrawUpdater( final DrawBuffer _draw, final GeometryBuffer _geometry )
	{
		this( Interpolation.LINEAR, _draw, _geometry ) ;
	}

	public DrawUpdater( Interpolation _mode, final DrawBuffer _draw, final GeometryBuffer _geometry )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		drawBuffer = _draw ;
		buffers.add( _geometry ) ;
	}

	private void forceUpdate()
	{
		forceUpdate = true ;
	}

	@Override
	public void makeDirty()
	{
		dirty = true ;
	}

	@Override
	public boolean isDirty()
	{
		return dirty || forceUpdate ;
	}

	@Override
	public void addBuffers( final GeometryBuffer ... _buffers )
	{
		makeDirty() ;
		drawBuffer.addBuffers( _buffers ) ;
	}

	@Override
	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		makeDirty() ;
		drawBuffer.removeBuffers( _buffers ) ;
	}

	@Override
	public void addDraws( final Draw ... _draws )
	{
		forceUpdate() ;
		buffers.get( 0 ).addDraws( _draws ) ;
	}

	@Override
	public void removeDraws( final Draw ... _draws )
	{
		forceUpdate() ;
		buffers.get( 0 ).removeDraws( _draws ) ;
	}

	public DrawBuffer getDrawBuffer()
	{
		return drawBuffer ;
	}

	@Override
	public List<Draw> getDraws()
	{
		return buffers.get( 0 ).getDraws() ;
	}

	@Override
	public List<GeometryBuffer> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration )
	{
		boolean update = false ;

		for( final GeometryBuffer buffer : buffers )
		{
			boolean bufferUpdate = false ;

			final List<Draw> draws = buffer.getDraws() ;
			for( final Draw draw : draws )
			{
				if( draw.update( mode, _diff, _iteration ) == true )
				{
					bufferUpdate = true ;
				}
			}

			// We only want to add the buffer to the update 
			// list if draw state has changed, or if the Updater 
			// wants to force an update due to draws being removed 
			// or added.
			if( bufferUpdate == true || forceUpdate == true )
			{
				update = ( bufferUpdate == true ) ? true : update ;
				_updated.add( buffer ) ;
			}
		}

		forceUpdate = false ;
		dirty = update ;
	}
	
		/**
		Find if a GeometryBuffer has the same signature as passed in.
		If no buffer exists create a new buffer and add it to 
		the passed in world.
	*/
	public static DrawUpdater getOrCreate( final World _world,
										   final Program _program,
										   final Shape _shape,
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
	public static DrawUpdater getOrCreate( final World _world,
										   final Program _program,
										   final Shape.Swivel[] _swivel,
										   final Shape.Style _style,
										   final boolean _ui,
										   final int _order )
	{
		DrawUpdater updater = DrawUpdater.get( _world, _program, _swivel, _style, _ui, _order ) ;
		if( updater == null )
		{
			final DrawBuffer buffer = DrawAssist.add( new DrawBuffer( _program, _swivel, _style, _ui, _order ) ) ;
			final GeometryBuffer geom = DrawAssist.add( new GeometryBuffer( _swivel, _style, _ui, _order ) ) ;

			updater = DrawAssist.add( new DrawUpdater( buffer, geom ) ) ;

			_world.addBuffers( buffer ) ;
			WorldAssist.update( _world ) ;

			buffer.addBuffers( geom ) ;
			DrawAssist.update( buffer ) ;

			synchronized( globals )
			{
				globals.add( new WeakReference( updater ) ) ;
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
	public static DrawUpdater get( final World _world,
								   final Program _program,
								   final Shape _shape,
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
	public static DrawUpdater get( final World _world,
								   final Program _program,
								   final Shape.Swivel[] _swivel,
								   final Shape.Style _style,
								   final boolean _ui,
								   final int _order )
	{
		synchronized( globals )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final WeakReference<DrawUpdater> weak : globals )
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

	private static boolean isCompatibleSwivel( final Shape.Swivel[] _a, final Shape.Swivel[] _b )
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
