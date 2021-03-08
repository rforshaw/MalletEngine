package com.linxonline.mallet.animation ;

import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.animation.MalletSprite ;

public class SpriteAnimations implements AnimationSystem.IAnimation
{
	private MalletSprite currentSprite = null ;
	private MalletSprite.Frame currentFrame = null ;
	private float currentElapsedTime = 0.0f ;
	private boolean paused = false ;
	private final IListener listener ;

	private final Map<String, MalletSprite> sprites = MalletMap.<String, MalletSprite>newMap() ;

	public SpriteAnimations( final IListener _listener )
	{
		listener = _listener ;
	}

	public void addSprite( final String _key, final MalletSprite _sprite )
	{
		sprites.put( _key, _sprite ) ;
	}

	public void play( final String _key )
	{
		final MalletSprite sprite = sprites.get( _key ) ;

		paused = false ;
		if( currentSprite != sprite )
		{
			listener.spriteChanged( currentSprite, sprite ) ;
			currentSprite = sprite ;
			currentFrame = null ;
		}
	}

	public void pause()
	{
		paused = true ;
	}

	public void stop()
	{
		pause() ;
		currentElapsedTime = 0.0f ;
		currentSprite = null ;
		currentFrame = null ;
	}

	@Override
	public void update( final float _dt )
	{
		if( paused == true )
		{
			// Don't update the elapsed time 
			// if the animation should be paused.
			return ;
		}

		currentElapsedTime += _dt ;

		if( currentSprite != null )
		{
			final MalletSprite.Frame next = currentSprite.getFrame( currentElapsedTime ) ;
			if( next != currentFrame )
			{
				listener.frameChanged( currentFrame, next ) ;
				currentFrame = next ;
			}
		}
	}

	@Override
	public void added()
	{
		listener.init() ;
	}

	@Override
	public void removed()
	{
		listener.shutdown() ;
	}

	public IListener getListener()
	{
		return listener ;
	}

	public interface IListener
	{
		public void init() ;
		public void shutdown() ;

		public void spriteChanged( final MalletSprite _previous, final MalletSprite _next ) ;
		public void frameChanged( final MalletSprite.Frame _previous, final MalletSprite.Frame _next ) ;
	}
}
