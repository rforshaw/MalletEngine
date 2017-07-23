package com.linxonline.mallet.core.android.gl ;

import android.app.Activity ;
import android.content.res.Resources ;
import android.view.Window ;
import android.view.WindowManager ;
import android.media.AudioManager ;

import com.linxonline.mallet.core.android.AndroidActivity ;
import com.linxonline.mallet.core.BasicSystem ;
import com.linxonline.mallet.core.DefaultShutdown ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.io.filesystem.android.AndroidFileSystem ;
import com.linxonline.mallet.renderer.android.GL.* ;
import com.linxonline.mallet.input.android.* ;
import com.linxonline.mallet.audio.android.* ;

public class GLAndroidSystem extends BasicSystem<AndroidFileSystem,
												 DefaultShutdown,
												 GL2DRenderer,
												 AndroidAudioGenerator,
												 AndroidInputSystem,
												 EventSystem>
{
	public final AndroidActivity activity ;
	public GL2DSurfaceView surface ;

	protected boolean execution = true ;

	public GLAndroidSystem( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		super( new DefaultShutdown(),
			   new GL2DRenderer( _notify ),
			   new AndroidAudioGenerator(),
			   new EventSystem( "ROOT_EVENT_SYSTEM" ),
			   new AndroidInputSystem(),
			   new AndroidFileSystem( _activity ) ) ;

		activity = _activity ;
		surface = new GL2DSurfaceView( _activity, getRenderer() ) ;
	}

	@Override
	public void initSystem()
	{
		activity.addAndroidInputListener( getInput() ) ;
		getAudioGenerator().startGenerator() ;
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
		super.shutdownSystem() ;
		activity.finish() ;
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
