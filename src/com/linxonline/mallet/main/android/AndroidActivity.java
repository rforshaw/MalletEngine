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
import com.linxonline.mallet.game.statemachine.* ;
import com.linxonline.mallet.game.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.game.android.* ;
import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.system.android.AndroidSystem ;
import com.linxonline.mallet.input.android.AndroidInputListener ;

public class AndroidActivity extends Activity
							implements EventHandler
{
	private ArrayList<AndroidInputListener> inputListeners = new ArrayList<AndroidInputListener>() ;
	protected AndroidStarter starter ;
	protected GameThread gameThread = null ;
	protected boolean fileSystemLoaded = false ;

	public AndroidActivity()
	{
		super() ;
	}

	@Override
	public void onCreate( Bundle _savedInstance )
	{
		requestWindowFeature( Window.FEATURE_NO_TITLE ) ;
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
									   WindowManager.LayoutParams.FLAG_FULLSCREEN ) ;
		super.onCreate( _savedInstance ) ;

		final AudioManager audioManager = ( AudioManager )getSystemService( Context.AUDIO_SERVICE ) ;
		audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, 
									  audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC ), 
									  0 ) ;
	}

	@Override
	public void onResume()
	{
		super.onResume() ;
		startGameThread() ;
	}

	@Override
	public void onPause()
	{
		super.onPause() ;
		stopGameThread() ;
	}

	@Override
	public void onStop()
	{
		super.onStop() ;
		stopGameThread() ;
	}

	public void addAndroidInputListener( AndroidInputListener _listener )
	{
		if( containsInputListener( _listener ) == true )
		{
			return ;
		}

		inputListeners.add( _listener ) ;
	}
	
	public void removeAndroidInputListener( AndroidInputListener _listener )
	{
		if( containsInputListener( _listener ) == true )
		{
			inputListeners.remove( _listener ) ;
		}
	}
	
	@Override
	public boolean onKeyDown( int _keyCode, KeyEvent _event )
	{
		if( controlVolume( _keyCode ) == true )
		{
			return true ;
		}

		for( AndroidInputListener listener : inputListeners )
		{
			listener.onKeyDown( _keyCode, _event ) ;
		}

		return true ;
	}

	@Override
	public boolean onKeyUp( int _keyCode, KeyEvent _event )
	{
		for( AndroidInputListener listener : inputListeners )
		{
			listener.onKeyUp( _keyCode, _event ) ;
		}

		return true ;
	}

	@Override
	public boolean onTouchEvent( MotionEvent _event )
	{
		for( AndroidInputListener listener : inputListeners )
		{
			listener.onTouchEvent( _event ) ;
		}

		return true ;
	}

	@Override
	public void onConfigurationChanged( Configuration _newConfig )
	{
		super.onConfigurationChanged( _newConfig ) ;
		// Update the render for new Screen Dimensions
		//androidSystem.setContentView() ;
		//androidSystem.addEvent( new Event( "REFRESH_STATE", null ) ) ;
		//initActivity() ;
	}

	@Override
	public void processEvent( final Event _event ) {}

	@Override
	public String getName()
	{
		return "ANDROID_ACTIVITY" ;
	}

	@Override
	public String[] getWantedEventTypes()
	{
		return Event.ALL_EVENT_TYPES ;
	}

	@Override
	public void passEvent( final Event _event )
	{
		starter.getAndroidSystem().addEvent( _event ) ;
	}

	private synchronized void startGameThread()
	{
		System.out.println( "Start Game Thread" ) ;
		if( starter == null )
		{
			starter = new AndroidStarter( this ) ;
		}

		if( gameThread == null )
		{
			gameThread = new GameThread()
			{
				public void run() 
				{
					starter.init() ;
				}
			} ;

			gameThread.start() ;
		}
	}
	
	private synchronized void stopGameThread()
	{
		/*if( gameThread != null )
		{
			starter.getAndroidSystem().stopSystem() ;
			gameThread.interrupt() ;
			gameThread = null ;
		}*/
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

	private boolean containsInputListener( AndroidInputListener _listener )
	{
		return inputListeners.contains( _listener ) ;
	}
}