package com.linxonline.mallet.util ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class Interpolate
{
	private Interpolate() {}

	public static boolean none( final float[] _future, final float[] _present, final float _coefficient )
	{
		FloatBuffer.copy( _future, _present ) ;
		return false ;
	}

	public static boolean linear( final float[] _future, final float[] _present, final float _coefficient )
	{
		boolean requiresMore = false ;

		for( int i = 0; i < _future.length; ++i )
		{
			final float future = _future[i] ;

			float present = _present[i] ;
			present += ( future - present ) * _coefficient ;
			if( Math.abs( future - present ) > 0.001f )
			{
				requiresMore = true ;
			}

			_present[i] = present ;
		}

		return requiresMore ;
	}

	@FunctionalInterface
	public interface IMode
	{
		public boolean interpolate( final float[] _future, final float[] _present, final float _coefficient ) ;
	}
}
