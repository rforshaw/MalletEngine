package com.linxonline.mallet.util ;

public final class Tuple<T, U>
{
	private final T left ;
	private final U right ;
	
	public Tuple( final T _left, final U _right )
	{
		left = _left ;
		right = _right ;
	}

	public T getLeft()
	{
		return left ;
	}

	public U getRight()
	{
		return right ;
	}

	public static <T, U> Tuple<T, U> build( final T _left, final U _right )
	{
		return new Tuple<T, U>( _left, _right ) ;
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( '[' ) ;
		buffer.append( left.toString() ) ;
		buffer.append( " | " ) ;
		buffer.append( right.toString() ) ;
		buffer.append( ']' ) ;

		return buffer.toString() ;
	}
}
