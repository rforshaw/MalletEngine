package com.linxonline.mallet.maths ;

/**
	Calculates the ratio between two integer values.
**/
public final class Ratio
{
	private int ratioA = 0 ;
	private int ratioB = 0 ;

	public Ratio( final int _a, final int _b )
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
		return new String( ratioA + ":" + ratioB ) ;
	}

	public static Ratio calculateRatio( final int _a, final int _b )
	{
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