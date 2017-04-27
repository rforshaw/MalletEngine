package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Set ;
import java.util.List ;

import javax.media.opengl.* ;

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

	public GLGeometryUploader getUploader()
	{
		return uploader ;
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
