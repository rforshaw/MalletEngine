package com.linxonline.mallet.system.android.gl ;

import android.content.Context ;
import android.content.res.Resources ;
import android.view.Window ;
import android.view.WindowManager ;
import android.media.AudioManager ;

import com.linxonline.mallet.main.android.AndroidActivity ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.DefaultShutdown ;
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

public class GLAndroidSystem implements SystemInterface
{
	public final AndroidActivity activity ;

	protected final EventSystem eventSystem = new EventSystem() ;
	protected final AndroidInputSystem inputSystem = new AndroidInputSystem() ;
	protected final AndroidAudioGenerator audioGenerator = new AndroidAudioGenerator() ;
	protected final DefaultShutdown shutdownDelegate = new DefaultShutdown() ;
	protected GL2DSurfaceView renderer ;

	protected boolean execution = true ;

	public GLAndroidSystem( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		activity = _activity ;
		renderer = new GL2DSurfaceView( _activity, _notify ) ;
	}

	public void initSystem()
	{
		setContentView() ;
		inputSystem.inputAdapter = renderer.renderer.render.renderInfo ;
		activity.addAndroidInputListener( inputSystem ) ;
	}

	public void setContentView()
	{
		activity.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				activity.setContentView( renderer ) ;
			}
		} ) ;
	}

	@Override
	public synchronized void startSystem() {}

	@Override
	public synchronized void stopSystem() {}

	@Override
	public synchronized void shutdownSystem()
	{
		activity.finish() ;
		audioGenerator.shutdownGenerator() ;
		renderer.renderer.shutdown() ;
	}

	/*INPUT HOOK*/
	@Override
	public void addInputHandler( InputHandler _handler )
	{
		inputSystem.addInputHandler( _handler ) ;
	}

	@Override
	public void removeInputHandler( InputHandler _handler )
	{
		inputSystem.removeInputHandler( _handler ) ;
	}

	/*EVENT HOOK*/
	@Override
	public void addEvent( Event _event )
	{
		eventSystem.addEvent( _event ) ;
	}

	@Override
	public void addEventHandler( EventHandler _handler )
	{
		eventSystem.addEventHandler( _handler ) ;
	}

	@Override
	public void removeEventHandler( EventHandler _handler )
	{
		eventSystem.removeEventHandler( _handler ) ;
	}

	@Override
	public ShutdownDelegate getShutdownDelegate()
	{
		return shutdownDelegate ;
	}

	/*RENDER*/

	@Override
	public RenderInterface getRenderInterface()
	{
		return renderer.renderer ;
	}

	@Override
	public AudioGenerator getAudioGenerator()
	{
		return audioGenerator ;
	}

	@Override
	public void sleep( final long _millis )
	{
		try
		{
			Thread.sleep( _millis ) ;
		}
		catch( InterruptedException ex )
		{
			ex.printStackTrace() ;
		}
	}

	@Override
	public synchronized boolean update( final float _dt )
	{
		renderer.renderer.updateState( _dt ) ;
		inputSystem.update() ;

		eventSystem.update() ;

		return true ;		// Update - called by Game State, return variable not used.
	}

	@Override
	public void draw( final float _dt )
	{
		renderer.draw( _dt ) ;
	}
}
