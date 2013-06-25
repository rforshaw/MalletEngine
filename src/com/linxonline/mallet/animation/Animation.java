package com.linxonline.mallet.animation ;

import com.linxonline.mallet.resources.texture.Sprite ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;

public class Animation
{
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
	}

	public void destroy()
	{
		sprite.unregister() ;
	}
}