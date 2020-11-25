package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Set ;
import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.BufferedList ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.World.AttachmentType ;
import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.ABuffer ;

/**
	Represents the OpenGL state for a world.
	A world cannot interact with other worlds.
*/
public class GLWorld
{
	protected final static int FRAME_BUFFER    = 0 ;

	protected final int[] buffers ;
	protected GLImage[] backBuffers ;

	protected String id ;
	protected final IntVector2 renderPosition = new IntVector2( 0, 0 ) ;
	protected final IntVector2 render = new IntVector2( 0, 0 ) ;
	protected final IntVector2 displayPosition = new IntVector2( 0, 0 ) ;
	protected final IntVector2 display = new IntVector2( 1280, 720 ) ;

	protected final List<GLCamera> cameras = new ArrayList<GLCamera>() ;
	protected final List<GLBuffer> drawBuffers = new ArrayList<GLBuffer>() ;

	public static GLWorld createCore( final World _world,
									  final AssetLookup<Camera, GLCamera> _cameras,
									  final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		return new GLCoreWorld( _world, _cameras, _buffers ) ;
	}

	public GLWorld( final World _world,
					final AssetLookup<Camera, GLCamera> _cameras,
					final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		// The first buffer generated is always the FrameBuffer
		// the rest is attachment buffers.
		final AttachmentType[] attachments = _world.getAttachments() ;
		buffers = new int[1 + attachments.length] ;
		backBuffers = new GLImage[1 + attachments.length] ;

		MGL.glGenFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		init( _world, _cameras, _buffers ) ;
	}

	public GLWorld( final World _world,
					final AssetLookup<Camera, GLCamera> _cameras,
					final AssetLookup<ABuffer, GLBuffer> _buffers,
					final int _frameID )
	{
		// The first buffer generated is always the FrameBuffer
		// the rest is attachment buffers.
		final AttachmentType[] attachments = _world.getAttachments() ;
		buffers = new int[1 + attachments.length] ;
		backBuffers = new GLImage[1 + attachments.length] ;

		// The framebuffer is specified by an external source.
		buffers[FRAME_BUFFER] = _frameID ;
		init( _world, _cameras, _buffers ) ;
	}

