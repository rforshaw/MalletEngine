package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.Interpolate ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class Camera
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;
	private final String id ;

	private final Vector3 oldUIPosition = new Vector3() ;
	private final Vector3 oldPosition = new Vector3() ;
	private final Vector3 oldRotation = new Vector3()  ;
	private final Vector3 oldScale = new Vector3( 1, 1, 1 ) ;

	private final Vector3 uiPosition = new Vector3() ;
	private final Vector3 position = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

	private final Vector3 currentUIPosition = new Vector3() ;
	private final Vector3 currentPosition = new Vector3() ;
	private final Vector3 currentRotation = new Vector3()  ;
	private final Vector3 currentScale = new Vector3( 1, 1, 1 ) ;

	private final Projection projection = new Projection() ;
	private final Screen displayScreen = new Screen() ;
	private final Screen renderScreen = new Screen() ;

	private final Vector2 screenOffset = new Vector2() ;
	private final Vector2 scaledRender = new Vector2() ;
	private final Vector2 ratio = new Vector2() ;

	public Camera( final String _id )
	{
		id = _id ;
	}

	public float convertInputToX( final float _x )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float halfRender = render.x * 0.5f ;

		final float t1 = ( ( ( _x - screenOffset.x ) * render.x ) / scaledRender.x ) - halfRender ;
		final float cam = ( t1 / scale.x ) + position.x ;
		return cam ;
	}

	public float convertInputToY( final float _y )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float halfRender = render.y * 0.5f ;

		final float t1 = ( ( ( _y - screenOffset.y ) * render.y ) / scaledRender.y ) - halfRender ;
		final float cam = ( t1 / scale.y ) + position.y  ;
		return cam ;
	}

	public float convertInputToUIX( final float _x )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		return ( ( ( _x - screenOffset.x ) * render.x ) / scaledRender.x ) + uiPosition.x ;
	}

	public float convertInputToUIY( final float _y )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		return ( ( ( _y - screenOffset.y ) * render.y ) / scaledRender.y ) + uiPosition.y ;
	}

	public Projection getProjection( final Camera.Projection _fill )
	{
		_fill.update( projection ) ;
		return _fill ;
	}

	public Screen getDisplayScreen( final Screen _fill )
	{
		_fill.update( displayScreen ) ;
		return _fill ;
	}

	public Screen getRenderScreen( final Screen _fill )
	{
		_fill.update( renderScreen ) ;
		return _fill ;
	}

	public String getID()
	{
		return id ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.x = _x ;
		position.y = _y ;
		position.z = _z ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.x = currentPosition.x ;
		_fill.y = currentPosition.y ;
		_fill.z = currentPosition.z ;
		return _fill ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		rotation.x = _x ;
		rotation.y = _y ; 
		rotation.z = _z ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		_fill.x = currentRotation.x ;
		_fill.y = currentRotation.y ;
		_fill.z = currentRotation.z ;
		return _fill ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		scale.x = _x ;
		scale.y = _y ;
		scale.z = _z ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		_fill.x = currentScale.x ;
		_fill.y = currentScale.y ;
		_fill.z = currentScale.z ;
		return _fill ;
	}

	public void setOrthographic( final float _top,
								 final float _bottom,
								 final float _left,
								 final float _right,
								 final float _near,
								 final float _far )
	{
		projection.nearPlane.setXYZ( _right - _left, _bottom - _top, _near ) ;
		projection.farPlane.setXYZ( projection.nearPlane.x, projection.nearPlane.y, _far ) ;

		final float invZ = 1.0f / ( _far - _near ) ;
		final float invY = 1.0f / ( _top - _bottom ) ;
		final float invX = 1.0f / ( _right - _left ) ;

		final Matrix4 proj = projection.matrix ;
		proj.set( 2.0f * invX, 0.0f,        0.0f,         ( -( _right + _left ) * invX ),
					0.0f,        2.0f * invY, 0.0f,         ( -( _top + _bottom ) * invY ),
					0.0f,        0.0f,        -2.0f * invZ, ( -( _far + _near ) * invZ ),
					0.0f,        0.0f,        0.0f,         1.0f ) ;
	}

	public void setUIPosition( final float _x, final float _y, final float _z )
	{
		uiPosition.x = _x ;
		uiPosition.y = _y ;
		uiPosition.z = _z ;
	}

	public Vector3 getUIPosition( final Vector3 _fill )
	{
		_fill.x = currentUIPosition.x ;
		_fill.y = currentUIPosition.y ;
		_fill.z = currentUIPosition.z ;
		return _fill ;
	}

	public void setScreenResolution( final int _width, final int _height )
	{
		renderScreen.setDimension( _width, _height ) ;
	}

	public void setScreenOffset( final int _x, final int _y )
	{
		renderScreen.setOffset( _x, _y ) ;
	}

	public void setDisplayResolution( final int _width, final int _height )
	{
		displayScreen.setDimension( _width, _height ) ;
	}

	public void setDisplayOffset( final int _x, final int _y )
	{
		displayScreen.setOffset( _x, _y ) ;
	}

	public Vector3 getDimensions( final Vector3 _fill )
	{
		_fill.x = projection.nearPlane.x ;
		_fill.y = projection.nearPlane.y ;
		_fill.z = projection.nearPlane.z ;
		return _fill ;
	}

	public void update( final int _diff, final int _iteration )
	{
		interpolate( uiPosition, oldUIPosition, currentUIPosition, _diff, _iteration ) ;
		interpolate( position,   oldPosition,   currentPosition,   _diff, _iteration ) ;
		interpolate( scale,      oldScale,      currentScale,      _diff, _iteration ) ;
		interpolate( rotation,   oldRotation,   currentRotation,   _diff, _iteration ) ;
	}

	private boolean interpolate( final Vector3 _future, final Vector3 _past, final Vector3 _present, final int _diff, final int _iteration )
	{
		if( Interpolate.linear( _future, _past, _present, _diff, _iteration ) )
		{
			// If an object has not reached its final state
			// then flag it for updating again during the next draw call.
			return true ;
		}
		
		return false ;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( "[ID: " ) ;
		builder.append( id ) ;
		builder.append( ", Screen Dim: " ) ;
		builder.append( renderScreen.dimension ) ;
		builder.append( ", Screen Offset: " ) ;
		builder.append( renderScreen.offset ) ;
		builder.append( ']' ) ;

		return builder.toString() ;
	}

	public int index()
	{
		return index ;
	}

	public enum Ratio
	{
		HOLD_RATIO,
		FILL_RATIO ;

		public static Vector2 calculateRatio( final Vector2 _ratio,
											  final Vector2 _render,
											  final Vector2 _display,
											  final Ratio _mode )
		{
			_ratio.setXY( _render.x / _display.x,
						  _render.y / _display.y ) ;

			switch( _mode )
			{
				case HOLD_RATIO :
				{
					// Choose a scaling Ratio that allows the renderDimensions to keep 
					// its aspect-ratio, but fill the display as much as possible 
					final float r = ( _ratio.x < _ratio.y ) ? _ratio.x : _ratio.y ;
					_ratio.setXY( r, r ) ;
					break ;
				}
				case FILL_RATIO :
				default         :
				{
					// Don't care about holding the renderDimensions natural aspect-ratio
					// just fill the display, will cause stretching
					_ratio.setXY( _ratio ) ;
					break ;
				}
			}

			return _ratio ;
		}

		public static Vector2 calculateScaleRender( final Vector2 _scale,
													final Vector2 _dimension,
													final Vector2 _ratio )
		{
			_scale.setXY( _dimension.x * _ratio.x, _dimension.y * _ratio.y ) ;
			return _scale ;
		}

		public static Vector2 calculateOffset( final Vector2 _offset,
											   final Vector2 _rOffset,
											   final Vector2 _dOffset )
		{
			_offset.setXY( _rOffset ) ;
			return _offset ;
		}
	}

	public static class Projection
	{
		public final Vector3 nearPlane = new Vector3() ;	// x = width, y = height, z = near
		public final Vector3 farPlane = new Vector3() ;		// x = width, y = height, z = far
		public final Matrix4 matrix = new Matrix4() ;		// Combined Model View and Projection Matrix

		public Projection() {}

		public Projection( final Projection _projection )
		{
			update( _projection ) ;
		}

		public void update( final Projection _projection )
		{
			nearPlane.setXYZ( _projection.nearPlane ) ;
			farPlane.setXYZ( _projection.farPlane ) ;
			matrix.set( _projection.matrix ) ;
		}

		@Override
		public String toString()
		{
			return "[Near: " + nearPlane.toString() + " Far: " + farPlane.toString() + " matrix " + matrix.toString() + "]" ; 
		}
	}

	/**
		Represents the size and position of the framebuffer that 
		this camera will render to.
	*/
	public static class Screen
	{
		public final Vector2 offset = new Vector2() ;
		public final Vector2 dimension = new Vector2() ;

		public Screen() {}

		public Screen( final Screen _screen )
		{
			update( _screen ) ;
		}

		public void update( final Screen _screen )
		{
			offset.setXY( _screen.offset ) ;
			dimension.setXY( _screen.dimension ) ;
		}
		
		public void setOffset( final float _x, final float _y )
		{
			offset.setXY( _x, _y ) ;
		}

		public void setDimension( final float _x, final float _y )
		{
			if( _x > 0.0f && _y > 0.0f )
			{
				dimension.setXY( _x, _y ) ;
			}
		}
		
		public static void setScreen( final Screen _screen, final int _x,
															final int _y,
															final int _width,
															final int _height )
		{
			setScreen( _screen, ( float )_x,
								( float )_y,
								( float )_width,
								( float )_height ) ;
		}
		
		public static void setScreen( final Screen _screen, final float _x,
															final float _y,
															final float _width,
															final float _height )
		{
			_screen.setOffset( ( int )_x, ( int )_y ) ;
			_screen.setDimension( ( int )_width, ( int )_height ) ;
		}

		@Override
		public String toString()
		{
			return "[Offset: " + offset.toString() + " Dimension: " + dimension.toString() + "]" ; 
		}
	}
}
