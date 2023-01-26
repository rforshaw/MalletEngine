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
public class StorageUpdater<D extends IUpdate> implements IUpdater<Storage>
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

	public void addBuffers( final Storage ... _buffers )
	{
		makeDirty() ;
		for( final Storage storage : _buffers )
		{
			buffers.add( storage ) ;
		}
	}

	public void removeBuffers( final Storage ... _buffers )
	{
		makeDirty() ;
		for( final Storage storage : _buffers )
		{
			buffers.remove( storage ) ;
		}
	}

	public void addDynamics( final D ... _dynamics )
	{
		forceUpdate() ;
		dynamics.ensureCapacity( dynamics.size() + _dynamics.length ) ;
		for( final D dynamic : _dynamics )
		{
			dynamics.add( dynamic ) ;
		}
	}

	public void removeDynamics( final D ... _dynamics )
	{
		forceUpdate() ;
		for( final D dynamic : _dynamics )
		{
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
	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration )
	{
		if( forceUpdate == false && dirty == false )
		{
			return ;
		}

		parallelUpdater.set( mode, _diff, _iteration, forceUpdate ) ;

		dirty = false ;
		Parallel.forEach( dynamics, parallelUpdater ) ;

		final boolean stateHasChanged = parallelUpdater.hasStateChanged() ;
		dirty = parallelUpdater.isDirty() ;

		if( stateHasChanged == true )
		{
			_updated.addAll( buffers ) ;
		}

		forceUpdate = false ;
	}
}
