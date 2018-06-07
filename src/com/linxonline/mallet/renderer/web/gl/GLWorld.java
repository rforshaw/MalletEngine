package com.linxonline.mallet.renderer.web.gl ;

import java.util.Set ;
import java.util.List ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLRenderbuffer ;
import org.teavm.jso.webgl.WebGLFramebuffer ;
import org.teavm.jso.webgl.WebGLTexture ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

/**
	Represents the OpenGL state for a world.
	A world cannot interact with other worlds.
*/
public class GLWorld extends BasicWorld<GLDrawData, CameraData>
{
	protected final GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;

	protected final WebGLFramebuffer  frameBuffer   = MGL.createFramebuffer() ;
	protected final WebGLTexture      colourBuffer  = MGL.createTexture() ;
	protected final WebGLRenderbuffer stencilBuffer = MGL.createRenderbuffer() ;
	protected final WebGLRenderbuffer depthBuffer   = MGL.createRenderbuffer() ;

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
		final IntVector2 renPosition = getRenderPosition() ;
		final IntVector2 render = getRender() ;

		if( render.x <= 0 || render.y <= 0 )
		{
			return ;
		}

		final IntVector2 disPosition = getDisplayPosition() ;
		final IntVector2 display = getDisplay() ;

		MGL.bindFramebuffer( GL3.FRAMEBUFFER, frameBuffer ) ;
		MGL.clear( GL3.COLOR_BUFFER_BIT | GL3.DEPTH_BUFFER_BIT | GL3.STENCIL_BUFFER_BIT ) ;

		super.draw() ;

		if( backbuffer != null )
		{
			MGL.bindTexture( GL3.TEXTURE_2D, backbuffer.textureIDs[0] ) ;
			MGL.copyTexImage2D( GL3.TEXTURE_2D, 0, GL3.RGBA, 0, 0, render.x, render.y, 0 ) ;
		}

		MGL.bindFramebuffer( GL3.FRAMEBUFFER, null ) ;
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
			backbuffer = new GLImage( MGL.createTexture(), estimatedConsumption ) ;

			MGL.bindTexture( GL3.TEXTURE_2D, backbuffer.textureIDs[0] ) ;

			MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_S, GL3.REPEAT ) ;
			MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_T, GL3.REPEAT ) ;

			MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MAG_FILTER, GL3.NEAREST ) ;
			MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MIN_FILTER, GL3.NEAREST ) ;
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
		final IntVector2 render = getRender() ;
		updateBufferDimensions( render.x, render.y ) ;

		MGL.bindFramebuffer( GL3.FRAMEBUFFER, frameBuffer ) ;

		MGL.framebufferTexture2D( GL3.FRAMEBUFFER, GL3.COLOR_ATTACHMENT0, GL3.TEXTURE_2D, colourBuffer, 0 ) ;
		MGL.framebufferRenderbuffer( GL3.FRAMEBUFFER, GL3.STENCIL_ATTACHMENT, GL3.RENDERBUFFER, stencilBuffer ) ;
		//MGL.framebufferRenderbuffer( GL3.FRAMEBUFFER, GL3.DEPTH_ATTACHMENT, GL3.RENDERBUFFER, depthBuffer ) ;

		switch( MGL.checkFramebufferStatus( GL3.FRAMEBUFFER ) )
		{
			case GL3.FRAMEBUFFER_COMPLETE                      : break ;
			case GL3.FRAMEBUFFER_INCOMPLETE_ATTACHMENT         : System.out.println( getID() + " framebuffer undefined." ) ; break ;
			case GL3.FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT : System.out.println( getID() + " framebuffer unsupported." ) ; break ;
			case GL3.FRAMEBUFFER_INCOMPLETE_DIMENSIONS         :
			case GL3.FRAMEBUFFER_UNSUPPORTED                   :
			default                                            : System.out.println( getID() + " framebuffer corrupt." ) ; break ;
		}

		MGL.bindFramebuffer( GL3.FRAMEBUFFER, null ) ;
	}

	public void shutdown()
	{
		if( backbuffer != null )
		{
			backbuffer.destroy() ;
			backbuffer = null ;
		}

		MGL.deleteFramebuffer( frameBuffer ) ;
		MGL.deleteTexture( colourBuffer ) ;
		MGL.deleteRenderbuffer( stencilBuffer ) ;
		//MGL.deleteRenderbuffer( depthBuffer ) ;
		uploader.shutdown() ;
	}

	private void updateBufferDimensions( final int _width, final int _height )
	{
		MGL.bindTexture( GL3.TEXTURE_2D, colourBuffer ) ;
		MGL.texImage2D( GL3.TEXTURE_2D, 0, GL3.RGBA, _width, _height, 0, GL3.RGBA, GL3.UNSIGNED_BYTE, null ) ;

		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_S, GL3.REPEAT ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_T, GL3.REPEAT ) ;

		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MAG_FILTER, GL3.NEAREST ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MIN_FILTER, GL3.NEAREST ) ;

		MGL.bindRenderbuffer( GL3.RENDERBUFFER, stencilBuffer ) ;
		MGL.renderbufferStorage( GL3.RENDERBUFFER, GL3.STENCIL_INDEX8, _width, _height ) ;

		//MGL.bindRenderbuffer( GL3.RENDERBUFFER, depthBuffer ) ;
		//MGL.renderbufferStorage( GL3.RENDERBUFFER, GL3.DEPTH_COMPONENT, _width, _height ) ;
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

			if( render.x <= 0 || render.y <= 0 )
			{
				return ;
			}

			//final IntVector2 disPosition = getDisplayPosition() ;
			//final IntVector2 display = getDisplay() ;

			MGL.bindFramebuffer( GL3.FRAMEBUFFER, null ) ;
			getCameraState().draw() ;

			//MGL.bindFramebuffer( GL3.FRAMEBUFFER, frameBuffer ) ;
			//MGL.blitFramebuffer( renPosition.x, renPosition.y, render.x, render.y,
			//					 disPosition.x, disPosition.y, display.x, display.y, GL3.COLOR_BUFFER_BIT , GL3.LINEAR ) ;
			//MGL.bindFramebuffer( GL3.FRAMEBUFFER, null ) ;
		}
	}
}
