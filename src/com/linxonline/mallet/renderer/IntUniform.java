package com.linxonline.mallet.renderer ;

public final class IntUniform implements IUniform
{
	private int value ;

	public IntUniform( final int _value )
	{
		value = _value ;
	}

	public int getState()
	{
		return value ;
	}
}
