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
		final GL3 gl = GLRenderer.getGL() ;

		final IntVector2 renPosition = getRenderPosition() ;
		final IntVector2 render = getRender() ;

		final IntVector2 disPosition = getDisplayPosition() ;
		final IntVector2 display = getDisplay() ;

		gl.glBindFramebuffer( GL3.GL_DRAW_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_STENCIL_BUFFER_BIT ) ;

		super.draw() ;

		gl.glBindFramebuffer( GL3.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		gl.glBindFramebuffer( GL3.GL_DRAW_FRAMEBUFFER, 0 ) ;
		gl.glBlitFramebuffer( renPosition.x, renPosition.y, render.x, render.y,
							  disPosition.x, disPosition.y, display.x, display.y, GL3.GL_COLOR_BUFFER_BIT , GL3.GL_LINEAR ) ;
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
		final GL3 gl = GLRenderer.getGL() ;
		final IntVector2 render = getRender() ;
		final IntVector2 display = getDisplay() ;

		final int frameOffset = FRAME_BUFFER ;
		final int renderOffset = COLOUR_BUFFER ;

		// First buffer is the Framebuffer.
		// Buffers afterwards are Renderbuffers.
		gl.glGenRenderbuffers( buffers.length - renderOffset, buffers, renderOffset ) ;

		updateBufferDimensions( render.x, render.y ) ;

		gl.glGenFramebuffers( 1, buffers, frameOffset ) ;
		gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0,  GL3.GL_RENDERBUFFER, buffers[COLOUR_BUFFER] ) ;
		gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_STENCIL_ATTACHMENT, GL3.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		//gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT,   GL3.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;
	}

	public void shutdown()
	{
		final GL3 gl = GLRenderer.getGL() ;
		final int frameOffset = FRAME_BUFFER ;
		final int renderOffset = COLOUR_BUFFER ;

		gl.glDeleteFramebuffers( 1, buffers, frameOffset ) ;
		gl.glDeleteRenderbuffers( buffers.length - renderOffset, buffers, renderOffset ) ;
		uploader.shutdown() ;
	}

	private void updateBufferDimensions( final int _width, final int _height )
	{
		final GL3 gl = GLRenderer.getGL() ;
		gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, buffers[COLOUR_BUFFER] ) ;
		gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_RGBA, _width, _height ) ;

		gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_STENCIL_INDEX8, _width, _height ) ;

		//gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;
		//gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, _width, _height ) ;
	}
}
