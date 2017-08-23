package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.MalletTexture ;

import com.linxonline.mallet.maths.* ;

public class UI
{
	public enum Unit
	{
		CENTIMETRE( 0.393701f ),
		INCH( 1.0f ) ;

		public final float conversion ;

		private Unit( final float _conversion )
		{
			conversion = _conversion ;
		}

		public int convert( final int _dpi )
		{
			return ( int )( _dpi * conversion ) ;
		}

		public static Unit derive( final String _type )
		{
			if( _type == null )
			{
				return CENTIMETRE ;
			}

			if( _type.isEmpty() == true )
			{
				return CENTIMETRE ;
			}

			return Unit.valueOf( _type ) ;
		}
	} ;

	public enum Modifier
	{
		RETAIN_ASPECT_RATIO,
		NONE ;

		public static Modifier derive( final String _type )
		{
			if( _type == null )
			{
				return NONE ;
			}

			if( _type.isEmpty() == true )
			{
				return NONE ;
			}

			return Modifier.valueOf( _type ) ;
		}
	} ;

	public enum Alignment
	{
		LEFT,
		CENTRE,
		RIGHT,
		TOP,
		BOTTOM ;

		public static Alignment derive( final String _type )
		{
			if( _type == null )
			{
				return LEFT ;
			}

			if( _type.isEmpty() == true )
			{
				return LEFT ;
			}

			return Alignment.valueOf( _type ) ;
		}
	} ;

	public static void align( final Alignment _alignX, final Alignment _alignY, final Vector3 _val, final Vector3 _dim, final Vector3 _length )
	{
		_val.x = UI.align( _alignX, _dim.x, _length.x ) ;
		_val.y = UI.align( _alignY, _dim.y, _length.y ) ;
	}

	public static float align( final Alignment _align, final float _dim, final float _length )
	{
		switch( _align )
		{
			case CENTRE :
			{
				return ( _length / 2.0f ) - ( _dim / 2.0f ) ;
			}
			case BOTTOM :
			case RIGHT  :
			{
				return _length - _dim ;
			}
			case TOP  :
			case LEFT :
			default   :
			{
				return 0.0f ;
			}
		}
	}

	/**
		Calculate the width and height covered by the uv-coordinates.
		Apply calculation to _dim.
	*/
	public static void calcSubDimension( final Vector3 _dim, final MalletTexture _tex, final UIElement.UV _uv )
	{
		_dim.x = _tex.getSubWidth( _uv.min.x, _uv.max.x ) ;
		_dim.y = _tex.getSubHeight( _uv.min.y, _uv.max.y ) ;
	}
	
	/**
		Set _val to use as much or as little space 
		that is provided by _length, depending on the Modifier. 
		For most cases _length represents the volume 
		that a UIElement has.

		RETAIN_ASPECT_RATIO - will ensure that _val 
		adheres to the aspect ratio defined by _ratio.

		NONE : Set _val to _length.
	*/
	public static void fill( final Modifier _mod, final Vector3 _val, final Vector3 _ratio, final Vector3 _length )
	{
		switch( _mod )
		{
			case RETAIN_ASPECT_RATIO :
			{
				final float ratioX = _ratio.x > 0.0f ?  _length.x / _ratio.x : 0.0f ;
				final float ratioY = _ratio.y > 0.0f ?  _length.y / _ratio.y : 0.0f ;
				final float ratioZ = _ratio.z > 0.0f ?  _length.z / _ratio.z : 0.0f ;

				final float ratio = findBestRatio( ratioX, ratioY, ratioZ ) ;
				_val.setXYZ( _ratio.x * ratio, _ratio.y * ratio, _ratio.z * ratio ) ;
				break ;
			}
			case NONE              :
			{
				_val.setXYZ( _length ) ;
				break ;
			}
		}
	}

	/**
		Apply _x, _y and _z to passed in Vector3 _update.
		Return true if the values are significantly different 
		from the values current in _update, else return false 
		if the values have no major difference.

		Used by UIElement to determine if position, offset, 
		margin and others require makeDirty to be called.
	*/
	public static boolean applyVec3( final Vector3 _update, final float _x, final float _y, final float _z )
	{
		final float xDiff = _update.x - _x ;
		final float yDiff = _update.x - _y ;
		final float zDiff = _update.x - _z ;

		_update.setXYZ( _x, _y, _z ) ;
		return Math.abs( xDiff ) > 0.001f || Math.abs( yDiff ) > 0.001f || Math.abs( zDiff ) > 0.001f ;
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
