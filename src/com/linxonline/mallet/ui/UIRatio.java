package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.Ratio ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class UIRatio
{
	private static UIRatio global = new UIRatio( 1, 1 ) ;

	private Ratio ratioX ;
	private Ratio ratioY ;
	private Ratio ratioZ ;

	public UIRatio( final int _pixelsX, final int _pixelsY )
	{
		ratioX = Ratio.calculateRatio( _pixelsX, 1 ) ;
		ratioY = Ratio.calculateRatio( _pixelsY, 1 ) ;
		ratioZ = Ratio.calculateRatio( 1, 1 ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public void toPixel( final Vector3 _unit, final Vector3 _pixel )
	{
		_pixel.setXYZ( toPixelX( _unit.x ), toPixelY( _unit.y ), toPixelZ( _unit.z ) ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public void toPixel( final Vector2 _unit, final Vector2 _pixel )
	{
		_pixel.setXY( toPixelX( _unit.x ), toPixelY( _unit.y ) ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public float toPixelX( final float _unit )
	{
		return _unit * ratioX.getA() ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public float toPixelY( final float _unit )
	{
		return _unit * ratioY.getA() ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public float toPixelZ( final float _unit )
	{
		return _unit * ratioZ.getA() ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public void toUnit( final Vector3 _pixel, final Vector3 _unit )
	{
		_unit.setXYZ( toUnitX( _pixel.x ), toUnitY( _pixel.y ), toUnitZ( _pixel.z ) ) ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public void toUnit( final Vector2 _pixel, final Vector2 _unit )
	{
		_unit.setXY( toUnitX( _pixel.x ), toUnitY( _pixel.y ) ) ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public float toUnitX( final float _pixel )
	{
		return _pixel / ratioX.getA() ; 
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public float toUnitY( final float _pixel )
	{
		return _pixel / ratioY.getA() ; 
	}
	
	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public float toUnitZ( final float _pixel )
	{
		return _pixel / ratioZ.getA() ; 
	}

	/**
		Change the global UI ratio for all UIElements.
		Pixels:unit. The unit can be whatever you want it to 
		be. mm, cm, inches.
		By default there is a 1:1 ratio, where the unit is pixel.
	*/
	public static void setGlobalUIRatio( final int _pixelsX, final int _pixelsY )
	{
		global.ratioX = Ratio.calculateRatio( _pixelsX, 1 ) ;
		global.ratioY = Ratio.calculateRatio( _pixelsY, 1 ) ;
	}

	public static UIRatio getGlobalUIRatio()
	{
		return global ;
	}
}
