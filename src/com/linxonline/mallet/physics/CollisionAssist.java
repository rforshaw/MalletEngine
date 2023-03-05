package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.OBB ;

import com.linxonline.mallet.physics.ICollisionDelegate ;
import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.hulls.Box2D ;

public class CollisionAssist
{
	private static IAssist assist ;

	private CollisionAssist() {}

	public static void setAssist( final IAssist _assist )
	{
		assist = _assist ;
	}

	public static Box2D createBox2D( final AABB _aabb, final int[] _collidables )
	{
		return assist.createBox2D( _aabb, _collidables ) ;
	}

	public static Box2D createBox2D( final OBB _obb, final int[] _collidables )
	{
		return assist.createBox2D( _obb, _collidables ) ;
	}

	public static void remove( final Hull _hull )
	{
		assist.remove( _hull ) ;
	}

	public static void remove( final Hull[] _hulls )
	{
		assist.remove( _hulls ) ;
	}

	public static ICollisionDelegate createCollisionDelegate()
	{
		return assist.createCollisionDelegate() ;
	}

	public static void removeCollisionDelegate( ICollisionDelegate _delegate )
	{
		assist.removeCollisionDelegate( _delegate ) ;
	}

	public interface IAssist
	{
		public Box2D createBox2D( final AABB _aabb, final int[] _collidables ) ;
		public Box2D createBox2D( final OBB _obb, final int[] _collidables ) ;

		public void remove( final Hull _hull ) ;
		public void remove( final Hull[] _hulls ) ;

		public ICollisionDelegate createCollisionDelegate() ;
		public void removeCollisionDelegate( ICollisionDelegate _delegate ) ;
	}
}
