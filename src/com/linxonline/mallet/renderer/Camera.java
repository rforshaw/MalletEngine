package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public final class Camera
{
	private static final float PI = ( float )Math.PI ;
	private static final float PI2 = ( float )Math.PI * 2.0f ;
	private final static Utility utility = new Utility() ;

	private static final int POSITION    = 0 ;
	private static final int ROTATION    = 3 ;
	private static final int SCALE       = 6 ;
	private static final int HUD_POSITION = 9 ;

	private final int index = utility.getGlobalIndex() ;
	private final String id ;

	// Each contain Position, Rotation, and Scale
	private final float[] old = FloatBuffer.allocate( 12 ) ;
	private final float[] present = FloatBuffer.allocate( 12 ) ;
	private final float[] future = FloatBuffer.allocate( 12 ) ;

	private final Vector3 up = new Vector3( 0.0f, 1.0f, 0.0f ) ;

	private final Vector3 eye = new Vector3() ;
	private final Vector3 direction = new Vector3() ;

	private final Projection hudProjection = new Projection() ;
	private final Projection worldProjection = new Projection() ;

	private final Screen displayScreen = new Screen() ;
	private final Screen renderScreen = new Screen() ;

	private final Vector2 screenOffset = new Vector2() ;
	private final Vector2 scaledRender = new Vector2() ;
	private final Vector2 ratio = new Vector2() ;

	public Camera( final String _id )
	{
		id = _id ;
		FloatBuffer.set( old, SCALE, 1.0f, 1.0f, 1.0f ) ;
		FloatBuffer.set( present, SCALE, 1.0f, 1.0f, 1.0f ) ;
		FloatBuffer.set( future, SCALE, 1.0f, 1.0f, 1.0f ) ;
	}

	public float projectXToWorld( final float _x )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float halfRender = render.x * 0.5f ;
		final float posX = FloatBuffer.get( future, POSITION + 0 ) ;
		final float scaleX = FloatBuffer.get( future, SCALE + 0 ) ;

		final float t1 = ( ( ( _x - screenOffset.x ) * render.x ) / scaledRender.x ) - halfRender ;
		final float cam = ( t1 / scaleX ) + posX ;
		return cam ;
	}

	public float projectYToWorld( final float _y )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float halfRender = render.y * 0.5f ;
		final float posY = FloatBuffer.get( future, POSITION + 1 ) ;
		final float scaleY = FloatBuffer.get( future, SCALE + 1 ) ;

		final float t1 = ( ( ( _y - screenOffset.y ) * render.y ) / scaledRender.y ) - halfRender ;
		final float cam = ( t1 / scaleY ) + posY ;
		return cam ;
	}

	public float projectXToHUD( final float _x )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float posX = FloatBuffer.get( future, HUD_POSITION + 0 ) ;

		return ( ( ( _x - screenOffset.x ) * render.x ) / scaledRender.x ) + posX ;
	}

	public float projectYToHUD( final float _y )
	{
		final Vector2 render = renderScreen.dimension ;
		final Vector2 display = displayScreen.dimension ;

		Ratio.calculateRatio( ratio, render, display, Ratio.FILL_RATIO ) ;
		Ratio.calculateScaleRender( scaledRender, render, ratio ) ;
		Ratio.calculateOffset( screenOffset, renderScreen.offset, displayScreen.offset ) ;

		final float posY = FloatBuffer.get( future, HUD_POSITION + 1 ) ;

		return ( ( ( _y - screenOffset.y ) * render.y ) / scaledRender.y ) + posY ;
	}

	public float projectXTo( final Camera.Mode _mode, final float _x )
	{
		switch( _mode )
		{
			default    : return projectXToWorld( _x ) ;
			case WORLD : return projectXToHUD( _x ) ;
		}
	}

	public float projectYTo( final Camera.Mode _mode, final float _y )
	{
		switch( _mode )
		{
			default    : return projectYToWorld( _y ) ;
			case WORLD : return projectYToHUD( _y ) ;
		}
	}

	public Projection getProjection( final Mode _mode, final Projection _fill )
	{
		switch( _mode )
		{
			default    :
			case WORLD : _fill.update( worldProjection ) ; break ;
			case HUD   : _fill.update( hudProjection ) ; break ;
		}

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
		FloatBuffer.set( future, POSITION, _x, _y, _z ) ;
	}

	public void addToPosition( final float _x, final float _y, final float _z )
	{
		FloatBuffer.add( future, POSITION, _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, POSITION ) ;
	}

	public Vector3 getFuturePosition( final Vector3 _fill )
	{
		return FloatBuffer.fill( future, _fill, POSITION ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		float oX = FloatBuffer.get( old, ROTATION + 0 ) ;
		float oY = FloatBuffer.get( old, ROTATION + 1 ) ;
		float oZ = FloatBuffer.get( old, ROTATION + 2 ) ;

		final float diffX = Math.abs( _x - oX ) ;
		if( diffX > PI )
		{
			oX += ( _x > oX ) ? PI2 : -PI2 ;
		}

		final float diffY = Math.abs( _y - oY ) ;
		if( diffY > PI )
		{
			oY += ( _y > oY ) ? PI2 : -PI2 ;
		}

		final float diffZ = Math.abs( _z - oZ ) ;
		if( diffZ > PI )
		{
			oZ += ( _z > oZ ) ? PI2 : -PI2 ;
		}

		FloatBuffer.set( old, ROTATION, oX, oY, oZ ) ;
		FloatBuffer.set( future, ROTATION, _x, _y, _z ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, ROTATION ) ;
	}

	public Vector3 getFutureRotation( final Vector3 _fill )
	{
		return FloatBuffer.fill( future, _fill, ROTATION ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, SCALE, _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, SCALE ) ;
	}

	public void lookAt( final float _x, final float _y, final float _z )
	{
		direction.setXYZ( _x, _y, _z ) ;
		direction.subtract( getFuturePosition( eye ) ) ;
		direction.normalise() ;

		final float yaw = ( float )Math.atan2( direction.x, -direction.z ) ;
		final float pitch = ( float )Math.asin( direction.y ) ;

		final float leftX = ( float )Math.sin( yaw ) ;
		final float leftZ = ( float )Math.cos( yaw ) ;

		float roll = ( float )Math.asin( up.x * leftX + up.z * leftZ ) ;
		if( up.y < 0.0f )
		{
			roll = ( Math.signum( roll ) ) * PI - roll ;
		}

		setRotation( pitch, yaw, roll ) ;
	}

	/**
		Apply the orthographic to both the World,
		and HUD projections.
	*/
	public void setOrthographic( final Mode _mode,
								 final float _top,
								 final float _bottom,
								 final float _left,
								 final float _right,
								 final float _near,
								 final float _far )
	{
		switch( _mode )
		{
			default    :
			case WORLD : updateOrtho( worldProjection, _top, _bottom, _left, _right, _near, _far ) ; break ;
			case HUD   : updateOrtho( hudProjection, _top, _bottom, _left, _right, _near, _far ) ; break ;
		}
	}

	public void setOrthographic( final float _top,
								 final float _bottom,
								 final float _left,
								 final float _right,
								 final float _near,
								 final float _far )
	{
		updateOrtho( worldProjection, _top, _bottom, _left, _right, _near, _far ) ;
		updateOrtho( hudProjection, _top, _bottom, _left, _right, _near, _far ) ;
	}

	private static void updateOrtho( final Projection _projection,
									 final float _top,
									 final float _bottom,
									 final float _left,
									 final float _right,
									 final float _near,
									 final float _far )
	{
		_projection.nearPlane.setXYZ( _right - _left, _bottom - _top, _near ) ;
		_projection.farPlane.setXYZ( _projection.nearPlane.x, _projection.nearPlane.y, _far ) ;

		final float invZ = 1.0f / ( _far - _near ) ;
		final float invY = 1.0f / ( _top - _bottom ) ;
		final float invX = 1.0f / ( _right - _left ) ;

		final Matrix4 proj = _projection.matrix ;
		proj.set( 2.0f * invX, 0.0f,        0.0f,        ( -( _right + _left ) * invX ),
				  0.0f,        2.0f * invY, 0.0f,        ( -( _top + _bottom ) * invY ),
				  0.0f,        0.0f,       -2.0f * invZ, ( -( _far + _near ) * invZ ),
				  0.0f,        0.0f,        0.0f,        1.0f ) ;
	}

	public void setPerspective( final Mode _mode, final float _fov, final float _near, final float _far )
	{
		final float aspectRatio = renderScreen.dimension.x / renderScreen.dimension.y ;
		setPerspective( _mode, _fov, aspectRatio, _near, _far ) ;
	}

	public void setPerspective( final Mode _mode, final float _fov, final float _aspectRatio, final float _near, final float _far )
	{
		final float scale = ( float )Math.tan( _fov * 0.5f * PI / 180.0f ) * _near ; 
		final float left = _aspectRatio * scale ;
		final float right = -left ;
		 
		final float bottom = scale ;
		final float top = -bottom ;

		setPerspective( _mode, top, bottom, left, right, _near, _far ) ;
	}

	public void setPerspective( final Mode _mode,
								final float _top,
								final float _bottom,
								final float _left,
								final float _right,
								final float _near,
								final float _far )
	{
		switch( _mode )
		{
			default    :
			case WORLD : setPerspective( worldProjection, _top, _bottom, _left, _right, _near, _far ) ; break ;
			case HUD   : setPerspective( hudProjection, _top, _bottom, _left, _right, _near, _far ) ; break ;
		}
	}

	private static void setPerspective( final Projection _projection,
										final float _top,
										final float _bottom,
										final float _left,
										final float _right,
										final float _near,
										final float _far )
	{
		_projection.nearPlane.setXYZ( _right - _left, _bottom - _top, _near ) ;
		_projection.farPlane.setXYZ( _projection.nearPlane.x, _projection.nearPlane.y, _far ) ;

		final float invZ = 1.0f / ( _far - _near ) ;
		final float invY = 1.0f / ( _top - _bottom ) ;
		final float invX = 1.0f / ( _right - _left ) ;
		
		final float m00 = 2.0f * _near * invX ;
		final float m11 = 2.0f * _near * invY ;
		final float m20 = ( _right + _left ) * invX ;
		final float m21 = ( _top + _bottom ) * invY ;

		final float m22 = -( ( _far + _near ) * invZ ) ;
		final float m23 = -( ( 2.0f * _far * _near ) * invZ ) ;

		final Matrix4 proj = _projection.matrix ;
		proj.set( m00,  0.0f,  m20, 0.0f,
				  0.0f, m11,   m21, 0.0f,
				  0.0f, 0.0f,  m22,  m23,
				  0.0f, 0.0f, -1.0f, 0.0f ) ;
	}
	
	public void setProjection( final Mode _mode, final Projection _projection )
	{
		switch( _mode )
		{
			default    :
			case WORLD : worldProjection.update( _projection ) ; break ;
			case HUD   : hudProjection.update( _projection ) ; break ;
		}
	}

	public void setHUDPosition( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, HUD_POSITION, _x, _y, _z ) ;
	}

	public Vector3 getHUDPosition( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, HUD_POSITION ) ;
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

	public Vector3 getDimensions( final Mode _mode, final Vector3 _fill )
	{
		switch( _mode )
		{
			default    :
			case WORLD : return getDimensions( worldProjection, _fill ) ;
			case HUD   : return getDimensions( hudProjection, _fill ) ;
		}
	}

	private static Vector3 getDimensions( final Projection _projection, final Vector3 _fill )
	{
		_fill.x = _projection.nearPlane.x ;
		_fill.y = _projection.nearPlane.y ;
		_fill.z = _projection.nearPlane.z ;
		return _fill ;
	}

	public boolean update( final int _diff, final int _iteration )
	{
		boolean update = false ;
		update |= Interpolate.linear( future, old, present, _diff, _iteration ) ;
		return update ;
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

	public enum Mode
	{
		WORLD,
		HUD
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
