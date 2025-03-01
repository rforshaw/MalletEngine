package com.linxonline.mallet.util ;

public class Interpolate
{
	private static final float TOLERANCE = 0.00001f ;

	private Interpolate() {}

	public static boolean linear( final float[] _future, final float[] _past, final float[] _present, final float _coefficient )
	{
		boolean requiresMore = false ;

		for( int i = 0; i < _future.length; ++i )
		{
			final float future = _future[i] ;
			final float past = _past[i] ;

			final float diff = ( future - past ) * _coefficient ;
			final float present = past + diff ;

			_present[i] = present ;
			_past[i] = present ;

			if( future != present )
			{
				requiresMore = true ;
			}
		}

		return requiresMore ;
	}
}
