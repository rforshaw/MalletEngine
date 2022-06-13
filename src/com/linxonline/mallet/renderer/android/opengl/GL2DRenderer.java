package com.linxonline.mallet.renderer.android.opengl ;

import android.content.Context ;

import android.opengl.GLES11 ;
import android.opengl.GLSurfaceView ;

import javax.microedition.khronos.opengles.GL10 ;
import javax.microedition.khronos.egl.EGLConfig ;

import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.EventType ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

public class GL2DRenderer implements IRender, 
									 GLSurfaceView.Renderer
{
	protected final GLRenderer render ;
	protected final Notification surfaceCreated = new Notification() ;

	public GL2DRenderer( final Notification.Notify _notify )
	{
		render = new GLRenderer() ;
		surfaceCreated.addNotify( _notify ) ;
	}

	@Override
	public synchronized void onSurfaceCreated( final GL10 _unused, final EGLConfig _config )
	{
		System.out.println( "onSurfaceCreated()" ) ;
		// If a render state previously existed but we lost the OpenGL 
		// context then we need to reload the lost resources.
		render.recover() ;
		start() ;
		surfaceCreated.inform() ;
	}

	@Override
	public synchronized void onDrawFrame( final GL10 _unused )
	{
		render.display() ;
	}

	@Override
	public void onSurfaceChanged( final GL10 _unused, final int _width, final int _height)
	{
		System.out.println( "onSurfaceChanged() width: " + _width + " height: " + _height ) ;
		int renderWidth = _width ;
		int renderHeight = _height ;

		if( GlobalConfig.getBoolean( "DISPLAYRENDERPARITY", true ) == false )
		{
			// Update the render dimensions if the window size 
			// and render size are meant to be identical.
			// Some users will not want parity, using a larger window 
			// size but rendering to a smaller size and subsequently being upscaled.
			renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", _width ) ;
			renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", _height ) ;
			//System.out.println( "Render parity width: " + renderWidth + " height: " + renderHeight ) ;
		}

		setDisplayDimensions( _width, _height ) ;
		final World world = WorldAssist.getDefault() ;
		world.setRenderDimensions( 0, 0, _width, _height ) ;
		WorldAssist.update( world ) ;

		final Camera camera = CameraAssist.getDefault() ;
		camera.setScreenResolution( _width, _height ) ;
		camera.setOrthographic( 0.0f, _height, 0.0f, _width, -1000.0f, 1000.0f ) ;
		CameraAssist.update( camera ) ;
	}

	@Override
	public synchronized void start()
	{
		render.start() ;
	}

	@Override
	public synchronized void shutdown()
	{
		render.shutdown() ;
	}

	@Override
	public synchronized void setDisplayDimensions( final int _width, final int _height )
	{
		render.setDisplayDimensions( _width, _height ) ;
	}

	@Override
	public synchronized void updateState( final float _dt )
	{
		render.updateState( _dt ) ;
	}

	@Override
	public synchronized void draw( final float _dt )
	{
		render.draw( _dt ) ;
	}

	@Override
	public EventController getEventController()
	{
		return render.getEventController() ;
	}

	@Override
	public synchronized void sort()
	{
		render.sort() ;
	}

	@Override
	public synchronized void clear()
	{
		render.clear() ;
	}

	@Override
	public synchronized void clean()
	{
		render.clean() ;
	}
}
