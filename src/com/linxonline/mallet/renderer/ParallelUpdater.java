package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.concurrent.atomic.AtomicBoolean ;

import com.linxonline.mallet.util.Parallel ;

public final class ParallelUpdater<T extends IUpdate> implements Parallel.IListRun<T>
{
	private Interpolation mode ;
	private int diff ;
	private int iteration ;

	private AtomicBoolean dirty = new AtomicBoolean( false ) ;

	public void set( final Interpolation _mode, final int _diff, final int _iteration )
	{
		mode = _mode ;
		diff = _diff ;
		iteration = _iteration ;

		dirty.set( false ) ;
	}

	public boolean isDirty()
	{
		return dirty.get() ;
	}

	@Override
	public void run( final int _start, final int _end, final List<T> _list )
	{
		boolean d = false ;

		// A draw object does not need the geometry buffer
		// to be updated if it's just the position, rotation,
		// or scale that has changed.
		for( int i = _start; i < _end; ++i )
		{
			if( _list.get( i ).update( mode, diff, iteration ) == true )
			{
				d = true ;
			}
		}

		if( d == true )
		{
			dirty.set( true ) ;
		}
	}
}
