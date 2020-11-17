package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.IntVector2 ;

import com.linxonline.mallet.util.notification.Notification.Notify ;

public final class WorldAssist
{
	private static Assist assist ;

	private WorldAssist() {}

	public static void setAssist( final WorldAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static World getDefault()
	{
		return assist.getDefault() ;
	}

	/**
		Add the world to the rendering system so it 
		can be rendered out.
		A world should not be added multiple times.
	*/
	public static World add( final World _world )
	{
		return assist.add( _world ) ;
	}

	/**
		Remove the World from being rendered out.
		Removing the world does not destroy any 
		resources it may have.
	*/
	public static World remove( final World _world )
	{
		return assist.remove( _world ) ;
	}

	public static World update( final World _world )
	{
		return assist.update( _world ) ;
	}

	public interface Assist
	{
		public World getDefault() ;

		public World add( final World _world ) ;
		public World remove( final World _world ) ;
		public World update( final World _world ) ;
	}
}
