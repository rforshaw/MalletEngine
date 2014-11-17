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
}