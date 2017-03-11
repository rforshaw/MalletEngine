package com.linxonline.mallet.animation ;

import java.util.List ;

import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.SourceCallback ;

import com.linxonline.mallet.animation.Sprite ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

public class AnimData<T extends AnimData> implements Anim<T>, Cacheable
{
	private final List<SourceCallback> callbacks = MalletList.<SourceCallback>newList() ;
	private String file   = null ;
	private Draw draw     = null ;

	private WeakReference<World> world = null ;
	private Sprite sprite = null ;

	private boolean play      = false ;
	private float elapsedTime = 0.0f ;
	private int frame         = 0 ;				// Current frame 
	private float frameDelta  = 0.0f ;			// Amount of time that needs to elapse before next frame
	private int length        = 0 ;				// How many frames

	public AnimData() {}

	public AnimData( final String _file, final Draw _draw )
	{
		file = _file ;
		draw = _draw ;
	}

	public void setWorld( final World _world )
	{
		world = new WeakReference<World>( _world ) ;
	}

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
		}
	}

	public void removeCallback( final SourceCallback _callback )
	{
		if( callbacks.remove( _callback ) == true )
		{
			_callback.callbackRemoved() ;
		}
	}

	/**
		Begin running the animation, inform the callbacks 
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

	private void changeTexture( final Draw _draw, final Sprite _sprite )
	{
		final Sprite.Frame f = sprite.getFrame( frame ) ;		// Grab the current frame

		ProgramAssist.map( DrawAssist.getProgram( draw ), "inTex0", f.path ) ;

		// If using a sprite sheet the UV coordinates 
		// will have changed. Though there is a possibility
		// that a change in texture could result in different 
		// UV's too. Or the texture stays the same and the UV 
		// coordinates have changed, to simulate a scrolling 
		// animation, like water.
		Shape.updatePlaneUV( DrawAssist.getDrawShape( _draw ), f.uv1, f.uv2 ) ;
		DrawAssist.forceUpdate( _draw ) ;
	}

	public void setSprite( final Sprite _sprite )
	{
		sprite     = _sprite ;
		changeTexture( draw, sprite ) ;
		frameDelta = 1.0f / sprite.framerate ;
		length     = sprite.size() ;
	}

	public String getFile()
	{
		return file ;
	}

	public Sprite getSprite()
	{
		return sprite ;
	}

	public World getWorld()
	{
		return ( world != null ) ? world.get() : null ;
	}

	public Draw getDraw()
	{
		return draw ;
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

	public void removeCallback()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).callbackRemoved() ;
		}
		callbacks.clear() ;
	}

	@Override
	public void reset()
	{
		file   = null ;
		draw   = null ;
		sprite = null ;
		world  = null ;

		play        = false ;
		elapsedTime = 0.0f ;
		frame       = 0 ; 
		frameDelta  = 0.0f ;
		length      = 0 ;
	}
}
