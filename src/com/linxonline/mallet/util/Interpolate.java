package com.linxonline.mallet.util ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class Interpolate
{
	private Interpolate() {}

	/**
		Interpolate between the the past and future state and update 
		present to reflect the current iteration. If the present has 
		yet to reach the future state return true.

		_hertz represents the frequency of draw calls before the next 
		update cycle.

		_iteration represents the current draw iteration.
	*/
	public static boolean linear( final Vector3 _future, final Vector3 _past, final Vector3 _present, final int _hertz, final int _iteration )
	{
		final float xDiff = ( _future.x - _past.x ) / _hertz ;
		final float yDiff = ( _future.y - _past.y ) / _hertz ;
		final float zDiff = ( _future.z - _past.z ) / _hertz ;

		boolean requiresMore = false ;
		if( Math.abs( xDiff ) > 0.001f || Math.abs( yDiff ) > 0.001f || Math.abs( zDiff ) > 0.001f )
		{
			// If an object has not reached its final state
			requiresMore = true ;
		}

		_present.setXYZ( _past.x + ( xDiff * _iteration ),
						 _past.y + ( yDiff * _iteration ),
						 _past.z + ( zDiff * _iteration ) ) ;
		_past.setXYZ( _present ) ;
		return requiresMore ;
	}

	public static boolean linear( final FloatBuffer _future, final FloatBuffer _past, final FloatBuffer _present, final int _hertz, final int _iteration )
	{
		boolean requiresMore = false ;

		final int size = _future.size() ;
		for( int i = 0; i < size; ++i )
		{
			final float future = _future.get( i ) ;
			final float past = _past.get( i ) ;

			final float diff = ( future - past ) / _hertz ;
			if( Math.abs( diff ) > 0.001f )
			{
				requiresMore = true ;
			}

			final float present = past + ( diff * _iteration ) ;
			_present.set( i, present ) ;
			_past.set( i, present ) ;
		}

		return requiresMore ;
	}
}
