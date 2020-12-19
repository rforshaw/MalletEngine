package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

/**
	A dynamic object should only be updated once per draw call.
	However, the dynamic object could be required for multiple different buffers.
	
	Add the dynamics to this updater as well as any buffers that should be 
	updated, Draw is a dynamic object as it implements IUpdate.
*/
public class BasicUpdater<D extends IUpdate, B extends ABuffer> implements IUpdater<D, B>
{
	private final Interpolation mode ;
	private final ArrayList<B> buffers = new ArrayList<B>() ;
	private final ArrayList<D> dynamics = new ArrayList<D>() ;

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
	public void addDynamics( final D ... _dynamics )
	{
		makeDirty() ;
		dynamics.ensureCapacity( dynamics.size() + _dynamics.length ) ;
		for( final D dynamic : _dynamics )
		{
			dynamic.update( Interpolation.NONE, 0, 0 ) ;
			dynamics.add( dynamic ) ;
		}
	}

	@Override
	public void removeDynamics( final D ... _dynamics )
	{
		makeDirty() ;
		for( final D dynamic : _dynamics )
		{
			dynamics.remove( dynamic ) ;
		}
	}

	@Override
	public List<D> getDynamics()
	{
		return dynamics ;
	}

	@Override
	public List<B> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration )
	{
		boolean update = false ;

		for( final D dynamic : dynamics )
		{
			if( dynamic.update( mode, _diff, _iteration ) == true )
			{
				update = true ;
			}
		}

		dirty = update ;
	}
}
