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
	protected final static int FRAME_BUFFER = 0 ;

	protected final int[] buffers ;
	protected GLImage[] backBuffers ;

	protected String id ;
	protected int order ;
	protected final IntVector2 render = new IntVector2( 0, 0 ) ;

	protected final List<GLCamera> cameras = new ArrayList<GLCamera>() ;
	protected final List<GLBuffer> drawBuffers = new ArrayList<GLBuffer>() ;

	protected int[] colourAttachments ; 	// GL_COLOR_ATTACHMENT0, used by glDrawBuffers
	protected boolean hasDepth = false ;
	protected boolean hasStencil = false ;

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

	protected void init( final World _world,
						 final AssetLookup<Camera, GLCamera> _cameras,
						 final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		id = _world.getID() ;
		order = _world.getOrder() ;

		_world.getRenderDimensions( render ) ;

		MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

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

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

					MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, MGL.GL_DEPTH_ATTACHMENT, MGL.GL_TEXTURE_2D, buffers[offset], 0 ) ;
					hasDepth = true ;
					break ;
				}
				case STENCIL :
				{
					//System.out.println( "Creating stencil" ) ;
					MGL.glGenTextures( 1, buffers, offset ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_DEPTH_STENCIL, render.x, render.y, 0, MGL.GL_DEPTH_STENCIL, MGL.GL_FLOAT, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

					MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, MGL.GL_STENCIL_ATTACHMENT, MGL.GL_TEXTURE_2D, buffers[offset], 0 ) ;
					hasStencil = true ;
					break ;
				}
			}
		}

		colourAttachments = new int[colourAttachmentOffset] ;
		for( int i = 0; i < colourAttachmentOffset; ++i )
		{
			colourAttachments[i] = MGL.GL_COLOR_ATTACHMENT0 + i ;
		}

		updateCameras( _world, _cameras ) ;
		updateDrawBuffers( _world, _buffers ) ;

		final int code = MGL.glCheckFramebufferStatus( MGL.GL_DRAW_FRAMEBUFFER ) ; 
		switch( code )
		{
			case MGL.GL_FRAMEBUFFER_COMPLETE    : /*System.out.println( _world.getID() + " framebuffer complete." ) ;*/ break ;
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
			final int offset = _index + 1 ;		// skip the framebuffer id
			System.out.println( "Buffer ID: " + buffers[offset] ) ;
			final GLImage buffer = new GLImage( buffers[offset], estimatedConsumption ) ;
			backBuffers[_index] = buffer ;
		}

		return backBuffers[_index] ;
	}

	public void update( final World _world, final AssetLookup<Camera, GLCamera> _cameras, final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		id = _world.getID() ;

		_world.getRenderDimensions( render ) ;
		updateBufferDimensions( _world, render.x, render.y ) ;

		updateCameras( _world, _cameras ) ;
		updateDrawBuffers( _world, _buffers ) ;
	}

	protected void updateCameras( final World _world, final AssetLookup<Camera, GLCamera> _cameras )
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

	protected void updateDrawBuffers( final World _world, final AssetLookup<ABuffer, GLBuffer> _buffers )
	{
		drawBuffers.clear() ;
		for( final ABuffer buffer : _world.getBuffers() )
		{
			switch( buffer.getBufferType() )
			{
				default                    : Logger.println( "Attempting to add incompatible buffer to World.", Logger.Verbosity.NORMAL ) ; break ;
				case DRAW_INSTANCED_BUFFER :
				case DRAW_BUFFER           :
				case TEXT_BUFFER           :
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
		if( hasDepth == true )
		{
			// Enable Depth Test if the framebuffer contains a depth attachment.
			MGL.glEnable( MGL.GL_DEPTH_TEST ) ;
		}

		MGL.glClearColor( 255.0f, 0.0f, 0.0f, 0.0f ) ;
		
		int clearBits = ( colourAttachments.length > 0 ) ? MGL.GL_COLOR_BUFFER_BIT : 0 ;
		clearBits |= ( hasDepth == true ) ? MGL.GL_DEPTH_BUFFER_BIT : 0 ;
		clearBits |= ( hasStencil == true ) ? MGL.GL_STENCIL_BUFFER_BIT : 0 ;
		MGL.glClear( clearBits ) ;

		// Allow the shader to access the colour attachments of the framebuffer.
		// These colour attachments can then be used within a MalletTexture for 
		// other operations.
		MGL.glDrawBuffers( colourAttachments.length, colourAttachments, 0 ) ;

		// Draw the buffers from all the camera perspectives.
		for( final GLCamera camera : cameras )
		{
			camera.draw( drawBuffers ) ;
		}

		if( hasDepth == true )
		{
			MGL.glDisable( MGL.GL_DEPTH_TEST ) ;
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

	public int getOrder()
	{
		return order ;
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

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
					break ;
				}
				case DEPTH   :
				{
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_DEPTH_COMPONENT, _width, _height, 0, MGL.GL_DEPTH_COMPONENT, MGL.GL_FLOAT, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
					break ;
				}
				case STENCIL :
				{
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[offset] ) ;
					MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_DEPTH_STENCIL, _width, _height, 0, MGL.GL_DEPTH_STENCIL, MGL.GL_FLOAT, null ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
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
			// Default Framebuffer 0
			super( _world, _cameras, _buffers, 0 ) ;
		}

		@Override
		protected void init( final World _world,
							 final AssetLookup<Camera, GLCamera> _cameras,
							 final AssetLookup<ABuffer, GLBuffer> _buffers )
		{
			id = _world.getID() ;

			_world.getRenderDimensions( render ) ;		
			MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

			updateCameras( _world, _cameras ) ;
			updateDrawBuffers( _world, _buffers ) ;
		}

		@Override
		public void update( final World _world, final AssetLookup<Camera, GLCamera> _cameras, final AssetLookup<ABuffer, GLBuffer> _buffers )
		{
			id = _world.getID() ;

			_world.getRenderDimensions( render ) ;

			updateCameras( _world, _cameras ) ;
			updateDrawBuffers( _world, _buffers ) ;
		}

		@Override
		public void draw()
		{
			MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			if( hasDepth == true )
			{
				// Enable Depth Test if the framebuffer contains a depth attachment.
				MGL.glEnable( MGL.GL_DEPTH_TEST ) ;
			}

			MGL.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

			int clearBits = MGL.GL_COLOR_BUFFER_BIT ;
			clearBits |= ( hasDepth == true ) ? MGL.GL_DEPTH_BUFFER_BIT : 0 ;
			clearBits |= ( hasStencil == true ) ? MGL.GL_STENCIL_BUFFER_BIT : 0 ;
			MGL.glClear( clearBits ) ;

			// Draw the buffers from all the camera perspectives.
			for( final GLCamera camera : cameras )
			{
				camera.draw( drawBuffers ) ;
			}

			if( hasDepth == true )
			{
				MGL.glDisable( MGL.GL_DEPTH_TEST ) ;
			}
		}
	}
}
