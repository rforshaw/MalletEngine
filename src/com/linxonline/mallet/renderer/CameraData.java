package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class CameraData<T extends CameraData> implements Camera<T>
{
	private final Camera.DrawInterface<T> DRAW_DEFAULT = new Camera.DrawInterface<T>()
	{
		@Override
		public void draw( final T _data ) {}
	} ;

	private final String id ;

	private World world = null ;		// Store the handler to the worldspace this data is associated with
	private Camera.DrawInterface<T> draw = DRAW_DEFAULT ;

	private final Vector3 oldPosition = new Vector3() ;
	private final Vector3 oldRotation = new Vector3()  ;
	private final Vector3 oldScale = new Vector3() ;

	private final Vector3 position ;
	private final Vector3 rotation ;
	private final Vector3 scale ;

	private final Vector3 currentPosition = new Vector3() ;
	private final Vector3 currentRotation = new Vector3()  ;
	private final Vector3 currentScale = new Vector3() ;

	private final Projection projection = new Projection() ;
	private final Screen renderScreen = new Screen() ;

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
		position = _position ;
		rotation = _rotation ;
		scale = _scale ;
	}
	
	public void setWorld( final World _world )
	{
		world = _world ;
	}

	public World getWorld()
	{
		return world ;
	}

	public Vector3 getPosition()
	{
		return position ;
	}

	public Vector3 getRotation()
	{
		return rotation ;
	}

	public Vector3 getScale()
	{
		return scale ;
	}

	public Projection getProjection()
	{
		return projection ;
	}

	public Screen getRenderScreen()
	{
		return renderScreen ;
	}

	public String getID()
	{
		return id ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		rotation.setXYZ( _x, _y, _z ) ;
	}
	
	public void setScale( final float _x, final float _y, final float _z )
	{
		scale.setXYZ( _x, _y, _z ) ;
	}

	@Override
	public void setDrawInterface( final CameraData.DrawInterface<T> _draw )
	{
		draw = ( _draw == null ) ? DRAW_DEFAULT : _draw ;
	}

	protected void draw( final int _diff, final int _iteration )
	{
		interpolate( position, oldPosition, currentPosition, _diff, _iteration ) ;
		interpolate( scale,    oldScale,    currentScale,    _diff, _iteration ) ;
		interpolate( rotation, oldRotation, currentRotation, _diff, _iteration ) ;

		draw.draw( ( T )this ) ;
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
		final float xDiff = ( _future.x - _past.x ) / _diff ;
		final float yDiff = ( _future.y - _past.y ) / _diff ;
		final float zDiff = ( _future.z - _past.z ) / _diff ;

		_present.setXYZ( _past.x + ( xDiff * _iteration ),
						 _past.y + ( yDiff * _iteration ),
						 _past.z + ( zDiff * _iteration ) ) ;
		_past.setXYZ( _present ) ;
	}

	public static class Projection
	{
		public final Vector3 nearPlane = new Vector3() ;		// x = width, y = height, z = near
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
			dimension.setXY( _x, _y ) ;
		}
	}
}
