package com.linxonline.mallet.ecs ;

import java.util.List ;

import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.caches.MemoryPool ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

public final class ECSCollision implements IECS<ECSCollision.Component>
{
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
		Parallel.forEach( components, 1000, componentUpdater ) ;
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

	private static void updateCollision( final Hull _hull, final ContactPoint _point, final Vector2 _penShift )
	{
		_penShift.setXY( 0.0f, 0.0f ) ;
		Hull.calculatePenetrationDepth( _hull.contactData, _point, _penShift ) ;
		_hull.addToPosition( _penShift.x, _penShift.y ) ;
	}

	public static class Component extends ECSEntity.Component
	{
		private final Hull[] hulls ;
		private boolean applyContact = true ;

		public Component( final ECSEntity _parent, final Hull[] _hulls )
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
	}

	private static class ComponentUpdater implements Parallel.IRangeRun<Component>
	{
		@Override
		public void run( final int _index, final Component _component )
		{
			if( _component.applyContact == false )
			{
				return ;
			}

			// Shift the hulls position by the penetration depth.
			Parallel.forBatch( _component.hulls, 1000, hullUpdater ) ;
		}
	}

	private static class HullUpdater implements Parallel.IArrayRun<Hull>
	{
		private final static MemoryPool<ContactPoint> contacts = new MemoryPool<ContactPoint>( () -> new ContactPoint() ) ;
		private final static MemoryPool<Vector2> vec2s = new MemoryPool<Vector2>( () -> new Vector2() ) ;

		@Override
		public void run( final int _start, final int _end, final Hull[] _hulls )
		{
			final ContactPoint point = contacts.takeSync() ;
			final Vector2 penShift = vec2s.takeSync() ;

			for( int i = _start; i < _end; ++i )
			{
				ECSCollision.updateCollision( _hulls[i], point, penShift ) ;
			}

			contacts.reclaimSync( point ) ;
			vec2s.reclaimSync( penShift ) ;
		}
	}
}
