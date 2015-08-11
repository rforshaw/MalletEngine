package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.audio.AudioFactory ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public class SoundComponent extends EventComponent implements SourceCallback
{
	private static final int SOUND_NOT_SET = -1 ;

	private final HashMap<String, Event<Settings>> sounds = new HashMap<String, Event<Settings>>() ;
	private Component.ReadyCallback toDestroy = null ;

	private String defaultSound = null ;				// Name of the default sound, used as a fallback if all else fails.
	private String toPlaySound = null ;				// The Animation to be played, once the previous Anim ID is recieved.

	private boolean waitForID = false ;						// true = waiting for sound ID, false = not waiting for ID
	private int soundID = -1 ;								// Denotes the id of the current running sound.

	public SoundComponent()
	{
		super( "SOUND", "SOUNDCOMPONENT" ) ;
	}

	public SoundComponent( final String _name )
	{
		super( _name ) ;
	}

	public void addSound( final String _name, final Event<Settings> _sound )
	{
		sounds.put( _name, _sound ) ;
	}

	public void removeSound( final String _name )
	{
		sounds.remove( _name ) ;
	}

	public Event<Settings> getSound( final String _name )
	{
		return sounds.get( _name ) ;
	}

	public void setDefaultSound( final String _name )
	{
		defaultSound = _name ;
	}

	/**
		We need to make sure we aren't waiting for any 
		sound ID's before we allow the parent to destroy 
		themselves.
	*/
	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		toDestroy = _callback ;
	}

	/**
		The progression through the audio file 
		being played.
	*/
	@Override
	public void tick( final float _dt )
	{
		//System.out.println( _dt ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		if( toDestroy != null && waitForID == false )
		{
			// Ensure that the component is not waiting 
			// for an ID from the Animation System.
			// Before we allow the parent to be destroyed.
			toDestroy.ready( this ) ;
		}
	}
	
	/**
		Begin playing specified sound as soon as possible.
		If called very quickly, repeatedly, some sounds 
		may never get rendered.
	**/
	public void playSound( final String _name )
	{
		if( toDestroy != null )
		{
			// Prevent any more sounds being run 
			// if the parent is being destroyed. 
			return ;
		}

		if( waitForID == true )
		{
			// Currently waiting for the ID of the previous
			// sound. Store the Animation name and wait till 
			// we get the ID, before playing the new Animation.
			toPlaySound = _name ;
			return ;
		}

		stopSound() ; 									// Stop the previous sound, else we'll leak sounds
		final Event<Settings> event = sounds.get( _name ) ;
		if( event != null )
		{
			waitForID = true ;							// Need to wait for ID before changing sound again
			passEvent( event ) ;						// Inform the Animation System of the new Animation.
		}
	}

	/**
		Remove the current sound from the Animation system.
	**/
	public void stopSound()
	{
		if( soundID != SOUND_NOT_SET )
		{
			passEvent( AudioFactory.removeAudio( soundID ) ) ;
			soundID = SOUND_NOT_SET ;
		}
	}

	public void recieveID( final int _id )
	{
		soundID = _id ;
		waitForID = false ;		// We've recieved the ID so we can accept other sound requests

		if( toPlaySound != null )
		{
			// If toPlayAnim is set, then another sound 
			// was requested before the previous sounds ID 
			// could be recieved. We can now play the new sound.
			playSound( toPlaySound ) ;
			toPlaySound = null ;
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
		if( defaultSound != null )
		{
			final Event<Settings> event = sounds.get( defaultSound ) ;
			if( event != null )
			{
				// Add the default Anim to the Initial Events.
				// Ensure the component knows it needs to wait for the ID
				// before requesting another sound.
				_events.add( event ) ;
				waitForID = true ;
			}
		}
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( AudioFactory.removeAudio( soundID ) ) ;

		toDestroy = null ;		// Blank toDestroy incase the component is reused.
	}
}