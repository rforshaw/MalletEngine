package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import android.content.Context ;

import android.opengl.GLES11 ;
import android.opengl.GLSurfaceView ;

import javax.microedition.khronos.opengles.GL10 ;
import javax.microedition.khronos.egl.EGLConfig ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventType ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;

import com.linxonline.mallet.renderer.android.* ;

public class GL2DRenderer implements RenderInterface, 
									 GLSurfaceView.Renderer
{
	public final GLRenderer render ;
	public final ResumeInitialisation resume ;

	public GL2DRenderer( final ResumeInitialisation _resume )
	{
		render = new GLRenderer() ;
		resume = _resume ;
	}

	@Override
	public void onSurfaceCreated( GL10 _unused, EGLConfig _config )
	{
		System.out.println( "Render Context available" ) ;
		render.start() ;
		resume.resume() ;
	}

	@Override
	public void onDrawFrame( GL10 _unused )
	{
		render.display() ;
	}

	@Override
	public void onSurfaceChanged( GL10 _unused, int _width, int _height)
	{
		render.setDisplayDimensions( _width, _height ) ;
	}

	@Override
	public void start() {}

	@Override
	public void shutdown()
	{
		render.shutdown() ;
	}

	@Override
	public void initFontAssist()
	{
		render.initFontAssist() ;
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
	public void setCameraPosition( final Vector3 _position )
	{
		render.setCameraPosition( _position ) ;
	}

	@Override
	public void updateState( final float _dt )
	{
		render.updateState( _dt ) ;
	}

	@Override
	public void draw( final float _dt )
	{
		render.draw( _dt ) ;
	}

	@Override
	public void processEvent( final Event _event )
	{
		render.processEvent( _event ) ;
	}

	@Override
	public final void passEvent( final Event _event )
	{
		render.passEvent( _event ) ;
	}

	@Override
	public String getName()
	{
		return "GL_2D_RENDERER" ;
	}

	@Override
	public ArrayList<EventType> getWantedEventTypes()
	{
		return render.getWantedEventTypes() ;
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

	public interface ResumeInitialisation
	{
		public void resume() ;
	}
}