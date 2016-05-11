package com.linxonline.mallet.renderer.android.GL ;

import android.opengl.GLES30 ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

/**
	Represents the OpenGL state for a world.
	A world cannot interact with other worlds.
*/
public class GLWorld extends BasicWorld<GLDrawData, CameraData>
{
	private final static GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;

	public GLWorld( final String _id, final int _order, DrawState.RemoveDelegate _remove )
	{
		super( _id, _order, _remove ) ;
	}

	public void remove( final GLDrawData _data )
	{
		uploader.remove( _data ) ;
	}

	public void upload( final GLDrawData _data )
	{
		uploader.upload( _data ) ;
	}

	public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		uploader.draw( _worldProjection, _uiProjection ) ;
	}

	public void clean()
	{
		uploader.clean() ;
	}

	public void shutdown()
	{
		uploader.shutdown() ;
	}
}