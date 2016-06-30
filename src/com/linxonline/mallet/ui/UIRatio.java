package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.Ratio ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class UIRatio
{
	private static UIRatio global = new UIRatio( 1 ) ;

	private Ratio ratio ;

	public UIRatio( final int _pixels )
	{
		ratio = Ratio.calculateRatio( _pixels, 1 ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public void toPixel( final Vector3 _unit, final Vector3 _pixel )
	{
		_pixel.setXYZ( toPixel( _unit.x ), toPixel( _unit.y ), toPixel( _unit.z ) ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public void toPixel( final Vector2 _unit, final Vector2 _pixel )
	{
		_pixel.setXY( toPixel( _unit.x ), toPixel( _unit.y ) ) ;
	}

	/**
		Take the unit and convert it to the appropriate 
		pixel value.
	*/
	public float toPixel( final float _unit )
	{
		return _unit * ratio.getA() ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public void toUnit( final Vector3 _pixel, final Vector3 _unit )
	{
		_unit.setXYZ( toUnit( _pixel.x ), toUnit( _pixel.y ), toUnit( _pixel.z ) ) ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public void toUnit( final Vector2 _pixel, final Vector2 _unit )
	{
		_unit.setXY( toUnit( _pixel.x ), toUnit( _pixel.y ) ) ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public float toUnit( final float _pixel )
	{
		return _pixel / ratio.getA() ; 
	}

	/**
		Change the global UI ratio for all UIElements.
		Pixels:unit. The unit can be whatever you want it to 
		be. mm, cm, inches.
		By default there is a 1:1 ratio, where the unit is pixel.
	*/
	public static void setGlobalUIRatio( final int _pixels )
	{
		global.ratio = Ratio.calculateRatio( _pixels, 1 ) ;
	}

	public static UIRatio getGlobalUIRatio()
	{
		return global ;
	}
}