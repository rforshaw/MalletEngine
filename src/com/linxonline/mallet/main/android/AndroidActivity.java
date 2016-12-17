package com.linxonline.mallet.main.android ;

import android.app.Activity ;
import android.content.pm.ActivityInfo ;
import android.content.res.Configuration ;
import android.content.Context ;
import android.view.Display ;
import android.view.KeyEvent ;
import android.view.MotionEvent ;
import android.view.Window ;
import android.view.WindowManager ;
import android.os.Bundle ;

import android.media.* ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.main.game.statemachine.* ;
import com.linxonline.mallet.main.game.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.system.android.gl.* ;
import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.input.android.AndroidInputListener ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

public class AndroidActivity extends Activity
							implements EventHandler
{
	private final ArrayList<AndroidInputListener> inputListeners = new ArrayList<AndroidInputListener>() ;
	private final Notify<Object> startGame = new Notify<Object>()
	{
		public void inform( final Object _noData )
		{
			// Only start the game thread once we know the 
			// OpenGL context has been initialised.
			System.out.println( "Inform: Starting Game Thread." ) ;
			startGameThread() ;
		}
	} ;

	protected AndroidStarter starter = null ;
	protected Thread gameThread = null ;

	public AndroidActivity()
	{
		super() ;
	}

	@Override
	public void onCreate( final Bundle _savedInstance )
	{
		System.out.println( "onCreate()" ) ;
		requestWindowFeature( Window.FEATURE_NO_TITLE ) ;
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
							  WindowManager.LayoutParams.FLAG_FULLSCREEN ) ;
		super.onCreate( _savedInstance ) ;
	}

	@Override
	public void onResume()
	{
		System.out.println( "onResume()" ) ;
		super.onResume() ;

		if( starter == null )
		{
			System.out.println( "INIT ANDROID STARTER" ) ;
			starter = constructStarter( this, startGame ) ;
			starter.init() ;
		}

		starter.getAndroidSystem().startSystem() ;
	}

	@Override
	public void onPause()
	{
		System.out.println( "onPause()" ) ;
		super.onPause() ;

		starter.stop() ;
		stopGameThread() ;
	}

	public void onDestroy()
	{
		System.out.println( "onDestroy()" ) ;
		super.onDestroy() ;

		starter.shutdown() ;
		starter = null ;
	}

	public void addAndroidInputListener( final AndroidInputListener _listener )
	{
		if( containsInputListener( _listener ) == true )
		{
			return ;
		}

		inputListeners.add( _listener ) ;
	}

	public void removeAndroidInputListener( final AndroidInputListener _listener )
	{
		if( containsInputListener( _listener ) == true )
		{
			inputListeners.remove( _listener ) ;
		}
	}

	@Override
	public boolean onKeyDown( final int _keyCode, final KeyEvent _event )
	{
		if( controlVolume( _keyCode ) == true )
		{
			return true ;
		}

		for( final AndroidInputListener listener : inputListeners )
		{
			listener.onKeyDown( _keyCode, _event ) ;
		}

		return true ;
	}

	@Override
	public boolean onKeyUp( final int _keyCode, final KeyEvent _event )
	{
		for( final AndroidInputListener listener : inputListeners )
		{
			listener.onKeyUp( _keyCode, _event ) ;
		}

		return true ;
	}

	@Override
	public boolean onTouchEvent( final MotionEvent _event )
	{
		for( final AndroidInputListener listener : inputListeners )
		{
			listener.onTouchEvent( _event ) ;
		}

		return true ;
	}

	@Override
	public void processEvent( final Event _event ) {}

	@Override
	public String getName()
	{
		return "ANDROID_ACTIVITY" ;
	}

	@Override
	public ArrayList<EventType> getWantedEventTypes()
	{
		final ArrayList<EventType> types = new ArrayList<EventType>() ;
		types.add( Event.ALL_EVENT_TYPES ) ;
		return types ;
	}

	@Override
	public void passEvent( final Event _event )
	{
		starter.getAndroidSystem().getEventInterface().addEvent( _event ) ;
	}

	/**
		Implemented by Event Handler.
	*/
	@Override
	public void reset() {}

	public AndroidStarter constructStarter( final AndroidActivity _activity, final Notify<Object> _notify )
	{
		return new AndroidTestStarter( _activity, _notify ) ;
	}

	private synchronized void startGameThread()
	{
		if( gameThread == null )
		{
			gameThread = new Thread( "GAME THREAD" )
			{
				public void run() 
				{
					starter.run() ;
				}
			} ;

			System.out.println( "Starting Game Thread" ) ;
			gameThread.start() ;
		}
	}

	private synchronized void stopGameThread()
	{
		if( gameThread != null )
		{
			System.out.println( "Stopping Game Thread" ) ;
			try
			{
				gameThread.join() ;
			}
			catch( InterruptedException ex )
			{
				gameThread.interrupt() ;
			}
			finally
			{
				gameThread = null ;
			}
		}
	}

	private boolean controlVolume( final int _keyCode )
	{
		if( _keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
		{
			final AudioManager audioManager = ( AudioManager )getSystemService( Context.AUDIO_SERVICE ) ;
			audioManager.adjustStreamVolume( AudioManager.STREAM_MUSIC, 
											AudioManager.ADJUST_LOWER, 
											0 ) ;
			return true ;
		}
		else if( _keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			final AudioManager audioManager = ( AudioManager )getSystemService( Context.AUDIO_SERVICE ) ;
			audioManager.adjustStreamVolume( AudioManager.STREAM_MUSIC, 
											AudioManager.ADJUST_RAISE, 
											0 ) ;
			return true ;
		}

		return false ;
	}

	private boolean containsInputListener( final AndroidInputListener _listener )
	{
		return inputListeners.contains( _listener ) ;
	}
}
