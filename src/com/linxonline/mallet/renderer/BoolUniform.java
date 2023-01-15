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

	@Override
	public IUniform.Type getType()
	{
		return IUniform.Type.BOOL ;
	}
}
