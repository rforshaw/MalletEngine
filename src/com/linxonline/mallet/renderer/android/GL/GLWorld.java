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
	private final static int FRAME_BUFFER    = 0 ;
	private final static int COLOUR_BUFFER   = 1 ;
	private final static int STENCIL_BUFFER  = 2 ;
	//private final static int DEPTH_BUFFER    = 3 ;

	private final static int BUFFER_LENGTH   = 3 ;

	private final GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;
	private final int[] buffers = new int[BUFFER_LENGTH] ;

	public GLWorld( final String _id, final int _order )
	{
		super( _id, _order ) ;
	}

	@Override
	public void draw()
	{
		final IntVector2 renPosition = getRenderPosition() ;
		final IntVector2 render = getRender() ;

		final IntVector2 disPosition = getDisplayPosition() ;
		final IntVector2 display = getDisplay() ;

		GLES30.glBindFramebuffer( GLES30.GL_DRAW_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		GLES30.glClear( GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_STENCIL_BUFFER_BIT ) ;

		super.draw() ;

		GLES30.glBindFramebuffer( GLES30.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		GLES30.glBindFramebuffer( GLES30.GL_DRAW_FRAMEBUFFER, 0 ) ;
		GLES30.glBlitFramebuffer( renPosition.x, renPosition.y, render.x, render.y,
								  disPosition.x, disPosition.y, display.x, display.y, GLES30.GL_COLOR_BUFFER_BIT , GLES30.GL_LINEAR ) ;
	}

	@Override
	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		super.setRenderDimensions( _x, _y, _width, _height ) ;
		updateBufferDimensions( _width, _height ) ;
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

	public void init()
	{
		final IntVector2 render = getRender() ;
		final IntVector2 display = getDisplay() ;

		final int frameOffset = FRAME_BUFFER ;
		final int renderOffset = COLOUR_BUFFER ;

		// First buffer is the Framebuffer.
		// Buffers afterwards are Renderbuffers.
		GLES30.glGenRenderbuffers( buffers.length - renderOffset, buffers, renderOffset ) ; //GLRenderer.handleError( "Gen Render Buffers" ) ;

		updateBufferDimensions( render.x, render.y ) ;

		GLES30.glGenFramebuffers( 1, buffers, frameOffset ) ; 							//GLRenderer.handleError( "Gen Frame Buffers" ) ;
		GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ; 		//GLRenderer.handleError( "Gen Bind Buffers: " + buffers[FRAME_BUFFER] ) ;
		GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,  GLES30.GL_RENDERBUFFER, buffers[COLOUR_BUFFER] ) ; 	//GLRenderer.handleError( "Gen Attach Colour Buffers" ) ;
		GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ; 	//GLRenderer.handleError( "Gen Attach Stencil Buffers" ) ;
		//GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,   GLES30.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ; 	//GLRenderer.handleError( "Gen Render Buffers" ) ;
	}

	public void shutdown()
	{
		final int frameOffset = FRAME_BUFFER ;
		final int renderOffset = COLOUR_BUFFER ;

		GLES30.glDeleteFramebuffers( 1, buffers, frameOffset ) ;
		GLES30.glDeleteRenderbuffers( buffers.length - renderOffset, buffers, renderOffset ) ;
		uploader.shutdown() ;
	}

	private void updateBufferDimensions( final int _width, final int _height )
	{
		GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, buffers[COLOUR_BUFFER] ) ; 				//GLRenderer.handleError( "Bind Colour" ) ;
		GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, GLES30.GL_RGBA8, _width, _height ) ; 	//GLRenderer.handleError( "Storage Colour" ) ;

		GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ; 						//GLRenderer.handleError( "Bind Stencil" ) ;
		GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, GLES30.GL_STENCIL_INDEX8, _width, _height ) ; //GLRenderer.handleError( "Storage Stencil" ) ;

		//GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;
		//GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT, _width, _height ) ;
	}
}