	private void init( final World _world,
					   final AssetLookup<Camera, GLCamera> _cameras,
					   final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		id = _world.getID() ;

		_world.getRenderPosition( renderPosition ) ;
		_world.getRenderDimensions( render ) ;

		_world.getDisplayPosition( displayPosition ) ;
		_world.getDisplayDimensions( display ) ;
	
		MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

		_world.getRenderDimensions( render ) ;

		int colourAttachmentOffset = 0 ;

		// Generate framebuffer then attachment buffers
		final AttachmentType[] attachments = _world.getAttachments() ;
		for( int i = 0; i < attachments.length; ++i )
		{
			final AttachmentType type = attachments[i] ;
			final int offset = i + 1 ; // add 1 to skip the framebuffer.

			switch( type )
			{
				default      :
				case COLOUR  :
				{
					//System.out.println( "Creating colour" ) ;
					MGL.glGenTextures( 1, buffers, offset ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_RGBA, render.x, render.y, 0, MGL.GL_RGBA, MGL.GL_UNSIGNED_BYTE, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_REPEAT ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

					final int attachment = MGL.GL_COLOR_ATTACHMENT0 + colourAttachmentOffset++ ;
					MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, attachment, MGL.GL_TEXTURE_2D, buffers[offset], 0 ) ;
					break ;
				}
				case DEPTH   :
				{
					//System.out.println( "Creating depth" ) ;
					MGL.glGenTextures( 1, buffers, offset ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_DEPTH_COMPONENT, render.x, render.y, 0, MGL.GL_DEPTH_COMPONENT, MGL.GL_FLOAT, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_CLAMP_TO_EDGE ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

					MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, MGL.GL_DEPTH_ATTACHMENT, MGL.GL_TEXTURE_2D, buffers[offset], 0 ) ;
					break ;
				}
				case STENCIL :
				{
					//System.out.println( "Creating stencil" ) ;
					MGL.glGenTextures( 1, buffers, offset ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_DEPTH_STENCIL, render.x, render.y, 0, MGL.GL_DEPTH_STENCIL, MGL.GL_FLOAT, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_CLAMP_TO_EDGE ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

					MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, MGL.GL_STENCIL_ATTACHMENT, MGL.GL_TEXTURE_2D, buffers[offset], 0 ) ;
					break ;
				}
			}
		}

		updateCameras( _world, _cameras ) ;
		updateDrawBuffers( _world, _buffers ) ;

		final int code = MGL.glCheckFramebufferStatus( MGL.GL_DRAW_FRAMEBUFFER ) ; 
		switch( code )
		{
			case MGL.GL_FRAMEBUFFER_COMPLETE    : break ;
			case MGL.GL_FRAMEBUFFER_UNDEFINED   : System.out.println( _world.getID() + " framebuffer undefined." ) ; break ;
			case MGL.GL_FRAMEBUFFER_UNSUPPORTED : System.out.println( _world.getID() + " framebuffer unsupported." ) ; break ;
			default                             : System.out.println( _world.getID() + " framebuffer corrupt: " + code ) ; break ;
		}

		MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, 0 ) ;
	}

	public GLImage getImage( final int _index )
	{
		// We only want to create a back buffer if it ever gets 
		// used by the developer, else we are allocating space 
		// for no reason.
		if( backBuffers[_index] == null )
		{
			final int channel = 3 ;
			final long estimatedConsumption = ( long )( render.x * render.y ) * ( long )( channel * 8 ) ;
			final GLImage buffer = new GLImage( 0, estimatedConsumption ) ;

			MGL.glGenTextures( 1, buffer.textureIDs, 0 ) ;
			MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffer.textureIDs[0] ) ;

			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_REPEAT ) ;
			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;

			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
			backBuffers[_index] = buffer ;
		}

		return backBuffers[_index] ;
	}

	public void update( final World _world, final AssetLookup<Camera, GLCamera> _cameras, final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		id = _world.getID() ;

		_world.getRenderPosition( renderPosition ) ;
		_world.getRenderDimensions( render ) ;

		_world.getDisplayPosition( displayPosition ) ;
		_world.getDisplayDimensions( display ) ;

		updateBufferDimensions( _world, render.x, render.y ) ;

		updateCameras( _world, _cameras ) ;
		updateDrawBuffers( _world, _buffers ) ;
	}

	private void updateCameras( final World _world, final AssetLookup<Camera, GLCamera> _cameras )
	{
		cameras.clear() ;
		for( final Camera camera : _world.getCameras() )
		{
			final GLCamera cam = _cameras.getRHS( camera.index() ) ;
			if( cam != null )
			{
				cameras.add( cam ) ;
			}
		}
	}

	private void updateDrawBuffers( final World _world, final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		drawBuffers.clear() ;
		for( final ABuffer buffer : _world.getBuffers() )
		{
			switch( buffer.getBufferType() )
			{
				default          : Logger.println( "Attempting to add incompatible buffer to World.", Logger.Verbosity.NORMAL ) ; break ;
				case DRAW_BUFFER :
				case TEXT_BUFFER :
				{
					final int index = buffer.index() ;
					final GLBuffer buff = _buffers.getRHS( index ) ;
					if( buff != null )
					{
						drawBuffers.add( buff ) ;
					}
				}
			}
		}
	}
	
	public void draw()
	{
		MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		MGL.glClear( MGL.GL_COLOR_BUFFER_BIT | MGL.GL_DEPTH_BUFFER_BIT | MGL.GL_STENCIL_BUFFER_BIT ) ;
		//MGL.glClearColor( 1.0f, 0.0f, 0.0f, 0.5f ) ;

		for( final GLCamera camera : cameras )
		{
			camera.draw( drawBuffers ) ;
		}

		if( backBuffers[0] != null )
		{
			MGL.glBindFramebuffer( MGL.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

			MGL.glBindTexture( MGL.GL_TEXTURE_2D, backBuffers[0].textureIDs[0] ) ;
			MGL.glCopyTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_RGB, 0, 0, render.x, render.y, 0 ) ;
		}
	}

	public void shutdown()
	{
		for( int i = 0; i < backBuffers.length; ++i )
		{
			final GLImage buffer = backBuffers[i] ;
			if( buffer != null )
			{
				buffer.destroy() ;
				backBuffers[i] = null ;
			}
		}

		MGL.glDeleteFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		MGL.glDeleteTextures( buffers.length - 1, buffers, 1 ) ;
	}

	public String getID()
	{
		return id ;
	}

	private void updateBufferDimensions( final World _world, final int _width, final int _height )
	{
		final AttachmentType[] attachments = _world.getAttachments() ;
		for( int i = 0; i < attachments.length; ++i )
		{
			final AttachmentType type = attachments[i] ;
			final int offset = i + 1 ; // add 1 to skip the framebuffer.

			switch( type )
			{
				default      :
				case COLOUR  :
				{
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_RGBA, _width, _height, 0, MGL.GL_RGBA, MGL.GL_UNSIGNED_BYTE, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_REPEAT ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
					break ;
				}
				case DEPTH   :
				{
					//MGL.glBindRenderbuffer( MGL.GL_RENDERBUFFER, buffers[offset] ) ;
					//MGL.glRenderbufferStorage( MGL.GL_RENDERBUFFER, MGL.GL_DEPTH_COMPONENT, _width, _height ) ;
					break ;
				}
				case STENCIL :
				{
					MGL.glBindRenderbuffer( MGL.GL_RENDERBUFFER, buffers[offset] ) ;
					MGL.glRenderbufferStorage( MGL.GL_RENDERBUFFER, MGL.GL_STENCIL_INDEX8, _width, _height ) ;
					break ;
				}
			}
		}
	}

	private static class GLCoreWorld extends GLWorld
	{
		private GLCoreWorld( final World _world,
							 final AssetLookup<Camera, GLCamera> _cameras,
							 final AssetLookup<ABuffer, GLBuffer> _buffers )
		{
			super( _world, _cameras, _buffers, 0 ) ;
		}

		@Override
		public void draw()
		{
			super.draw() ;
			MGL.glBindFramebuffer( MGL.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, 0 ) ;
			MGL.glBlitFramebuffer( renderPosition.x, renderPosition.y,
								   render.x, render.y,
								   displayPosition.x, displayPosition.y,
								   display.x, display.y,
								   MGL.GL_COLOR_BUFFER_BIT , MGL.GL_LINEAR ) ;
		}
	}
}
