package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;

/**
	Camera handler.
*/
public interface Camera
{
	public String getID() ;
	
	public void setPosition( final float _x, final float _y, final float _z ) ;
	public Vector3 getPosition( final Vector3 _fill ) ;

	public void setRotation( final float _x, final float _y, final float _z ) ;
	public Vector3 getRotation( final Vector3 _fill ) ;

	public void setScale( final float _x, final float _y, final float _z ) ;
	public Vector3 getScale( final Vector3 _fill ) ;

	public void setOrthographic( final float _top,
								 final float _bottom,
								 final float _left,
								 final float _right,
								 final float _near,
								 final float _far ) ;
								 
	public void setUIPosition( final float _x, final float _y, final float _z ) ;
	public Vector3 getUIPosition( final Vector3 _fill ) ;

	public void setScreenResolution( final int _width, final int _height ) ;
	public void setScreenOffset( final int _x, final int _y ) ;

	public void setDisplayResolution( final int _width, final int _height ) ;
	public void setDisplayOffset( final int _x, final int _y ) ;

	public Vector3 getDimensions( final Vector3 _fill ) ;

	public float convertInputToX( final float _inputX ) ;
	public float convertInputToY( final float _inputY ) ;

	public float convertInputToUIX( final float _inputX ) ;
	public float convertInputToUIY( final float _inputY ) ;
}
