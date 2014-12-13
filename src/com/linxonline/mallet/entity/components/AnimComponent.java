package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public class AnimComponent extends EventComponent implements SourceCallback
{
	private static final int ANIM_NOT_SET = -1 ;

	private final HashMap<String, Event<Settings>> animations = new HashMap<String, Event<Settings>>() ;
	private Component.ReadyCallback toDestroy = null ;

	private String defaultAnim = null ;			// Name of the default animation, used as a fallback if all else fails.
	private String toPlayAnim = null ;			// The Animation to be played, once the previous Anim ID is recieved.

	private boolean waitForID = false ;			// true = waiting for animation ID, false = not waiting for ID
	private int animationID = -1 ;				// Denotes the id of the current running animation.

	public AnimComponent()
	{
		super( "ANIM" ) ;
	}

	public AnimComponent( final String _name )
	{
		super( _name ) ;
	}

	public void addAnimation( final String _name, final Event<Settings> _anim )
	{
		animations.put( _name, _anim ) ;
	}

	public void removeAnimation( final String _name )
	{
		animations.remove( _name ) ;
	}

	public Event<Settings> getAnimation( final String _name )
	{
		return animations.get( _name ) ;
	}

	public void setDefaultAnim( final String _name )
	{
		defaultAnim = _name ;
	}

	/**
		We need to make sure we aren't waiting for any 
		animation ID's before we allow the parent to destroy 
		themselves.
	*/
	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		toDestroy = _callback ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		if( toDestroy != null && waitForID == false )
		{
			toDestroy.ready( this ) ;
		}
	}
	
	/**
		Begin playing specified animation as soon as possible.
		If called very quickly, repeatedly, some animations 
		may never get rendered.
	**/
	public void playAnimation( final String _name )
	{
		if( toDestroy != null )
		{
			// Prevent any more animations being run 
			// if the parent is being destroyed. 
			return ;
		}

		if( waitForID == true )
		{
			// Currently waiting for the ID of the previous
			// animation. Store the Animation name and wait till 
			// we get the ID, before playing the new Animation.
			toPlayAnim = _name ;
			return ;
		}

		stopAnimation() ; 								// Stop the previous animation, else we'll leak animations
		final Event<Settings> event = animations.get( _name ) ;
		if( event != null )
		{
			waitForID = true ;							// Need to wait for ID before changing animation again
			passEvent( event ) ;						// Inform the Animation System of the new Animation.
		}
	}

	/**
		Remove the current animation from the Animation system.
	**/
	public void stopAnimation()
	{
		if( animationID != ANIM_NOT_SET )
		{
			passEvent( AnimationFactory.removeAnimation( animationID ) ) ;
			animationID = ANIM_NOT_SET ;
		}
	}

	public void recieveID( final int _id )
	{
		animationID = _id ;
		waitForID = false ;		// We've recieved the ID so we can accept other animation requests

		if( toPlayAnim != null )
		{
			// If toPlayAnim is set, then another animation 
			// was requested before the previous animations ID 
			// could be recieved. We can now play the new animation.
			playAnimation( toPlayAnim ) ;
			toPlayAnim = null ;
		}
	}

	public void callbackRemoved() {}

	public void start() {}
	public void pause() {}
	public void stop() {}

	public void finished() {}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		if( defaultAnim != null )
		{
			final Event<Settings> event = animations.get( defaultAnim ) ;
			if( event != null )
			{
				// Add the default Anim to the Initial Events.
				// Ensure the component knows it needs to wait for the ID
				// before requesting another animation.
				_events.add( event ) ;
				waitForID = true ;
			}
		}
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( AnimationFactory.removeAnimation( animationID ) ) ;

		toDestroy = null ;		// Blank toDestroy incase the component is reused.
	}
}