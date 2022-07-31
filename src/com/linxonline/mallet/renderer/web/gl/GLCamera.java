package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;

import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLCamera
{
	private final Matrix4 uiMatrix = new Matrix4() ;		// Used for rendering GUI elements not impacted by World/Camera position
	private final Matrix4 worldMatrix = new Matrix4() ;		// Used for moving the camera around the world

	private final Matrix4 worldProjection = new Matrix4() ;
	private final Matrix4 uiProjection = new Matrix4() ;

	private final Vector3 uiPosition = new Vector3() ;
	private final Vector3 position = new Vector3() ;
	private final Vector3 scale = new Vector3() ;

	private final Camera.Screen screen = new Camera.Screen() ;
	private final Camera.Projection projection = new Camera.Projection() ;

	public GLCamera( final Camera _camera )
	{
		update( _camera ) ;
	}

	public void update( final Camera _camera )
	{
		_camera.getUIPosition( uiPosition ) ;
		_camera.getPosition( position ) ;
		_camera.getScale( scale ) ;

		_camera.getRenderScreen( screen ) ;
		_camera.getProjection( projection ) ;
	}

	public void draw( final List<GLBuffer> _buffers )
	{
		final int width = ( int )screen.dimension.x ;
		final int height = ( int )screen.dimension.y ;
		MGL.viewport( 0, 0, width, height ) ;

		worldMatrix.setIdentity() ;
		worldMatrix.translate( projection.nearPlane.x / 2 , projection.nearPlane.y / 2, 0.0f ) ;
		worldMatrix.scale( scale.x, scale.y, scale.z ) ;
		worldMatrix.translate( -position.x, -position.y, 0.0f ) ;

		uiMatrix.setIdentity() ;
		uiMatrix.translate( -uiPosition.x, -uiPosition.y, 0.0f ) ;

		worldProjection.setIdentity() ;
		Matrix4.multiply( projection.matrix, worldMatrix, worldProjection ) ;

		uiProjection.setIdentity() ;
		Matrix4.multiply( projection.matrix, uiMatrix, uiProjection ) ;

		// Render all the buffers from the perspective of the camera.
		for( final GLBuffer buffer : _buffers )
		{
			//worldProjection, uiProjection
			buffer.draw( ( buffer.isUI() ) ? uiProjection : worldProjection ) ;
		}
	}
}
