package com.linxonline.mallet.renderer ;

/**
	Extend this class if you wish to load float
	values into a uniform.
	See Matrix4, Vector2, Vector3 for examples.
*/
public abstract class FloatUniform implements IUniform
{
	public abstract int fill( int _offset, final float[] _fill ) ;

	@Override
	public final IUniform.Type getType()
	{
		return IUniform.Type.FLOAT32 ;
	}
}
