package com.linxonline.mallet.renderer.android.GL ;

import java.util.Set ;
import java.util.List ;

import android.opengl.GLES30 ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

/**
	Represents the OpenGL state for a world.
	A world cannot interact with other worlds.
*/
public class GLWorld extends BasicWorld<GLDrawData, CameraData>
{
	private final GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;

	public GLWorld( final String _id, final int _order )
	{
		super( _id, _order ) ;
	}

	/**
		Remove the Draw object from the World/GL state
	*/
	public static void remove( GLWorld _world, final GLDrawData _data )
	{
		_world.uploader.remove( _data ) ;
	}

	/**
		Add/Update the Draw object to the World/GL state
	*/
	public static void upload( final GLWorld _world, final GLDrawData _data )
	{
		_world.uploader.upload( _data ) ;
	}

	/**
		Render the world state to the passed in projections.
	*/
	public static void draw( final GLWorld _world, final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		_world.uploader.draw( _worldProjection, _uiProjection ) ;
	}

	/**
		Return a list of resources currently being used.
		Take the opportunity to also clear uploader 
		of empty buffers.
	*/
	public void clean( final Set<String> _activeKeys )
	{
		final DrawState<GLDrawData> state = getDrawState() ;
		final List<GLDrawData> list = state.getNewState() ;

		for( final GLDrawData draw : list )
		{
			draw.getUsedResources( _activeKeys ) ;
		}

		uploader.clean() ;
	}

	public void shutdown()
	{
		uploader.shutdown() ;
	}
}
