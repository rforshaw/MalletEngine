package com.linxonline.mallet.maths ;

public class MathUtil
{
	private MathUtil() {}

	public static int toInt( final StringBuilder _builder )
	{
		return toInt( _builder, 0, _builder.length() ) ;
	}

	public static int toInt( final StringBuilder _builder, final int _start, final int _end )
	{
		final boolean isNegative = ( _builder.charAt( 0 ) == '-' ) ? true : false ;
		final int start = ( isNegative == true ) ? _start + 1 : _start ;

		int num = 0 ;
		for( int i = start; i < _end; i++ )
		{
			num = ( num * 10 ) + Character.getNumericValue( _builder.charAt( i ) ) ;
		}

		return ( isNegative == true ) ? -num : num ;
	}

	public static long toLong( final StringBuilder _builder )
	{
		return toLong( _builder, 0, _builder.length() ) ;
	}

	public static long toLong( final StringBuilder _builder, final int _start, final int _end )
	{
		final boolean isNegative = ( _builder.charAt( 0 ) == '-' ) ? true : false ;
		final int start = ( isNegative == true ) ? _start + 1 : _start ;

		long num = 0 ;
		for( int i = start; i < _end; i++ )
		{
			num = ( num * 10L ) + ( long )Character.getNumericValue( _builder.charAt( i ) ) ;
		}

		return ( isNegative == true ) ? -num : num ;
	}

	public static float toFloat( final StringBuilder _builder )
	{
		return toFloat( _builder, 0, _builder.length() ) ;
	}

	public static float toFloat( final StringBuilder _builder, final int _start, final int _end )
	{
		final boolean isNegative = ( _builder.charAt( 0 ) == '-' ) ? true : false ;
		final int start = ( isNegative == true ) ? _start + 1 : _start ;

		boolean isDec = false ;
		int increment = 0 ;

		float num = 0.0f ;
		for( int i = start; i < _end; i++ )
		{
			final char c = _builder.charAt( i ) ;
			if( c == '.' )
			{
				isDec = true ;
				continue ;
			}

			increment += ( isDec == true ) ? 1 : 0 ;
			num = ( num * 10.0f ) + ( float )Character.getNumericValue( c ) ;
		}

		if( isNegative == true )
		{
			num = -num ;
		}

		return num / ( float )Math.pow( 10, increment ) ;
	}

	public static double toDouble( final StringBuilder _builder )
	{
		return toDouble( _builder, 0, _builder.length() ) ;
	}

	public static double toDouble( final StringBuilder _builder, final int _start, final int _end )
	{
		final boolean isNegative = ( _builder.charAt( 0 ) == '-' ) ? true : false ;
		final int start = ( isNegative == true ) ? _start + 1 : _start ;

		boolean isDec = false ;
		int increment = 0 ;

		double num = 0.0f ;
		for( int i = start; i < _end; i++ )
		{
			final char c = _builder.charAt( i ) ;
			if( c == '.' )
			{
				isDec = true ;
				continue ;
			}

			increment += ( isDec == true ) ? 1 : 0 ;
			num = ( num * 10.0 ) + ( double )Character.getNumericValue( c ) ;
		}

		if( isNegative == true )
		{
			num = -num ;
		}

		return num / Math.pow( 10, increment ) ;
	}
}
