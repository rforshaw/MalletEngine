package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.Parallel ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawUpdater is designed to trigger the update 
	of buffers when the Draw object state is still influx.
*/
public final class StorageUpdater<D extends IUpdate> implements IUpdater
{
	private final Interpolate.IMode mode ;
	private final ArrayList<D> dynamics = new ArrayList<D>() ;
	private final ArrayList<Storage> buffers = new ArrayList<Storage>() ;

	private final ParallelUpdater<D> parallelUpdater = new ParallelUpdater<D>() ;
	
	private boolean forceUpdate = true ;

	public StorageUpdater( final D _update, final Storage _storage )
	{
		this( Interpolate::linear, _update, _storage ) ;
	}

	public StorageUpdater( Interpolate.IMode _mode, final D _update, final Storage _storage )
	{
		mode = ( _mode != null ) ? _mode : Interpolate::linear ;
		dynamics.add( _update ) ;
		buffers.add( _storage ) ;
	}

	@Override
	public void forceUpdate()
	{
		forceUpdate = true ;
	}

	public Storage addBuffer( final Storage _storage )
	{
		forceUpdate() ;
		buffers.add( _storage ) ;
		return _storage ;
	}

	public void removeBuffer( final Storage _storage )
	{
		forceUpdate() ;
		buffers.remove( _storage ) ;
	}

	public void addDynamics( final D ... _dynamics )
	{
		forceUpdate() ;
		dynamics.ensureCapacity( dynamics.size() + _dynamics.length ) ;
		final int size = _dynamics.length ;
		for( int i = 0; i < size; ++i )
		{
			final D dynamic = _dynamics[i] ;
			dynamics.add( dynamic ) ;
		}
	}

	public void removeDynamics( final D ... _dynamics )
	{
		forceUpdate() ;
		final int size = _dynamics.length ;
		for( int i = 0; i < size; ++i )
		{
			final D dynamic = _dynamics[i] ;
			dynamics.remove( dynamic ) ;
		}
	}

	public List<D> getDynamics()
	{
		return dynamics ;
	}

	public List<Storage> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<? super IUpdateState> _updated, final float _coefficient )
	{
		if( forceUpdate == false )
		{
			return ;
		}

		forceUpdate = false ;
		parallelUpdater.set( mode, _coefficient ) ;

		Parallel.forBatch( dynamics, 1000, parallelUpdater ) ;
		if( parallelUpdater.isDirty() )
		{
			forceUpdate = true ;
		}

		_updated.addAll( buffers ) ;
	}
}
