package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.* ;

public class UI
{
	public enum Modifier
	{
		RetainAspectRatio,
		None ;
	} ;

	/**
		Set _val to use as much or as little space 
		that is provided by _length, depending on the Modifier. 
		For most cases _length represents the volume 
		that a UIElement has.

		RetainAspectRatio - will ensure that _val 
		adheres to the aspect ratio defined by _ratio.

		None : Set _val to _length.
	*/
	public static void fill( final Modifier _mod, final Vector3 _val, final Vector3 _ratio, final Vector3 _length )
	{
		switch( _mod )
		{
			case RetainAspectRatio :
			{
				final float ratioX = _ratio.x > 0.0f ?  _length.x / _ratio.x : 0.0f ;
				final float ratioY = _ratio.y > 0.0f ?  _length.y / _ratio.y : 0.0f ;
				final float ratioZ = _ratio.z > 0.0f ?  _length.z / _ratio.z : 0.0f ;

				final float ratio = findBestRatio( ratioX, ratioY, ratioZ ) ;
				_val.setXYZ( _ratio.x * ratio, _ratio.y * ratio, _ratio.z * ratio ) ;
				break ;
			}
			case None              :
			{
				_val.setXYZ( _length ) ;
				break ;
			}
		}
	}

	/**
		The best ratio is the one that is smallest.
		A ratio of 0.0 however means that the axis is not being used.
		That axis should be ignored when trying to find the best ratio.
	*/
	private static float findBestRatio( final float _ratioX, final float _ratioY, final float _ratioZ )
	{
		// Only check an axis if it is greater than zero.
		// An axis less than zero is considered to be not in use.
		// This will most likely be the z-axis while developing 
		// 2D user interfaces. 
		if( _ratioX > 0.0f )
		{
			if( compare( _ratioX, _ratioY ) && compare( _ratioX, _ratioZ ) )
			{
				return _ratioX ;
			}
		}

		if( _ratioY > 0.0f )
		{
			if( compare( _ratioY, _ratioX ) && compare( _ratioY, _ratioZ ) )
			{
				return _ratioY ;
			}
		}

		if( _ratioZ > 0.0f )
		{
			if( compare( _ratioZ, _ratioX ) && compare( _ratioZ, _ratioY ) )
			{
				return _ratioZ ;
			}
		}

		// We should only reach here if all axis are zero
		// It is impossible for a rock-paper-scissors to happen.
		return _ratioX ;
	}

	private static boolean compare( final float _compare, final float _to )
	{
		if( _to > 0.0 )
		{
			return _compare <= _to ;
		}

		return true ;
	}
}
