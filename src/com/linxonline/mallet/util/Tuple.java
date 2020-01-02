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

	public static <T, U> Tuple<T, U> build( final T _left, final U _right )
	{
		return new Tuple<T, U>( _left, _right ) ;
	}

	@Override
	public int hashCode()
	{
		int hash = 7 ;
		hash = 31 * hash + left.hashCode() ;
		hash = 31 * hash + right.hashCode() ;
		return hash ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj == null )
		{
			return false ;
		}

		if( _obj == this )
		{
			return true ;
		}

		if( _obj instanceof Tuple )
		{
			final Tuple tuple = ( Tuple )_obj ;
			return left.equals( tuple.getLeft() ) && right.equals( tuple.getRight() ) ;
		}

		return false ;
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
