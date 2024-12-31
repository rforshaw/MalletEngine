package com.linxonline.mallet.ecs ;

import java.util.List ;
import java.util.Arrays ;

import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.caches.MemoryPool ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

public final class ECSCollision implements IECS<ECSCollision.Component>
{
	private final static MemoryPool<ContactPoint> contacts = new MemoryPool<ContactPoint>( () -> new ContactPoint() ) ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final List<Component> components = MalletList.<Component>newList() ;

	private final static ComponentUpdater componentUpdater = new ComponentUpdater() ;
	private final static HullUpdater hullUpdater = new HullUpdater() ;

	public ECSCollision() {}

	@Override
	public Component create( final ECSEntity _parent )
	{
		return create( _parent, new Hull[0] ) ;
	}

	public Component create( final ECSEntity _parent, final Hull[] _hulls )
	{
		final Component component = new Component( _parent, _hulls ) ;
		invokeLater( () ->
		{
			components.add( component ) ;
		} ) ;
		return component ;
	}

	@Override
	public void remove( final Component _component )
	{
		invokeLater( () ->
		{
			if( components.remove( _component ) )
			{
				for( final Hull hull : _component.getHulls() )
				{
					CollisionAssist.remove( hull ) ;
				}
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;
		Parallel.forBatch( components, 1000, componentUpdater ) ;
	}

	private void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	private void updateExecutions()
	{
		executions.update() ;
		final List<Runnable> runnables = executions.getCurrentData() ;
		if( runnables.isEmpty() )
		{
			return ;
		}

		final int size = runnables.size() ;
		for( int i = 0; i < size; i++ )
		{
			runnables.get( i ).run() ;
		}
		runnables.clear() ;
	}

	private static void updateCollision( final Hull _hull, final ContactPoint _point )
	{
		float x = 0.0f ;
		float y = 0.0f ;

		final int size = _hull.contactData.size() ;
		for( int i = 0; i < size; ++i )
		{
			_hull.contactData.get( i, _point ) ;
			if( _point.physical == true )
			{
				x += _point.contactNormalX * _point.penetration ;
				y += _point.contactNormalY * _point.penetration ;
			}
		}

		_hull.addToPosition( x, y ) ;
	}

	public static final class Component extends ECSEntity.Component
	{
		private final Hull[] hulls ;
		private boolean applyContact = true ;

		private Component( final ECSEntity _parent, final Hull[] _hulls )
		{
			_parent.super() ;
			hulls = _hulls ;
		}

		/**
			Update the hulls position to take into account 
			contact data, using the penetration depth shift 
			the hull so it no longer collides with other objects.
		*/
		public void applyContact( final boolean _apply )
		{
			applyContact = _apply ;
		}

		public Hull[] getHulls()
		{
			return hulls ;
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode( hulls ) ;
		}

		@Override
		public boolean equals( final Object _obj )
		{
			if( !( _obj instanceof Component ) )
			{
				return false ;
			}

			final Component b = ( Component )_obj ;
			if( applyContact != b.applyContact )
			{
				return false ;
			}

			if( hulls.length != b.hulls.length )
			{
				return false ;
			}

			for( int i = 0; i < hulls.length; ++i )
			{
				if( hulls[i].equals( b.hulls[i] ) == false )
				{
					return false ;
				}
			}

			return true ;
		}

		@Override
		public String toString()
		{
			return "Hulls: " + hulls.length ;
		}
	}

	private static final class ComponentUpdater implements Parallel.IListRun<Component>
	{
		@Override
		public void run( final int _start, final int _end, final List<Component> _components )
		{
			final int batchSize = 1000 ;

			final ContactPoint point = contacts.takeSync() ;

			for( int i = _start; i < _end; ++i )
			{
				final Component component = _components.get( i ) ;
				if( component.applyContact == false )
				{
					continue ;
				}

				// Shift the hulls position by the penetration depth.

				final Hull[] hulls = component.hulls ;
				final int size = hulls.length ;

				if( size > batchSize )
				{
					// If there are enough hulls, update them in parallel.
					Parallel.forBatch( hulls, batchSize, hullUpdater ) ;
					continue ;
				}

				// A component is likely to only have a handful of hulls
				// so there is no point spinning them onto their own worker.
				for( int j = 0; j < size; ++j )
				{
					ECSCollision.updateCollision( hulls[j], point ) ;
				}
			}

			contacts.reclaimSync( point ) ;
		}
	}

	private static final class HullUpdater implements Parallel.IArrayRun<Hull>
	{
		@Override
		public void run( final int _start, final int _end, final Hull[] _hulls )
		{
			final ContactPoint point = contacts.takeSync() ;

			for( int i = _start; i < _end; ++i )
			{
				ECSCollision.updateCollision( _hulls[i], point ) ;
			}

			contacts.reclaimSync( point ) ;
		}
	}
}
