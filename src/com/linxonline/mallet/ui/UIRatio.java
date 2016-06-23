package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.Ratio ;

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
	public float scale( final float _unit )
	{
		return _unit * ratio.getA() ;
	}

	/**
		Take the pixel and convert it to the appropriate 
		unit value.
	*/
	public float descale( final float _pixel )
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