package com.linxonline.mallet.main.android ;

import android.app.Activity ;
import android.content.pm.ActivityInfo ;
import android.content.res.Configuration ;
import android.content.Context ;
import android.view.Display ;
import android.view.KeyEvent ;
import android.view.MotionEvent ;
import android.os.Bundle ;
import android.util.DisplayMetrics ;
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
	protected boolean fileSystemLoaded = false ;
	protected AndroidSystem androidSystem = null ;
	protected GameSystem gameSystem = new GameSystem() ;
	protected GameThread gameThread = null ;

	public AndroidActivity()
	{
		super() ;
	}

	protected void init()
	{
		androidSystem = new AndroidSystem( this ) ;
		androidSystem.initSystem() ;
		androidSystem.addEventHandler( this ) ;
		gameSystem.setSystem( androidSystem ) ;

		gameSystem.addGameState( new GameState( "DEFAULT" )
		{
			// Called when state is started.
			public void initGame()
			{
				renderTextureExample() ;
				//renderAnimationExample() ;
				renderTextExample() ;
			}

			/**
				Add a texture and render directly to the renderer
			**/
			public void renderTextureExample()
			{
				eventSystem.addEvent( DrawFactory.createTexture( "com.linxonline:drawable/moomba",//"base/textures/moomba.png", 			// Texture Location
																	new Vector3( 0.0f, 0.0f, 0.0f ),	// Position
																	new Vector2( -32, -32 ), 			// Offset
																	new Vector2( 64, 64 ),				// Dimension, how large - scaled
																	null,								// fill, texture repeat
																	null,								// clip
																	null,								// clip offset
																	10 ) ) ;							// layer
			}

			/**
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				eventSystem.addEvent( DrawFactory.createText(  "Hello World!", 						// Text
																new Vector3( 0.0f, 0.0f, 0.0f ),	// Position
																new Vector2( 0, 0 ), 				// Offset
																new MalletFont( "Arial", 20 ),		// Mallet Font
																null,								// Mallet Colour
																null,								// clip
																null,								// clip offset
																10,									// layer
																2 ) ) ;								// Text alignment, Centre
			}
		} ) ;
		
		gameSystem.setDefaultGameState( "DEFAULT" ) ;
		startGameThread() ;
	}

	public void initActivity()
	{
		final DisplayMetrics display = new DisplayMetrics() ;
		getWindowManager().getDefaultDisplay().getMetrics( display ) ; 
		final int width = display.widthPixels ;
		final int height = display.heightPixels ;

		final int renderWidth = width ;
		final int renderHeight = height ;

		androidSystem.setDisplayDimensions( new Vector2( width, height ) ) ;
		androidSystem.setRenderDimensions( new Vector2( renderWidth, renderHeight ) ) ;
		androidSystem.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;

		final Settings config = new Settings() ;
		config.addInteger( "RENDERWIDTH", renderWidth ) ;
		config.addInteger( "RENDERHEIGHT", renderHeight ) ;
	}

	@Override
	public void onCreate( Bundle _savedInstance )
	{
		super.onCreate( _savedInstance ) ;

		final AudioManager audioManager = ( AudioManager )getSystemService( Context.AUDIO_SERVICE ) ;
		audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, 
									  audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC ), 
									  0 ) ;

		if( fileSystemLoaded == false )
		{
			loadFileSystem() ;
			init() ;
			initActivity() ;
			fileSystemLoaded = true ;
		}
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
		androidSystem.setContentView() ;
		androidSystem.addEvent( new Event( "REFRESH_STATE", null ) ) ;
		initActivity() ;
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
		androidSystem.addEvent( _event ) ;
	}

	private synchronized void startGameThread()
	{
		System.out.println( "Start Game Thread" ) ;
		if( gameThread == null )
		{
			gameThread = new GameThread()
			{
				public void run() 
				{
					gameSystem.runSystem() ;
				}
			} ;

			androidSystem.startSystem() ;
			gameThread.start() ;
		}
	}
	
	private synchronized void stopGameThread()
	{
		if( gameThread != null )
		{
			androidSystem.stopSystem() ;
			gameThread.interrupt() ;
			gameThread = null ;
		}
	}
	
	private void loadFileSystem()
	{
		final AndroidFileSystem fileSystem = new AndroidFileSystem() ;
		GlobalFileSystem.setFileSystem( fileSystem ) ;
		
		fileSystem.init( this ) ;
		fileSystem.scanBaseDirectory() ;
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