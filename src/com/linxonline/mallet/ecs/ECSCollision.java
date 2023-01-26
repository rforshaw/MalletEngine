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

	private final HullUpdater[] hullUpdaters = new HullUpdater[]
	{
		new HullUpdater(),
		new HullUpdater(),
		new HullUpdater(),
		new HullUpdater()
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

	public class Component extends ECSEntity.Component
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

		public void update()
		{
			if( applyContact == false )
			{
				return ;
			}

			// Shift the hulls position by the penetration depth.
			Parallel.forEach( hulls, hullUpdaters ) ;
		}
	}

	private static class ComponentUpdater implements Parallel.IRangeRun<Component>
	{
		@Override
		public void run( final int _index, final Component _component )
		{
			_component.update() ;
		}
	}
	
	private static class HullUpdater implements Parallel.IRangeRun<Hull>
	{
		private final ContactPoint point = new ContactPoint() ;
		private final Vector2 penShift = new Vector2() ;

		@Override
		public void run( final int _index, final Hull _hull )
		{
			penShift.setXY( 0.0f, 0.0f ) ;
			Hull.calculatePenetrationDepth( _hull.contactData, point, penShift ) ;
			_hull.addToPosition( penShift.x, penShift.y ) ;
		}
	}
}
