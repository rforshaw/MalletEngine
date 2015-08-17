package com.linxonline.mallet.system.android.gl ;

import android.content.Context ;
import android.content.res.Resources ;
import android.view.Window ;
import android.view.WindowManager ;
import android.media.AudioManager ;

import com.linxonline.mallet.main.android.AndroidActivity ;

import com.linxonline.mallet.system.BasicSystem ;
import com.linxonline.mallet.system.DefaultShutdown ;
import com.linxonline.mallet.input.InputSystemInterface ;
import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.event.EventHandler ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.resources.android.* ;
import com.linxonline.mallet.renderer.android.GL.* ;
import com.linxonline.mallet.input.android.* ;
import com.linxonline.mallet.audio.android.* ;

public class GLAndroidSystem extends BasicSystem
{
	public final AndroidActivity activity ;
	public GL2DSurfaceView surface ;

	protected boolean execution = true ;

	public GLAndroidSystem( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		activity = _activity ;

		surface = new GL2DSurfaceView( _activity, _notify ) ;
		renderer = surface.renderer ;

		shutdownDelegate = new DefaultShutdown() ;
		audioGenerator = new AndroidAudioGenerator() ;
		eventSystem = new EventSystem( "ROOT_EVENT_SYSTEM" ) ;
		inputSystem = new AndroidInputSystem() ;
	}

	@Override
	public void initSystem()
	{
		final AndroidInputSystem input = ( AndroidInputSystem )inputSystem ;
		final GL2DRenderer render = ( GL2DRenderer )renderer ;
		input.inputAdapter = render.render.renderInfo ;
		activity.addAndroidInputListener( input ) ;
	}

	public void setContentView()
	{
		activity.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				activity.setContentView( surface ) ;
			}
		} ) ;
	}

	@Override
	public synchronized void startSystem()
	{
		setContentView() ;
		surface.onResume() ;
	}

	@Override
	public synchronized void stopSystem()
	{
		// Don't call super, the renderer will be destroyed 
		// via another route.
		surface.onPause() ;
	}

	@Override
	public synchronized void shutdownSystem()
	{
		shutdownDelegate.shutdown() ;
		audioGenerator.shutdownGenerator() ;
	}

	@Override
	public synchronized boolean update( final float _dt )
	{
		return super.update( _dt ) ;
	}

	@Override
	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		surface.requestRender() ;
	}
}
