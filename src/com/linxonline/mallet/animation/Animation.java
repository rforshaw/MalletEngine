package com.linxonline.mallet.animation ;

import java.util.ArrayList ;

import com.linxonline.mallet.resources.texture.Sprite ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.SourceCallback ;

public class Animation
{
	private final ArrayList<SourceCallback> callbacks = new ArrayList<SourceCallback>() ;
	public final int id ;
	private final Sprite sprite ;
	private final Event event ;

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

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
			_callback.recieveID( id ) ;
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
	
	private void changeTexture( final Event _event, final Sprite _sprite )
	{
		final Settings settings = ( Settings )_event.getVariable() ;
		final String file = sprite.getTexture( frame ) ;

		settings.addString( "FILE", file ) ;
		settings.addObject( "TEXTURE", null ) ;
	}

	public void update( final float _dt )
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

	private void updateCallbacks()
	{
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).update( ( float )frame ) ;
		}
	}
	
	public void destroy()
	{
		sprite.unregister() ;
	}
}