package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public sealed interface IUniform permits
	BoolUniform,
	UIntUniform,
	IntUniform, IntVector2, IntVector3,
	FloatUniform, Vector2, Vector3,
	Matrix4,
	Texture, TextureArray,
	ArrayUniform, StructUniform,
	Font
{
	public interface IEach
	{
		public boolean each( final String _absoluteName, final IUniform _uniform ) ;
	}
}
