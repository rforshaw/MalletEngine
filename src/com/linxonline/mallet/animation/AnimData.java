package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.SourceCallback ;

import com.linxonline.mallet.animation.Sprite ;

import com.linxonline.mallet.renderer.IUpdater ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

public class AnimData<T extends AnimData> implements Anim<T>, Cacheable
{
	private final List<SourceCallback> callbacks = MalletList.<SourceCallback>newList() ;
	private final Program program ; 
	private String file   = null ;
	private Draw draw     = null ;

	private World world = null ;
	private Sprite sprite = null ;
	private DrawBuffer drawBuffer ;
	private IUpdater<Draw, GeometryBuffer> updater ;

	private int order         = 0 ;
	private boolean play      = false ;
	private float elapsedTime = 0.0f ;
	private int prevFrame     = 0 ;
	private int currFrame     = 0 ;				// Current frame 
	private float frameDelta  = 0.0f ;			// Amount of time that needs to elapse before next frame
	private int length        = 0 ;				// How many frames

	public AnimData()
	{
		this( null, null ) ;
	}

	public AnimData( final String _file, final Draw _draw )
	{
		this( _file, ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ), _draw ) ;
	}

	public AnimData( final String _file, final Program _program, final Draw _draw )
	{
		program = _program ;
		file = _file ;
		draw = _draw ;
	}

	public int setOrder( final int _order )
	{
		order = _order ;
		return order ;
	}
	
	public void setWorld( final World _world )
	{
		world = _world ;
	}

	public void setUpdater( final IUpdater<Draw, GeometryBuffer> _updater, final DrawBuffer _buffer )
	{
		updater = _updater ;
		drawBuffer = _buffer ;
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
		prevFrame = 0 ;
		currFrame = 0 ;

		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).stop() ;
		}
	}

	public boolean update( final float _dt )
	{
		boolean update = false ;
		if( play == true )
		{
			elapsedTime += _dt ;
			if( elapsedTime >= frameDelta )
			{
				update = true ;
				changeTexture( draw, sprite ) ;
				elapsedTime -= frameDelta ;
				prevFrame = currFrame ;
				currFrame = ++currFrame % length ; // Increment frame, reset to 0 if reaches length.
				if( currFrame == 0 )
				{
					finishedCallbacks() ;
				}
			}
			updateCallbacks() ;
		}
		return update ;
	}

	private void changeTexture( final Draw _draw, final Sprite _sprite )
	{
		final Sprite.Frame p = _sprite.getFrame( prevFrame ) ;		// Grab the previous frame
		final Sprite.Frame c = _sprite.getFrame( currFrame ) ;		// Grab the current frame

		if( prevFrame == currFrame || p.path.equals( c.path ) == false )
		{
			// We only want to remap the programs texture 
			// if the sprite is not using a spritesheet.
			program.mapUniform( "inTex0", c.path ) ;
			if( drawBuffer != null )
			{
				DrawAssist.update( drawBuffer ) ;
			}
		}

		// If using a sprite sheet the UV coordinates 
		// will have changed. Though there is a possibility
		// that a change in texture could result in different 
		// UV's too. Or the texture stays the same and the UV 
		// coordinates have changed, to simulate a scrolling 
		// animation, like water.
		Shape.updatePlaneUV( _draw.getShape(), c.uv ) ;
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
		return ( world != null ) ? world : WorldAssist.getDefault() ;
	}

	public Program getProgram()
	{
		return program ;
	}

	public Draw getDraw()
	{
		return draw ;
	}

	public int getOrder()
	{
		return order ;
	}

	public IUpdater<Draw, GeometryBuffer> getUpdater()
	{
		return updater ;
	}

	private void updateCallbacks()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).tick( ( float )currFrame * frameDelta ) ;
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
		prevFrame   = 0 ;
		currFrame   = 0 ; 
		frameDelta  = 0.0f ;
		length      = 0 ;
	}
}
