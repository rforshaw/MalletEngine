package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Set ;
import java.util.List ;

import com.jogamp.opengl.* ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

/**
	Represents the OpenGL state for a world.
	A world cannot interact with other worlds.
*/
public class GLWorld extends BasicWorld<GLDrawData, CameraData>
{
	protected final static int FRAME_BUFFER    = 0 ;
	protected final static int COLOUR_BUFFER   = 1 ;
	protected final static int STENCIL_BUFFER  = 2 ;
	protected final static int DEPTH_BUFFER    = 3 ;

	protected final static int BUFFER_LENGTH   = 4 ;

	protected final GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;
	protected final int[] buffers = new int[BUFFER_LENGTH] ;

	protected GLImage backbuffer = null ;

	public GLWorld( final String _id, final int _order )
	{
		super( _id, _order ) ;
	}

	public static GLWorld createDefaultWorld( final String _id, final int _order )
	{
		return new GLDefaultWorld( _id, _order ) ;
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

		if( backbuffer != null )
		{
			gl.glBindFramebuffer( GL3.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			gl.glBindFramebuffer( GL3.GL_DRAW_FRAMEBUFFER, 0 ) ;

			gl.glBindTexture( GL3.GL_TEXTURE_2D, backbuffer.textureIDs[0] ) ;
			gl.glCopyTexImage2D( GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, 0, 0, render.x, render.y, 0 ) ;
		}
	}

	@Override
	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		super.setRenderDimensions( _x, _y, _width, _height ) ;
		updateBufferDimensions( _width, _height ) ;
	}

	public GLImage getImage()
	{
		// We only want to create a back buffer if it ever gets 
		// used by the developer, else we are allocating space 
		// for no reason.
		if( backbuffer == null )
		{
			final int channel = 3 ;
			final IntVector2 render = getRender() ;

			final long estimatedConsumption = render.x * render.y * ( channel * 8 ) ;
			backbuffer = new GLImage( 0, estimatedConsumption ) ;

			final GL3 gl = GLRenderer.getGL() ;
			gl.glGenTextures( 1, backbuffer.textureIDs, 0 ) ;
			gl.glBindTexture( GL3.GL_TEXTURE_2D, backbuffer.textureIDs[0] ) ;

			gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT ) ;
			gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT ) ;

			gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST ) ;
			gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST ) ;
		}

		return backbuffer ;
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

		// First buffer is the Framebuffer.
		// Buffers afterwards are Renderbuffers.
		gl.glGenTextures( 1, buffers, COLOUR_BUFFER ) ;
		gl.glGenRenderbuffers( 1, buffers, STENCIL_BUFFER ) ;
		//gl.glGenRenderbuffers( 1, buffers, DEPTH_BUFFER ) ;

		final IntVector2 render = getRender() ;
		updateBufferDimensions( render.x, render.y ) ;

		gl.glGenFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

		gl.glFramebufferTexture2D( GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, buffers[COLOUR_BUFFER], 0 ) ;
		gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_STENCIL_ATTACHMENT, GL3.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		//gl.glFramebufferRenderbuffer( GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT,   GL3.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;

		switch( gl.glCheckFramebufferStatus( GL3.GL_DRAW_FRAMEBUFFER ) )
		{
			case GL3.GL_FRAMEBUFFER_COMPLETE    : break ;
			case GL3.GL_FRAMEBUFFER_UNDEFINED   : System.out.println( getID() + " framebuffer undefined." ) ; break ;
			case GL3.GL_FRAMEBUFFER_UNSUPPORTED : System.out.println( getID() + " framebuffer unsupported." ) ; break ;
			default                             : System.out.println( getID() + " framebuffer corrupt." ) ; break ;
		}

		gl.glBindFramebuffer( GL3.GL_FRAMEBUFFER, 0 ) ;
	}

	public void shutdown()
	{
		final GL3 gl = GLRenderer.getGL() ;
		if( backbuffer != null )
		{
			backbuffer.destroy() ;
			backbuffer = null ;
		}

		gl.glDeleteFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		gl.glDeleteRenderbuffers( 1, buffers, COLOUR_BUFFER ) ;
		gl.glDeleteRenderbuffers( 1, buffers, STENCIL_BUFFER ) ;
		//gl.glDeleteRenderbuffers( 1, buffers, DEPTH_BUFFER ) ;
		uploader.shutdown() ;
	}

	private void updateBufferDimensions( final int _width, final int _height )
	{
		final GL3 gl = GLRenderer.getGL() ;

		gl.glBindTexture( GL3.GL_TEXTURE_2D, buffers[COLOUR_BUFFER] ) ;
		gl.glTexImage2D( GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, _width, _height, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, null ) ;

		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT ) ;
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT ) ;

		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST ) ;
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST ) ;

		gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_STENCIL_INDEX8, _width, _height ) ;

		//gl.glBindRenderbuffer( GL3.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;
		//gl.glRenderbufferStorage( GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, _width, _height ) ;
	}

	private static class GLDefaultWorld extends GLWorld
	{
		public GLDefaultWorld( final String _id, final int _order )
		{
			super( _id, _order ) ;
		}

		@Override
		public void draw()
		{
			super.draw() ;
			final IntVector2 renPosition = getRenderPosition() ;
			final IntVector2 render = getRender() ;

			final IntVector2 disPosition = getDisplayPosition() ;
			final IntVector2 display = getDisplay() ;

			final GL3 gl = GLRenderer.getGL() ;
			gl.glBindFramebuffer( GL3.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			gl.glBindFramebuffer( GL3.GL_DRAW_FRAMEBUFFER, 0 ) ;
			gl.glBlitFramebuffer( renPosition.x, renPosition.y, render.x, render.y,
								  disPosition.x, disPosition.y, display.x, display.y, GL3.GL_COLOR_BUFFER_BIT , GL3.GL_LINEAR ) ;
		}
	}
}
