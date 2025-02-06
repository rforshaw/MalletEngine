package com.linxonline.mallet.renderer ;

public final class BoolUniform implements IUniform
{
	private boolean bool ;

	public BoolUniform( final boolean _bool )
	{
		bool = _bool ;
	}

	public boolean getState()
	{
		return bool ;
	}
}
