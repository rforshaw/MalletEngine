package com.linxonline.mallet.ecs ;

import java.util.List ;
import java.util.Arrays ;

import com.linxonline.mallet.physics.* ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

public class ECSCollision implements IECS<ECSCollision.Component>
{
	public final static ICollider DEFAULT_COLLIDER = ( final ContactPoint _point ) ->
	{
		final Hull a = _point.a ;
		final Hull b = _point.b ;

		if( a.isCollidableWithGroup( b.getGroupID() ) )
		{
			synchronized( a )
			{
				a.getCollider().apply( a, b, _point ) ;
			}
		}

		if( b.isCollidableWithGroup( a.getGroupID() ) )
		{
			_point.contactNormalX *= -1.0f ;
			_point.contactNormalY *= -1.0f ;

			synchronized( b )
			{
				b.getCollider().apply( b, a, _point ) ;
			}
		}
	} ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final List<Component> components = MalletList.<Component>newList() ;

	private final CollisionSystem cs = new CollisionSystem() ;
	private final List<ContactData> contacts = MalletList.<ContactData>newList() ;

	private final ICollider collider ;

	public ECSCollision()
	{
		this( DEFAULT_COLLIDER ) ;
	}

	public ECSCollision( final ICollider _collider )
	{
		collider = _collider ;
	}

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
			cs.add( _hulls ) ;
			components.add( component ) ;
		} ) ;
		return component ;
	}

	@Override
	public void remove( final Component _component )
	{
		invokeLater( () ->
		{
			final Hull[] hulls = _component.getHulls() ;
			for( int i = 0; i < hulls.length; ++i )
			{
				cs.remove( hulls[i] ) ;
			}
			components.remove( _component ) ;
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;

		cs.update( ( float )_dt, contacts ) ;

		//final long start = System.nanoTime() ;

		Parallel.forEach( contacts, 100, ( final int _index, final ContactData _data ) ->
		{
			final ContactPoint point = new ContactPoint() ;

			final int size = _data.size() ;
			//System.out.println( "Contacts: " + size ) ;
			for( int i = 0; i < size; ++i )
			{
				collider.apply( _data.get( i, point ) ) ;
			}
		} ) ;

		//final long end = System.nanoTime() ;
		//System.out.println( "Separation: " + ( ( end - start ) / 1000000L ) ) ;

		contacts.clear() ;
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

	public static class Component extends ECSEntity.Component
	{
		private final Hull[] hulls ;

		private Component( final ECSEntity _parent, final Hull[] _hulls )
		{
			_parent.super() ;
			hulls = _hulls ;
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
			if( _obj instanceof Component b )
			{
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

			return false ;
		}

		@Override
		public String toString()
		{
			return "Hulls: " + hulls.length ;
		}
	}

	public interface ICollider
	{
		public void apply( final ContactPoint _point ) ;
	}
}
