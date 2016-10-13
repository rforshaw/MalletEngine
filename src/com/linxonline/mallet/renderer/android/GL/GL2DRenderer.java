package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import android.content.Context ;

import android.opengl.GLES11 ;
import android.opengl.GLSurfaceView ;

import javax.microedition.khronos.opengles.GL10 ;
import javax.microedition.khronos.egl.EGLConfig ;

import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.EventType ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.renderer.android.* ;

public class GL2DRenderer implements RenderInterface, 
									 GLSurfaceView.Renderer
{
	public final GLRenderer render ;
	public final Notification surfaceCreated = new Notification() ;

	public GL2DRenderer( final Notification.Notify _notify )
	{
		render = new GLRenderer() ;
		surfaceCreated.addNotify( _notify ) ;
	}

	@Override
	public void onSurfaceCreated( final GL10 _unused, final EGLConfig _config )
	{
		System.out.println( "onSurfaceCreated()" ) ;
		// If a render state previously existed but we lost the OpenGL 
		// context then we need to reload the lost resources.
		render.recover() ;
		start() ;
		surfaceCreated.inform() ;
	}

	@Override
	public void onDrawFrame( final GL10 _unused )
	{
		synchronized( render )
		{
			render.display() ;
		}
	}

	@Override
	public void onSurfaceChanged( final GL10 _unused, final int _width, final int _height)
	{
		System.out.println( "onSurfaceChanged()" ) ;
		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", _width ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", _height ) ;

		GlobalConfig.addInteger( "DISPLAYWIDTH", _width ) ;
		GlobalConfig.addInteger( "DISPLAYHEIGHT", _height ) ;

		GlobalConfig.addInteger( "RENDERWIDTH", renderWidth ) ;
		GlobalConfig.addInteger( "RENDERHEIGHT", renderHeight ) ;

		setDisplayDimensions( _width, _height ) ;
		setRenderDimensions( renderWidth, renderHeight ) ;
	}

	@Override
	public void start()
	{
		render.start() ;
	}

	@Override
	public void shutdown()
	{
		render.shutdown() ;
	}

	@Override
	public void initAssist()
	{
		render.initAssist() ;
	}

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		render.setRenderDimensions( _width, _height ) ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		render.setDisplayDimensions( _width, _height ) ;
	}

	@Override
	public void updateState( final float _dt )
	{
		synchronized( render )
		{
			render.updateState( _dt ) ;
		}
	}

	@Override
	public void draw( final float _dt )
	{
		render.draw( _dt ) ;
	}

	@Override
	public RenderInfo getRenderInfo()
	{
		return render.getRenderInfo() ;
	}

	@Override
	public EventController getEventController()
	{
		return render.getEventController() ;
	}

	@Override
	public WorldState getWorldState()
	{
		return render.getWorldState() ;
	}

	@Override
	public void sort()
	{
		render.sort() ;
	}

	@Override
	public void clear()
	{
		render.clear() ;
	}

	@Override
	public void clean()
	{
		render.clean() ;
	}
}
