package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.maths.* ;

public final class CollisionSystem
{
	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final List<Hull> hulls = MalletList.<Hull>newList() ;
	private final QuadTree treeHulls = new QuadTree() ;

	public CollisionSystem() {}

	public void add( final Hull _hull )
	{
		hulls.add( _hull ) ;
	}

	public void remove( final Hull _hull )
	{
		hulls.remove( _hull ) ;
	}

	public void update( final float _dt )
	{
		updateExecutions() ;

		treeHulls.clear() ;
		treeHulls.insertHulls( hulls ) ;
		treeHulls.update( _dt ) ;
	}

	public CollisionAssist.IAssist createCollisionAssist()
	{
		return new CollisionAssist.IAssist()
		{
			@Override
			public Box2D createBox2D( final AABB _aabb, final int[] _collidables )
			{
				final Box2D hull = new Box2D( _aabb, _collidables ) ;
				CollisionSystem.this.invokeLater( () ->
				{
					CollisionSystem.this.add( hull ) ;
				} ) ;

				return hull ;
			}

			@Override
			public Box2D createBox2D( final OBB _obb, final int[] _collidables )
			{
				final Box2D hull = new Box2D( _obb, _collidables ) ;
				CollisionSystem.this.invokeLater( () ->
				{
					CollisionSystem.this.add( hull ) ;
				} ) ;

				return hull ;
			}

			@Override
			public void add( final Hull _hull )
			{
				CollisionSystem.this.invokeLater( () ->
				{
					CollisionSystem.this.add( _hull ) ;
				} ) ;
			}

			@Override
			public void remove( final Hull _hull )
			{
				CollisionSystem.this.invokeLater( () ->
				{
					CollisionSystem.this.remove( _hull ) ;
				} ) ;
			}

			@Override
			public void remove( final Hull[] _hulls )
			{
				CollisionSystem.this.invokeLater( () ->
				{
					for( int i = 0; i < _hulls.length; ++i )
					{
						CollisionSystem.this.remove( _hulls[i] ) ;
					}
				} ) ;
			}

			@Override
			public ICollisionDelegate createCollisionDelegate()
			{
				return constructCollisionDelegate() ;
			}

			@Override
			public void removeCollisionDelegate( ICollisionDelegate _delegate )
			{
			
			}
		} ;
	}

	private ICollisionDelegate constructCollisionDelegate()
	{
		return new ICollisionDelegate()
		{
			private final Ray ray = Ray.create() ;		// Not thread-safe.
			private boolean shutdown = false ;

			@Override
			public Hull generateContacts( final Hull _hull )
			{
				if( shutdown == false )
				{
					treeHulls.generateContacts( _hull ) ;
				}
				return _hull ;
			}

			@Override
			public Hull ray( final Vector2 _start, final Vector2 _end )
			{
				return ray( _start, _end, null ) ;
			}

			@Override
			public Hull ray( final Vector2 _start, final Vector2 _end, final int[] _filters )
			{
				ray.setFromPoints( _start.x, _start.y, _end.x, _end.y ) ;
				final Hull hull = treeHulls.ray( ray, _filters ) ;
				return hull ;
			}

			@Override
			public void shutdown()
			{
				shutdown = true ;
			}
		} ;
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

	private static void simpleUpdate( final CollisionCheck _check, final List<Hull> _hulls )
	{
		for( final Hull hull1 : _hulls )
		{
			if( hull1.isCollidable() == false )
			{
				continue ;
			}

			updateCollisions( _check, hull1, _hulls ) ;
		}
	}

	private static void updateCollisions( final CollisionCheck _check, final Hull _hull1, final List<Hull> _hulls )
	{
		for( final Hull hull2 : _hulls )
		{
			if( _hull1 == hull2 )
			{
				continue ;
			}

			if( _hull1.isCollidableWithGroup( hull2.getGroupID() ) == true )
			{
				if( _check.generateContactPoint( _hull1, hull2 ) == true )
				{
					if( _hull1.contactData.size() >= ContactData.MAX_COLLISION_POINTS )
					{
						// No point looking for more contacts if 
						// we've reached maximum.
						return ;
					}
				}
			}
		}
	}

	private final boolean exists( final Hull _hull )
	{
		return hulls.contains( _hull ) ;
	}
}
