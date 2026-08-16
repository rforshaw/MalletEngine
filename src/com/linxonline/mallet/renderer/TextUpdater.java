package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Interpolate ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawUpdater is designed to trigger the update 
	of buffers when the Draw object state is still influx.
*/
public final class TextUpdater implements IUpdater
{
	private final ArrayList<TextBuffer> buffers = new ArrayList<TextBuffer>() ;

	private Interpolate.IMode mode ;
	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public TextUpdater( final TextBuffer _buffer )
	{
		this( Interpolate::linear, _buffer ) ;
	}

	public TextUpdater( Interpolate.IMode _mode, final TextBuffer _buffer )
	{
		mode = ( _mode != null ) ? _mode : Interpolate::linear ;
		buffers.add( _buffer ) ;
	}

	@Override
	public void forceUpdate()
	{
		forceUpdate = true ;
	}

	public void makeDirty()
	{
		dirty = true ;
	}

	public boolean isDirty()
	{
		return dirty || forceUpdate ;
	}

	public TextBuffer addBuffer( final TextBuffer _buffer )
	{
		forceUpdate() ;
		buffers.add( _buffer ) ;
		return _buffer ;
	}

	public void removeBuffer( final TextBuffer _buffer )
	{
		forceUpdate() ;
		buffers.remove( _buffer ) ;
	}

	public TextBuffer getBuffer( final int _index )
	{
		return buffers.get( _index ) ;
	}

	public List<TextBuffer> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<? super IUpdateState> _updated, final float _coefficient )
	{
		if( forceUpdate == false && dirty == false )
		{
			return ;
		}

		final int bufferSize = buffers.size() ;
		for( int i = 0; i < bufferSize; ++i )
		{
			final TextBuffer buffer = buffers.get( i ) ;

			boolean updateBuffer = false ;
			final List<TextDraw> draws = buffer.getTextDraws() ;

			final int drawSize = draws.size() ;
			for( int j = 0; j < drawSize; ++j )
			{
				final TextDraw draw = draws.get( j ) ;
				if( draw.update( mode, _coefficient ) == true )
				{
					updateBuffer = true ;
				}
			}

			// We only want to add the buffer to the update 
			// list if draw state has changed, or if the Updater 
			// wants to force an update due to draws being removed 
			// or added.
			if( updateBuffer == true || forceUpdate == true )
			{
				_updated.add( buffer ) ;
			}
		}

		forceUpdate = false ;
		dirty = !_updated.isEmpty() ;
	}

	public void setInterpolation( Interpolate.IMode _mode )
	{
		mode = ( _mode != null ) ? _mode : mode ;
	}
}
