package com.linxonline.mallet.physics ;

import java.util.ArrayList ;

import com.linxonline.mallet.physics.hulls.* ;

public class CollisionSystem
{
	private final ArrayList<Hull> hulls = new ArrayList<Hull>() ;

	public CollisionSystem() {}

	public void add( final Hull _hull )
	{
		if( exists( _hull ) == false )
		{
			hulls.add( _hull ) ;
		}
	}

	public void remove( final Hull _hull )
	{
		if( exists( _hull ) == true )
		{
			hulls.remove( _hull ) ;
		}
	}

	public void update( final float _dt )
	{
		Hull hull1 = null ;
		Hull hull2 = null ;
		final int size = hulls.size() ;
		
		for( int i = 0; i < size; i++ )
		{
			hull1 = hulls.get( i ) ;
			if( hull1.isCollidable() == false )
			{
				continue ;
			}

			for( int j = 0; j < size; j++ )
			{
				hull2 = hulls.get( j ) ;
				if( hull1 != hull2 )
				{
					if( hull1.isCollidableWithGroup( hull2.getGroupID() ) == true )
					{
						CollisionCheck.generateContactPoint( hull1, hull2 ) ;
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
