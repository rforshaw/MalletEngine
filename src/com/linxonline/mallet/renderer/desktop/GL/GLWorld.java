package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Set ;
import java.util.List ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.BufferedList ;

import com.linxonline.mallet.renderer.BasicWorld ;
import com.linxonline.mallet.renderer.DrawState ;
import com.linxonline.mallet.renderer.CameraData ;
import com.linxonline.mallet.renderer.CameraState ;

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

	protected final int[] buffers = new int[BUFFER_LENGTH] ;

	protected final DrawState<GLDrawData> state = new DrawState<GLDrawData>() ;			// Objects to be drawn
	protected final CameraState<CameraData> cameras = new CameraState<CameraData>() ;		// Camera view portals

	protected final GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;
	protected GLImage backbuffer = null ;

	private GLWorld( final String _id, final int _order )
	{
		super( _id, _order ) ;
		state.setUploadInterface( new GLBasicUpload( this ) ) ;
		state.setRemoveListener( new GLBasicRemove( this ) ) ;
		cameras.setDrawInterface( new GLCameraDraw( this ) ) ;
	}

	public static GLWorld createCore( final String _id, final int _order )
	{
		return new GLCoreWorld( _id, _order ) ;
	}

	public static GLWorld create( final String _id, final int _order )
	{
		return new GLWorld( _id, _order ) ;
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

			MGL.glGenTextures( 1, backbuffer.textureIDs, 0 ) ;
			MGL.glBindTexture( MGL.GL_TEXTURE_2D, backbuffer.textureIDs[0] ) ;

			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_REPEAT ) ;
			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;

			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
			MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;
		}

		return backbuffer ;
	}

	public GLGeometryUploader getUploader()
	{
		return uploader ;
	}

	@Override
	public void init()
	{
		// First buffer is the Framebuffer.
		// Buffers afterwards are Renderbuffers.
		MGL.glGenTextures( 1, buffers, COLOUR_BUFFER ) ;
		MGL.glGenRenderbuffers( 1, buffers, STENCIL_BUFFER ) ;
		//MGL.glGenRenderbuffers( 1, buffers, DEPTH_BUFFER ) ;

		final IntVector2 render = getRender() ;
		updateBufferDimensions( render.x, render.y ) ;

		MGL.glGenFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;

		MGL.glFramebufferTexture2D( MGL.GL_FRAMEBUFFER, MGL.GL_COLOR_ATTACHMENT0, MGL.GL_TEXTURE_2D, buffers[COLOUR_BUFFER], 0 ) ;
		MGL.glFramebufferRenderbuffer( MGL.GL_FRAMEBUFFER, MGL.GL_STENCIL_ATTACHMENT, MGL.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		//MGL.glFramebufferRenderbuffer( MGL.GL_FRAMEBUFFER, MGL.GL_DEPTH_ATTACHMENT,   MGL.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;

		switch( MGL.glCheckFramebufferStatus( MGL.GL_DRAW_FRAMEBUFFER ) )
		{
			case MGL.GL_FRAMEBUFFER_COMPLETE    : break ;
			case MGL.GL_FRAMEBUFFER_UNDEFINED   : System.out.println( getID() + " framebuffer undefined." ) ; break ;
			case MGL.GL_FRAMEBUFFER_UNSUPPORTED : System.out.println( getID() + " framebuffer unsupported." ) ; break ;
			default                             : System.out.println( getID() + " framebuffer corrupt." ) ; break ;
		}

		MGL.glBindFramebuffer( MGL.GL_FRAMEBUFFER, 0 ) ;
	}

	@Override
	public void draw()
	{
		final IntVector2 renPosition = getRenderPosition() ;
		final IntVector2 render = getRender() ;

		final IntVector2 disPosition = getDisplayPosition() ;
		final IntVector2 display = getDisplay() ;

		MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
		MGL.glClear( MGL.GL_COLOR_BUFFER_BIT | MGL.GL_DEPTH_BUFFER_BIT | MGL.GL_STENCIL_BUFFER_BIT ) ;

		cameras.draw() ;

		if( backbuffer != null )
		{
			MGL.glBindFramebuffer( MGL.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, 0 ) ;

			MGL.glBindTexture( MGL.GL_TEXTURE_2D, backbuffer.textureIDs[0] ) ;
			MGL.glCopyTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_RGBA, 0, 0, render.x, render.y, 0 ) ;
		}
	}

	@Override
	public void update( final int _diff, final int _iteration )
	{
		state.update( _diff, _iteration ) ;
		cameras.update( _diff, _iteration ) ;
	}

	@Override
	public void clean( final Set<String> _activeKeys )
	{
		final List<GLDrawData> list = state.getActiveDraws() ;
		for( final GLDrawData draw : list )
		{
			draw.getUsedResources( _activeKeys ) ;
		}

		uploader.clean() ;
	}

	@Override
	public void clear()
	{
		state.clear() ;
	}

	@Override
	public void shutdown()
	{
		if( backbuffer != null )
		{
			backbuffer.destroy() ;
			backbuffer = null ;
		}

		MGL.glDeleteFramebuffers( 1, buffers, FRAME_BUFFER ) ;
		MGL.glDeleteRenderbuffers( 1, buffers, COLOUR_BUFFER ) ;
		MGL.glDeleteRenderbuffers( 1, buffers, STENCIL_BUFFER ) ;
		//MGL.glDeleteRenderbuffers( 1, buffers, DEPTH_BUFFER ) ;

		uploader.shutdown() ;
	}

	@Override
	public void sort()
	{
		state.sort() ;
	}

	@Override
	public void addCamera( final CameraData _camera )
	{
		cameras.add( _camera ) ;
	}

	@Override
	public void removeCamera( final CameraData _camera )
	{
		cameras.remove( _camera ) ;
	}

	@Override
	public CameraData getCamera( final String _id )
	{
		return cameras.getCamera( _id ) ;
	}

	@Override
	public void addDraw( final GLDrawData _draw )
	{
		state.add( _draw ) ;
	}

	@Override
	public void removeDraw( final GLDrawData _draw )
	{
		state.remove( _draw ) ;
	}

	@Override
	public void displayChanged()
	{
		final IntVector2 position = getDisplayPosition() ;
		final IntVector2 dim = getDisplay() ;
		cameras.setDisplayDimensions( position.x, position.y, dim.x, dim.y ) ;
	}

	@Override
	public void renderChanged()
	{
		final IntVector2 dim = getRender() ;
		updateBufferDimensions( dim.x, dim.y ) ;
	}

	@Override
	public void orderChanged() {}

	private void updateBufferDimensions( final int _width, final int _height )
	{
		MGL.glBindTexture( MGL.GL_TEXTURE_2D, buffers[COLOUR_BUFFER] ) ;
		MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 0, MGL.GL_RGBA, _width, _height, 0, MGL.GL_RGBA, MGL.GL_UNSIGNED_BYTE, null ) ;

		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_REPEAT ) ;
		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;

		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_NEAREST ) ;
		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_NEAREST ) ;

		MGL.glBindRenderbuffer( MGL.GL_RENDERBUFFER, buffers[STENCIL_BUFFER] ) ;
		MGL.glRenderbufferStorage( MGL.GL_RENDERBUFFER, MGL.GL_STENCIL_INDEX8, _width, _height ) ;

		//MGL.glBindRenderbuffer( MGL.GL_RENDERBUFFER, buffers[DEPTH_BUFFER] ) ;
		//MGL.glRenderbufferStorage( MGL.GL_RENDERBUFFER, MGL.GL_DEPTH_COMPONENT, _width, _height ) ;
	}

	private static class GLCoreWorld extends GLWorld
	{
		private GLCoreWorld( final String _id, final int _order )
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

			MGL.glBindFramebuffer( MGL.GL_READ_FRAMEBUFFER, buffers[FRAME_BUFFER] ) ;
			MGL.glBindFramebuffer( MGL.GL_DRAW_FRAMEBUFFER, 0 ) ;
			MGL.glBlitFramebuffer( renPosition.x, renPosition.y, render.x, render.y,
								   disPosition.x, disPosition.y, display.x, display.y, MGL.GL_COLOR_BUFFER_BIT , MGL.GL_LINEAR ) ;
		}
	}

	private final static class GLCameraDraw implements CameraState.IDraw<CameraData>
	{
		private final GLWorld world ;

		private final static Matrix4 uiMatrix = new Matrix4() ;		// Used for rendering GUI elements not impacted by World/Camera position
		private final static Matrix4 worldMatrix = new Matrix4() ;	// Used for moving the camera around the world

		private final Matrix4 worldProjection = new Matrix4() ;
		private final Matrix4 uiProjection = new Matrix4() ;

		public GLCameraDraw( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void draw( final CameraData _camera )
		{
			final CameraData.Projection projection = _camera.getProjection() ;
			final CameraData.Screen screen = _camera.getRenderScreen() ;

			final int width = ( int )screen.dimension.x ;
			final int height = ( int )screen.dimension.y ;
			MGL.glViewport( 0, 0, width, height ) ;

			final Vector3 uiPosition = _camera.getUIPosition() ;
			final Vector3 position = _camera.getPosition() ;
			final Vector3 scale = _camera.getScale() ;
			//final Vector3 rotation = _camera.getRotation() ;

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

			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.draw( worldProjection, uiProjection ) ;

			//System.out.println( "Camera: " + world.getID() + " Order: " + world.getOrder() ) ;
		}
	}

	private final static class GLBasicRemove implements BufferedList.RemoveListener<GLDrawData>
	{
		private final GLWorld world ;

		public GLBasicRemove( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void remove( final GLDrawData _data )
		{
			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.remove( _data ) ;
		}
	}

	private final static class GLBasicUpload implements DrawState.IUpload<GLDrawData>
	{
		private final GLWorld world ;

		public GLBasicUpload( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void upload( final GLDrawData _data )
		{
			if( GLRenderer.loadProgram( _data ) == false )
			{
				return ;
			}

			final Vector3 position = _data.getPosition() ;
			final Vector3 offset   = _data.getOffset() ;
			final Vector3 rotation = _data.getRotation() ;

			final Matrix4 positionMatrix = _data.getDrawMatrix() ;
			positionMatrix.setIdentity() ;

			positionMatrix.setTranslate( position.x, position.y, 0.0f ) ;
			positionMatrix.rotate( rotation.x, 1.0f, 0.0f, 0.0f ) ;
			positionMatrix.rotate( rotation.y, 0.0f, 1.0f, 0.0f ) ;
			positionMatrix.rotate( rotation.z, 0.0f, 0.0f, 1.0f ) ;
			positionMatrix.translate( offset.x, offset.y, offset.z ) ;

			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.upload( _data ) ;
		}
	}
}
