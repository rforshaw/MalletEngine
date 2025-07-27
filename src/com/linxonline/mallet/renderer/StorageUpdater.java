package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Parallel ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawUpdater is designed to trigger the update 
	of buffers when the Draw object state is still influx.
*/
public final class StorageUpdater<D extends IUpdate> implements IUpdater
{
	private final Interpolation mode ;
	private final ArrayList<D> dynamics = new ArrayList<D>() ;
	private final ArrayList<Storage> buffers = new ArrayList<Storage>() ;

	private final ParallelUpdater<D> parallelUpdater = new ParallelUpdater<D>() ;
	
	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public StorageUpdater( final D _update, final Storage _storage )
	{
		this( Interpolation.LINEAR, _update, _storage ) ;
	}

	public StorageUpdater( Interpolation _mode, final D _update, final Storage _storage )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		dynamics.add( _update ) ;
		buffers.add( _storage ) ;
	}

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

	public Storage addBuffer( final Storage _storage )
	{
		makeDirty() ;
		buffers.add( _storage ) ;
		return _storage ;
	}

	public void removeBuffer( final Storage _storage )
	{
		makeDirty() ;
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
	public void update( final List<ABuffer> _updated, final float _coefficient )
	{
		if( forceUpdate == false && dirty == false )
		{
			return ;
		}

		parallelUpdater.set( mode, _coefficient ) ;

		Parallel.forBatch( dynamics, 1000, parallelUpdater ) ;
		final boolean update = parallelUpdater.isDirty() ;
		dirty = update ;

		if( forceUpdate == true || update == true )
		{
			_updated.addAll( buffers ) ;
		}

		forceUpdate = update ;
	}
}
