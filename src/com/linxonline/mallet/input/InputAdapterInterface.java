package com.linxonline.mallet.input ;

import com.linxonline.mallet.maths.Vector2 ;

public interface InputAdapterInterface
{
	public float convertInputToRenderX( final float _x ) ;
	public float convertInputToRenderY( final float _y ) ;
	public Vector2 convertInputToRender( final Vector2 _input ) ;
}