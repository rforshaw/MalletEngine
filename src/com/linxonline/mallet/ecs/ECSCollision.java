package com.linxonline.mallet.ecs ;

import java.util.List ;

import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.physics.primitives.* ;
import com.linxonline.mallet.physics.hulls.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

public final class ECSCollision implements IECS<ECSCollision.Component>
{
	private final static int PARALLEL_HULL_MIN = 250 ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final CollisionSystem system ;
	private final List<Component> components = MalletList.<Component>newList() ;

	private final ComponentUpdater[] componentUpdaters = new ComponentUpdater[]
	{
		new ComponentUpdater(),
		new ComponentUpdater(),
		new ComponentUpdater(),
		new ComponentUpdater()
	} ;

	public ECSCollision( final CollisionSystem _system )
	{
		system = _system ;
	}

	@Override
	public Component create( final ECSEntity _parent )
	{
		return create( _parent, new Hull[0] ) ;
	}

	public Component create( final ECSEntity _parent,
							 final Vector2 _min,
							 final Vector2 _max,
							 final Vector2 _position,
							 final Vector2 _offset )
	{
		final Box2D box = new Box2D( new AABB( _min, _max ), _position, _offset ) ;
		return create( _parent, new Hull[] { box } ) ;
	}

	public Component create( final ECSEntity _parent, final Hull[] _hulls )
	{
		final Component component = new Component( _parent, _hulls ) ;
		invokeLater( () ->
		{
			components.add( component ) ;
			for( final Hull hull : component.getHulls() )
			{
				system.add( hull ) ;
			}
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
					system.remove( hull ) ;
				}
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;
		Parallel.forEach( components, componentUpdaters ) ;
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

	public class Component extends ECSEntity.Component
	{
		private final Hull[] hulls ;
		private boolean applyContact = true ;

		private final HullUpdater[] hullUpdaters ;

		public Component( final ECSEntity _parent, final Hull[] _hulls )
		{
			_parent.super() ;
			hulls = _hulls ;

			// We only want to use the parallel path if there
			// is enough hulls to warrant it.
			hullUpdaters = ( hulls.length <= PARALLEL_HULL_MIN ) ? null : new HullUpdater[]
			{
				new HullUpdater(),
				new HullUpdater(),
				new HullUpdater(),
				new HullUpdater()
			} ;
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
		private final ContactPoint point = new ContactPoint() ;
		private final Vector2 penShift = new Vector2() ;

		@Override
		public void run( final int _index, final Component _component )
		{
			if( _component.applyContact == false )
			{
				return ;
			}

			// If there is enough hulls to warrant updating across
			// multiple threads.
			final Hull[] hulls = _component.hulls ;
			final HullUpdater[] updaters = _component.hullUpdaters ;
			if( updaters != null )
			{
				// Shift the hulls position by the penetration depth.
				Parallel.forEach( hulls, updaters ) ;
				return ;
			}

			// If not fallback to processing on the main thread.
			for( final Hull hull : hulls )
			{
				updateCollision( hull, point, penShift ) ;
			}
		}
	}

	private static class HullUpdater implements Parallel.IRangeRun<Hull>
	{
		private final ContactPoint point = new ContactPoint() ;
		private final Vector2 penShift = new Vector2() ;

		@Override
		public void run( final int _index, final Hull _hull )
		{
			ECSCollision.updateCollision( _hull, point, penShift ) ;
		}
	}
}
