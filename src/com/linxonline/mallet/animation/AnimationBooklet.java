package com.linxonline.mallet.animation ;

import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;

/**
	An animation booklet contains a set of animation sequences.
	An animation sequence can be played by calling play() and
	passing the key that is associated with a specific sequence.
	The animation will continue to play until pause() or stop()
	is called. Attach an IListener to the booklet to find out when
	the next frame is triggered, or when the animation sequence
	has changed.
*/
public final class AnimationBooklet<T extends Animation.Frame> implements AnimationSystem.IAnimation
{
	private Animation<T> currentAnim = null ;
	private T currentFrame = null ;

	private float currentElapsedTime = 0.0f ;

	private boolean paused = false ;
	private final IListener<T> listener ;

	private final Map<String, Animation<T>> animations = MalletMap.<String, Animation<T>>newMap() ;

	public AnimationBooklet( final IListener<T> _listener )
	{
		listener = _listener ;
	}

	public void addAnimation( final String _key, final Animation<T> _anim )
	{
		animations.put( _key, _anim ) ;
	}

	public void play( final String _key )
	{
		final Animation<T> anim = animations.get( _key ) ;

		paused = false ;
		if( currentAnim != anim )
		{
			listener.animationChanged( currentAnim, anim ) ;
			currentAnim = anim ;
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
		currentAnim = null ;
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

		if( currentAnim != null )
		{
			final T next = currentAnim.getFrame( currentElapsedTime ) ;
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

	public IListener<T> getListener()
	{
		return listener ;
	}

	public interface IListener<T extends Animation.Frame>
	{
		public void init() ;
		public void shutdown() ;

		public void animationChanged( final Animation<T> _previous, final Animation<T> _next ) ;
		public void frameChanged( final T _previous, final T _next ) ;
	}
}
