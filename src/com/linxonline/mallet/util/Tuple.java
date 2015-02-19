package com.linxonline.mallet.util ;

public class Tuple<T, U>
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