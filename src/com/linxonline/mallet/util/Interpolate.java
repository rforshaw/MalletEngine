package com.linxonline.mallet.util ;

public class Interpolate
{
	private Interpolate() {}

	public static boolean linear( final float[] _future, final float[] _past, final float[] _present, final float _coefficient )
	{
		boolean requiresMore = false ;

		for( int i = 0; i < _future.length; ++i )
		{
			final float future = _future[i] ;
			final float past = _past[i] ;

			final float present = past + ( future - past ) * _coefficient ;

			_present[i] = present ;
			_past[i] = present ;

			if( Math.abs( future - present ) > 0.001f )
			{
				requiresMore = true ;
			}
		}

		return requiresMore ;
	}
}
