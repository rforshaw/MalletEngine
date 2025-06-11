package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;

import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLCamera
{
	private final Matrix4 uiMatrix = new Matrix4() ;		// Used for rendering GUI elements not impacted by World/Camera position
	private final Matrix4 viewMatrix = new Matrix4() ;		// Used for moving the camera around the world

	private final Matrix4 uiProjectionMatrix = new Matrix4() ;

	private final Vector3 uiPosition = new Vector3() ;

	private final Camera camera ;
	private final Camera.Screen screen = new Camera.Screen() ;
	private final Camera.Projection uiProjection = new Camera.Projection() ;
	private final Camera.Projection projection = new Camera.Projection() ;

	public GLCamera( final Camera _camera )
	{
		camera = _camera ;
		update( _camera ) ;
	}

	public void update( final Camera _camera )
	{
		_camera.getHUDPosition( uiPosition ) ;
		_camera.getViewMatrix( viewMatrix ) ;

		_camera.getRenderScreen( screen ) ;
		_camera.getProjection( Camera.Mode.HUD, uiProjection ) ;
		_camera.getProjection( Camera.Mode.WORLD, projection ) ;
	}

	public void draw( final List<GLBuffer> _buffers )
	{
		final int width = ( int )screen.dimension.x ;
		final int height = ( int )screen.dimension.y ;
		MGL.glViewport( 0, 0, width, height ) ;

		uiMatrix.setIdentity() ;
		uiMatrix.translate( -uiPosition.x, -uiPosition.y, 0.0f ) ;

		uiProjectionMatrix.setIdentity() ;
		Matrix4.multiply( uiProjection.matrix, uiMatrix, uiProjectionMatrix ) ;

		// Render all the buffers from the perspective of the camera.
		final int size = _buffers.size() ;
		for( int i = 0; i < size; ++i )
		{
			final GLBuffer buffer = _buffers.get( i ) ;
			buffer.draw( this ) ;
		}
	}

	public float getWidth()
	{
		return screen.dimension.x ;
	}

	public float getHeight()
	{
		return screen.dimension.y ;
	}

	public Matrix4 getUIProjection()
	{
		return uiProjectionMatrix ;
	}

	public Matrix4 getView()
	{
		return viewMatrix ;
	}

	public Matrix4 getProjection()
	{
		return projection.matrix ;
	}

	public Camera getCamera()
	{
		return camera ;
	}
}
