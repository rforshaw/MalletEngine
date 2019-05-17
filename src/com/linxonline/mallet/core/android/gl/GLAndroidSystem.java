package com.linxonline.mallet.core.android.gl ;

import android.app.Activity ;
import android.content.Context ;
import android.content.res.Resources ;
import android.view.Window ;
import android.view.WindowManager ;
import android.view.inputmethod.InputMethodManager ;
import android.media.AudioManager ;

import com.linxonline.mallet.core.android.AndroidActivity ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.buffers.* ;
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
												 EventSystem,
												 GameSystem>
{
	public final AndroidActivity activity ;
	public GL2DSurfaceView surface ;

	protected final EventController eventController = new EventController() ;
	protected boolean execution = true ;

	public GLAndroidSystem( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		super( new DefaultShutdown(),
			   new GL2DRenderer( _notify ),
			   new AndroidAudioGenerator(),
			   new EventSystem( "ROOT_EVENT_SYSTEM" ),
			   new AndroidInputSystem(),
			   new AndroidFileSystem( _activity ),
			   new GameSystem() ) ;

		activity = _activity ;
		surface = new GL2DSurfaceView( _activity, getRenderer() ) ;
		getGameSystem().setMainSystem( this ) ;
	}

	@Override
	public void initSystem()
	{
		initEventProcessors() ;

		activity.addAndroidInputListener( getInput() ) ;
		getAudioGenerator().startGenerator() ;
	}

	protected void initEventProcessors()
	{
		eventController.addProcessor( "USE_SYSTEM_KEYBOARD", new EventController.IProcessor<Boolean>()
		{
			private boolean show = false ;

			@Override
			public void process( final Boolean _variable )
			{
				final InputMethodManager imm = ( InputMethodManager )activity.getSystemService( Context.INPUT_METHOD_SERVICE ) ;
				if( show != _variable.booleanValue() )
				{
					imm.toggleSoftInput( 0, 0 ) ;
				}
			}
		} ) ;

		getEventSystem().addEventHandler( eventController ) ;
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
		super.update( _dt ) ;
		eventController.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}

	@Override
	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		surface.requestRender() ;
	}
}
