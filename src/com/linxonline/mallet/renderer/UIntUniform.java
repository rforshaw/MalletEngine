package com.linxonline.mallet.renderer ;

public final class UIntUniform implements IUniform
{
	private int value ;

	public UIntUniform( final int _value )
	{
		value = _value ;
	}

	public int getState()
	{
		return value ;
	}
}
