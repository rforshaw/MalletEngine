package com.linxonline.mallet.input ;

import com.linxonline.mallet.maths.Vector2 ;

public interface InputAdapterInterface
{
	public float applyScreenOffsetX( final float _x ) ;
	public float applyScreenOffsetY( final float _y ) ;

	/**
		Convert the window mouse position x or y 
		into worldspace co-ordinates.
		This applies the camera position.
	*/
	public float convertInputToRenderX( final float _x ) ;
	public float convertInputToRenderY( final float _y ) ;
	public Vector2 convertInputToRender( final Vector2 _input ) ;

	/**
		Convert the window mouse position x or y 
		into UI co-ordinates.
	*/
	public float convertInputToUIRenderX( final float _x ) ;
	public float convertInputToUIRenderY( final float _y ) ;
	public Vector2 convertInputUIToRender( final Vector2 _input ) ;
}