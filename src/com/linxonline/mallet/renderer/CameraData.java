package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

import com.linxonline.mallet.util.Interpolate ;

public class CameraData implements Camera
{
	private final String id ;

	private final Vector3 oldUIPosition = new Vector3() ;
	private final Vector3 oldPosition = new Vector3() ;
	private final Vector3 oldRotation = new Vector3()  ;
	private final Vector3 oldScale = new Vector3() ;

	private final Vector3 uiPosition ;
	private final Vector3 position ;
	private final Vector3 rotation ;
	private final Vector3 scale ;

	private final Vector3 currentUIPosition = new Vector3() ;
	private final Vector3 currentPosition = new Vector3() ;
	private final Vector3 currentRotation = new Vector3()  ;
	private final Vector3 currentScale = new Vector3() ;

	private final Projection projection = new Projection() ;
	private final Screen displayScreen = new Screen() ;
	private final Screen renderScreen = new Screen() ;

	private final Vector2 screenOffset = new Vector2() ;
	private final Vector2 scaledRender = new Vector2() ;
	private final Vector2 ratio = new Vector2() ;

	public CameraData( final String _id )
	{
		this( _id, new Vector3(), new Vector3(), new Vector3( 1, 1, 1 ) ) ;
	}

	public CameraData( final String _id,
					   final Vector3 _position,
					   final Vector3 _rotation,
					   final Vector3 _scale )
	{
		id = _id ;
		uiPosition = new Vector3() ;
		position = _position ;
		rotation = _rotation ;
		scale = _scale ;
	}

	@Override
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

	@Override
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

	@Override
	public float convertInputToUIX( final float _x )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		return ( ( ( _x - screenOffset.x ) * render.x ) / scaledRender.x ) + uiPosition.x ;
	}

	@Override
	public float convertInputToUIY( final float _y )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		return ( ( ( _y - screenOffset.y ) * render.y ) / scaledRender.y ) + uiPosition.y ;
	}

	public Vector3 getUIPosition()
	{
		return currentUIPosition ;
	}

	public Vector3 getPosition()
	{
		return currentPosition ;
	}

	public Vector3 getRotation()
	{
		return currentRotation ;
	}

	public Vector3 getScale()
	{
		return currentScale ;
	}

	public Projection getProjection()
	{
		return projection ;
	}

	public Screen getDisplayScreen()
	{
		return displayScreen ;
	}

	public Screen getRenderScreen()
	{
		return renderScreen ;
	}

	@Override
	public String getID()
	{
		return id ;
	}

	@Override
	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.setXYZ( position ) ;
		return _fill ;
	}

	@Override
	public void setRotation( final float _x, final float _y, final float _z )
	{
		rotation.setXYZ( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getRotation( final Vector3 _fill )
	{
		_fill.setXYZ( rotation ) ;
		return _fill ;
	}

	@Override
	public void setScale( final float _x, final float _y, final float _z )
	{
		scale.setXYZ( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getScale( final Vector3 _fill )
	{
		_fill.setXYZ( scale ) ;
		return _fill ;
	}

	public void setOrthographic( final float _top,
								 final float _bottom,
								 final float _left,
								 final float _right,
								 final float _near,
								 final float _far )
	{
		final CameraData.Projection projection = getProjection() ;

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

	@Override
	public void setUIPosition( final float _x, final float _y, final float _z )
	{
		uiPosition.setXYZ( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getUIPosition( final Vector3 _fill )
	{
		_fill.setXYZ( uiPosition ) ;
		return _fill ;
	}

	@Override
	public void setScreenResolution( final int _width, final int _height )
	{
		final CameraData.Screen screen = getRenderScreen() ;
		screen.setDimension( _width, _height ) ;
	}

	@Override
	public void setScreenOffset( final int _x, final int _y )
	{
		final CameraData.Screen screen = getRenderScreen() ;
		screen.setOffset( _x, _y ) ;
	}

	@Override
	public void setDisplayResolution( final int _width, final int _height )
	{
		final CameraData.Screen screen = getDisplayScreen() ;
		screen.setDimension( _width, _height ) ;
	}
	
	@Override
	public void setDisplayOffset( final int _x, final int _y )
	{
		final CameraData.Screen screen = getDisplayScreen() ;
		screen.setOffset( _x, _y ) ;
	}

	@Override
	public Vector3 getDimensions( final Vector3 _fill )
	{
		final CameraData.Projection projection = getProjection() ;
		_fill.setXYZ( projection.nearPlane ) ;
		return _fill ;
	}

	public void update( final int _diff, final int _iteration )
	{
		interpolate( uiPosition, oldUIPosition, currentUIPosition, _diff, _iteration ) ;
		interpolate( position,   oldPosition,   currentPosition,   _diff, _iteration ) ;
		interpolate( scale,      oldScale,      currentScale,      _diff, _iteration ) ;
		interpolate( rotation,   oldRotation,   currentRotation,   _diff, _iteration ) ;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( "[ID: " ) ;
		builder.append( id ) ;
		builder.append( ", Screen Dim: " ) ;
		builder.append( getRenderScreen().dimension ) ;
		builder.append( ", Screen Offset: " ) ;
		builder.append( getRenderScreen().offset ) ;
		builder.append( ']' ) ;
	
		return builder.toString() ;
	}

	private void interpolate( final Vector3 _future, final Vector3 _past, final Vector3 _present, final int _diff, final int _iteration )
	{
		if( Interpolate.linear( _future, _past, _present, _diff, _iteration ) )
		{
			// If an object has not reached its final state
			// then flag it for updating again during the next draw call.
			//forceUpdate() ;
		}
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
			//_offset.subtract( _rDimension ) ;
			//_offset.multiply( 0.5f ) ;
			return _offset ;
		}
	}

	public static class Projection
	{
		public final Vector3 nearPlane = new Vector3() ;	// x = width, y = height, z = near
		public final Vector3 farPlane = new Vector3() ;		// x = width, y = height, z = far
		public final Matrix4 matrix = new Matrix4() ;		// Combined Model View and Projection Matrix
	}

	/**
		Represents the size and position of the framebuffer that 
		this camera will render to.
	*/
	public static class Screen
	{
		public final Vector2 offset = new Vector2() ;
		public final Vector2 dimension = new Vector2() ;

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
	}
}
