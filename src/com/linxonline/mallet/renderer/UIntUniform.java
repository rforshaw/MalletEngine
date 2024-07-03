package com.linxonline.mallet.renderer ;

/**
	Extend this class if you wish to load uint
	values into a uniform.
	NOTE: Java does not have a concept of an unsigned int
	only signed int, the GPU will consider the unsigned 
	representation instead of the signed.
*/
public abstract class UIntUniform implements IUniform
{
	public abstract int fill( int _offset, final int[] _fill ) ;

	@Override
	public final IUniform.Type getType()
	{
		return IUniform.Type.UINT32 ;
	}
}
