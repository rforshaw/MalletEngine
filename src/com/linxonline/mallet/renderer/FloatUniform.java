package com.linxonline.mallet.renderer ;

/**
	Extend this class if you wish to load float
	values into a uniform.
	See Matrix4, Vector2, Vector3 for examples.
*/
public final class FloatUniform implements IUniform
{
	private float value ;

	public FloatUniform( final float _value )
	{
		value = _value ;
	}

	public float getState()
	{
		return value ;
	}
}
