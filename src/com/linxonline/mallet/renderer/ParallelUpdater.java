package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.Parallel ;

public final class ParallelUpdater<T extends IUpdate> implements Parallel.IRangeRun<T>
{
	private Interpolation mode ;
	private int diff ;
	private int iteration ;

	private boolean dirty = false ;

	public void set( final Interpolation _mode, final int _diff, final int _iteration )
	{
		mode = _mode ;
		diff = _diff ;
		iteration = _iteration ;

		dirty = false ;
	}

	public boolean isDirty()
	{
		return dirty ;
	}

	@Override
	public void run( final int _index, final T _t )
	{
		// A draw object does not need the geometry buffer
		// to be updated if it's just the position, rotation,
		// or scale that has changed.
		if( _t.update( mode, diff, iteration ) == true )
		{
			dirty = true ;
		}
	}
}
