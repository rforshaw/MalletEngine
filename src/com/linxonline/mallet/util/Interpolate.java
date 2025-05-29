package com.linxonline.mallet.util ;

public class Interpolate
{
	private Interpolate() {}

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
}
