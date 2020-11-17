package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public interface ICollisionDelegate
{
	public Hull generateContacts( final Hull _hull ) ;

	public Hull ray( final Vector2 _start, final Vector2 _end ) ;
	public Hull ray( final Vector2 _start, final Vector2 _end, final int[] _filters ) ;

	/**
		Inform the collision-system to ignore further 
		requests from this delegate and to stop any 
		processes linked to this delegate.
	*/
	public void shutdown() ;

	public interface ICallback
	{
		public void callback( ICollisionDelegate _delegate ) ;
	}
}
