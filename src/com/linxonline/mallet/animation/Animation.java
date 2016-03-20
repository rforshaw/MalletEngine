package com.linxonline.mallet.animation ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.resources.texture.Texture ;
import com.linxonline.mallet.resources.texture.Sprite ;
import com.linxonline.mallet.resources.model.Model ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.maths.Vector2 ;

public class Animation implements IDInterface, Cacheable
{
	private final ArrayList<SourceCallback> callbacks = new ArrayList<SourceCallback>() ;
	public int id = 0 ;
	private Sprite sprite = null ;
	protected Draw draw = null ;

	private boolean play = false ;

	private float elapsedTime = 0.0f ;
	private int frame = 0 ;						// Current frame 
	private float frameDelta = 0.0f ;			// Amount of time that needs to elapse before next frame
	private int length = 0 ;					// How many frames

	public Animation() {}

	public Animation( final int _id, final Draw _draw, final Sprite _sprite )
	{
		setAnimation( _id, _draw, _sprite ) ;
	}

	public void setAnimation( final int _id, Draw _draw, final Sprite _sprite )
	{
		id = _id ;
		draw = _draw ;
		sprite = _sprite ;
		frameDelta = 1.0f / sprite.framerate ;
		length = sprite.size() ;

		changeTexture( draw, sprite ) ;
	}

	/**
		Passes in the render ID for the particular Animation.
		This id allows the animation to make modifications 
		to what it renders out.
	**/
	public void recievedID( final int _id )
	{
		// Only call recieveID() once we have acquired the render ID.
		// Else making modifications will become hard!
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).recieveID( id ) ;
		}
	}

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
			if( draw != null )
			{
				_callback.recieveID( id ) ;
			}
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

	/**
		Beging running the animation, inform the callbacks 
		the animation has started.
	*/
	public void play()
	{
		play = true ;
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).start() ;
		}
	}

	/**
		Pause the running animation, inform the callbacks 
		the animation has been paused.
	*/
	public void pause()
	{
		play = false ;
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).pause() ;
		}
	}

	/**
		Stop the running the animation, inform the callbacks 
		the animation has stopped.
		Calling play, will start the animation from the begining.
	*/
	public void stop()
	{
		play = false ;
		frame = 0 ;

		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).stop() ;
		}
	}

	private void changeTexture( final Draw _draw, final Sprite _sprite )
	{
		final Sprite.Frame f = sprite.getFrame( frame ) ;		// Grab the current frame

		// Doesn't assume the next frame is part of a spritesheet.
		// We could check to see if the current path is the same as 
		// f.path. Note sure if this would be more performant.
		// Though the Animation System doesn't assume, the renderer 
		// does, this will need to be resolved..
		DrawAssist.clearTextures( _draw ) ;
		DrawAssist.amendTexture( _draw, f.path ) ;

		// If using a sprite sheet the UV coordinates 
		// will have changed. Though there is a possibility
		// that a change in texture could result in different 
		// UV's too. Or the texture stays the same and the UV 
		// coordinates have changed, to simulate a scrolling 
		// animation, like water.
		Shape.updatePlaneUV( DrawAssist.getDrawShape( _draw ), f.uv1, f.uv2 ) ;
		DrawAssist.forceUpdate( _draw ) ;
	}

	public void update( final float _dt )
	{
		if( play == true )
		{
			elapsedTime += _dt ;
			if( elapsedTime >= frameDelta )
			{
				changeTexture( draw, sprite ) ;
				elapsedTime -= frameDelta ;
				frame = ++frame % length ; // Increment frame, reset to 0 if reaches length.
				if( frame == 0 )
				{
					finishedCallbacks() ;
				}
			}
			updateCallbacks() ;
		}
	}

	private void updateCallbacks()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).tick( ( float )frame * frameDelta ) ;
		}
	}

	private void finishedCallbacks()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).finished() ;
		}
	}

	/**
		Reset the Animation object so it 
		can be used for another animation 
		request.
		Reset should clear everything, no residual 
		information should be preserved.
	*/
	public void reset()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).callbackRemoved() ;
		}
		callbacks.clear() ;

		id = 0 ;
		sprite = null ;		// Is the Sprite unregistered before a reset?
		draw = null ;

		play = false ;

		elapsedTime = 0.0f ;
		frame = 0 ; 
		frameDelta = 0.0f ;
		length = 0 ;
	}

	/**
		Unregister the sprite being used by this animation.
	*/
	public void destroy()
	{
		sprite.unregister() ;
	}
}