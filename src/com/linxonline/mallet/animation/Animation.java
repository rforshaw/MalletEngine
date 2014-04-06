package com.linxonline.mallet.animation ;

import java.util.ArrayList ;

import com.linxonline.mallet.resources.Sprite ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.id.IDInterface ;

public class Animation implements IDInterface
{
	private final ArrayList<SourceCallback> callbacks = new ArrayList<SourceCallback>() ;
	public final int id ;
	private final Sprite sprite ;
	private final Event event ;
	public int renderID = -1 ;

	private boolean play = false ;

	private float elapsedTime = 0.0f ;
	private int frame = 0 ;						// Current frame 
	private final float frameDelta ;				// Amount of time that needs to elapse before next frame
	private final int length ;					// How many frames

	public Animation( final int _id, final Event _event, final Sprite _sprite )
	{
		id = _id ;
		event = _event ;
		sprite = _sprite ;
		frameDelta = 1.0f / sprite.framerate ;
		length = sprite.size() ;
		
		changeTexture( event, sprite ) ;
	}

	/**
		Passes in the render ID for the particular Animation.
		This id allows the animation to make modifications 
		to what it renders out.
	**/
	public void recievedID( final int _id )
	{
		renderID = _id ;

		// Only call recieveID() once we have acquired the render ID.
		// Else making modifications will become hard!
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).recieveID( id ) ;
		}
	}

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
			if( renderID > -1 ) { _callback.recieveID( id ) ; }
		}
	}

	public void removeCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == true )
		{
			callbacks.remove( _callback ) ;
			_callback.callbackRemoved() ;
		}
	}

	public void play()
	{
		play = true ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).start() ;
		}
	}

	public void pause()
	{
		play = false ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).pause() ;
		}
	}

	public void stop()
	{
		play = false ;
		frame = 0 ;

		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).stop() ;
		}
	}
	
	private void changeTexture( final Event _event, final Sprite _sprite )
	{
		final Settings settings = ( Settings )_event.getVariable() ;
		final String file = sprite.getTexture( frame ) ;

		settings.addString( "FILE", file ) ;
		settings.addObject( "TEXTURE", null ) ;
	}

	public void update( final float _dt )
	{
		if( play == true )
		{
			elapsedTime += _dt ;
			if( elapsedTime >= frameDelta )
			{
				changeTexture( event, sprite ) ;
				elapsedTime -= frameDelta ;
				frame = ++frame % length ; // Increment frame, reset to 0 if reaches length.
			}
			updateCallbacks() ;
		}
	}

	private void updateCallbacks()
	{
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).update( ( float )frame * frameDelta ) ;
		}
	}
	
	public void destroy()
	{
		sprite.unregister() ;
	}
}