package com.linxonline.mallet.renderer.android ;

import android.content.Context ;
import android.view.SurfaceHolder ;
import android.view.SurfaceView ;
import android.graphics.Paint.Style ;
import android.graphics.* ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;

import com.linxonline.mallet.renderer.android.* ;

public class Android2DRenderer extends SurfaceView 
							   implements RenderInterface, 
										  SurfaceHolder.Callback
{
	public final Basic2DRender render ; 

	public Android2DRenderer( final Context _context )
	{
		super( _context ) ;

		final SurfaceHolder holder = getHolder() ;
		holder.addCallback( this ) ;

		render = new Canvas2DRenderer( holder, _context.getResources() ) ;

		setFocusable( true ) ;
	}

	@Override
	public void surfaceDestroyed( final SurfaceHolder _holder ) {}

	@Override
	public void surfaceCreated( final SurfaceHolder _holder ) {}

	@Override
	public void surfaceChanged( final SurfaceHolder _holder, 
								final int _format, 
								final int _width, 
								final int _height )
	{
		//render.setRenderDimensions( _width, _height ) ;
		render.setDisplayDimensions( _width, _height ) ;
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
	public void onDraw( final Canvas _canvas ) {}

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
		return "ANDROID_2D_RENDERER" ;
	}

	@Override
	public String[] getWantedEventTypes()
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
}