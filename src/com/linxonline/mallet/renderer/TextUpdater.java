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
public class TextUpdater implements IUpdater<TextDraw, TextBuffer>
{
	private final static List<WeakReference<TextUpdater>> globals = new ArrayList<WeakReference<TextUpdater>>() ;

	private final Interpolation mode ;
	private final ArrayList<TextBuffer> buffers = new ArrayList<TextBuffer>() ;

	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public TextUpdater( final TextBuffer _buffer )
	{
		this( Interpolation.LINEAR, _buffer ) ;
	}

	public TextUpdater( Interpolation _mode, final TextBuffer _buffer )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		buffers.add( _buffer ) ;
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
	public void addBuffers( final TextBuffer ... _buffers )
	{
		forceUpdate() ;
		for( final TextBuffer buffer : _buffers )
		{
			buffers.add( buffer ) ;
		}
	}

	@Override
	public void removeBuffers( final TextBuffer ... _buffers )
	{
		forceUpdate() ;
		for( final TextBuffer buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	@Override
	public void addDraws( final TextDraw ... _draws )
	{
		forceUpdate() ;
		buffers.get( 0 ).addDraws( _draws ) ;
	}

	@Override
	public void removeDraws( final TextDraw ... _draws )
	{
		forceUpdate() ;
		buffers.get( 0 ).removeDraws( _draws ) ;
	}

	@Override
	public List<TextDraw> getDraws()
	{
		return buffers.get( 0 ).getTextDraws() ;
	}

	@Override
	public List<TextBuffer> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration )
	{
		boolean update = false ;

		for( final TextBuffer buffer : buffers )
		{
			boolean bufferUpdate = false ;

			final List<TextDraw> draws = buffer.getTextDraws() ;
			for( final TextDraw draw : draws )
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
	public static TextUpdater getOrCreate( final World _world,
										   final Program _program,
										   final boolean _ui,
										   final int _order )
	{
		TextUpdater updater = TextUpdater.get( _world, _program, _ui, _order ) ;
		if( updater == null )
		{
			final TextBuffer buffer = DrawAssist.add( new TextBuffer( _program, _ui, _order ) ) ;
			updater = DrawAssist.add( new TextUpdater( buffer ) ) ;

			updater.addBuffers( buffer ) ;
			DrawAssist.update( buffer ) ;

			_world.addBuffers( buffer ) ;
			WorldAssist.update( _world ) ;

			synchronized( globals )
			{
				globals.add( new WeakReference( updater ) ) ;
			}
		}

		return updater ;
	}

	/**
		When a TextUpdater is created it is added to the global 
		pool of available DrawBuffers.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public static TextUpdater get( final World _world,
								   final Program _program,
								   final boolean _ui,
								   final int _order )
	{
		synchronized( globals )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final WeakReference<TextUpdater> weak : globals )
			{
				final TextUpdater updater = weak.get() ;
				if( updater == null )
				{
					continue ;
				}

				final TextBuffer buffer = updater.getBuffers().get( 0 ) ;
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
