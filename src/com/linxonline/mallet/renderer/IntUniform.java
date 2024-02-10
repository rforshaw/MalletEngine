package com.linxonline.mallet.renderer ;

/**
	Extend this class if you wish to load float
	values into a uniform.
	See IntVector2, IntVector3 for examples.
*/
public abstract class IntUniform implements IUniform
{
	public abstract int fill( int _offset, final int[] _fill ) ;
	
	@Override
	public final IUniform.Type getType()
	{
		return IUniform.Type.INT32 ;
	}
}
