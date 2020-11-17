package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawUpdater is designed to trigger the update 
	of buffers when the Draw object state is still influx.
*/
public class BasicUpdater<D extends IUpdate, B extends ABuffer> implements IUpdater<D, B>
{
	private final Interpolation mode ;
	private final ArrayList<B> buffers = new ArrayList<B>() ;
	private final ArrayList<D> draws = new ArrayList<D>() ;

	private boolean dirty = true ;

	public BasicUpdater()
	{
		this( Interpolation.LINEAR ) ;
	}

	public BasicUpdater( Interpolation _mode )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
	}

	@Override
	public void makeDirty()
	{
		dirty = true ;
	}

	@Override
	public boolean isDirty()
	{
		return dirty ;
	}

	@Override
	public void addBuffers( final B ... _buffers )
	{
		makeDirty() ;
		buffers.ensureCapacity( buffers.size() + _buffers.length ) ;
		for( final B buffer : _buffers )
		{
			buffers.add( buffer ) ;
		}
	}

	@Override
	public void removeBuffers( final B ... _buffers )
	{
		makeDirty() ;
		for( final B buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	@Override
	public void addDraws( final D ... _draws )
	{
		makeDirty() ;
		draws.ensureCapacity( draws.size() + _draws.length ) ;
		for( final D draw : _draws )
		{
			draw.update( Interpolation.NONE, 0, 0 ) ;
			draws.add( draw ) ;
		}
	}

	@Override
	public void removeDraws( final D ... _draws )
	{
		makeDirty() ;
		for( final D draw : _draws )
		{
			draws.remove( draw ) ;
		}
	}

	@Override
	public List<D> getDraws()
	{
		return draws ;
	}

	@Override
	public List<B> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<ABuffer> _updated,  final int _diff, final int _iteration )
	{
		boolean update = false ;

		for( final D draw : draws )
		{
			if( draw.update( mode, _diff, _iteration ) == true )
			{
				update = true ;
			}
		}

		_updated.addAll( buffers ) ;

		dirty = update ;
	}
}
