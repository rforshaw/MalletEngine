package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.OBB ;

public class CollisionAssist
{
	private static IAssist assist ;

	private CollisionAssist() {}

	public static void setAssist( final IAssist _assist )
	{
		assist = _assist ;
	}

	public static <T extends Hull> T add( T _hull )
	{
		return assist.add( _hull ) ;
	}

	public static <T extends Hull> void add( final T[] _hulls ) 
	{
		assist.add( _hulls ) ;
	}

	public static <T extends Hull> void remove( final T _hull )
	{
		assist.remove( _hull ) ;
	}

	public static <T extends Hull> void remove( final T[] _hulls )
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
		public <T extends Hull> T add( final T _hull ) ;
		public <T extends Hull> void add( final T[] _hulls ) ;

		public <T extends Hull> void remove( final T _hull ) ;
		public <T extends Hull> void remove( final T[] _hulls ) ;

		public ICollisionDelegate createCollisionDelegate() ;
		public void removeCollisionDelegate( ICollisionDelegate _delegate ) ;
	}
}
