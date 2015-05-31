package com.linxonline.mallet.maths ;

/**
	Calculates the ratio between two integer values.
**/
public final class Ratio
{
	private final int ratioA ;
	private final int ratioB ;

	private Ratio( final int _a, final int _b )
	{
		ratioA = _a ;
		ratioB = _b ;
	}

	public final int getA()
	{
		return ratioA ;
	}

	public final int getB()
	{
		return ratioB ;
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "[" + ratioA + ":" + ratioB + "]" ) ;
		return buffer.toString() ;
	}

	/**
		Typically used to calculate the ratio of the screen resolution.
		But can still be used to determine the ratio of other variations.
	*/
	public static Ratio calculateRatio( final int _a, final int _b )
	{
		if( _a == 0 || _b == 0 )
		{
			return new Ratio( 0, 0 ) ;
		}

		final int ratio = Ratio.gcd( _a, _b ) ;
		return new Ratio( _a / ratio, _b / ratio ) ;
	}

	/**
		Greatest Common Divisor
	**/
	private static int gcd( final int _a, final int _b )
	{
		if( _b == 0 )
		{
			return _a ;
		}

		return gcd( _b, _a % _b ) ;
	}
}