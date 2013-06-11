package com.linxonline.mallet.audio ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.sound.* ;

// Play Sound
	// Set callback on Sound
	// Get Sound ID
// Modify running sound

public class AudioSystem implements EventHandler
{
	private static final String[] EVENT_TYPES = { "AUDIO" } ;
	private static final String REQUEST_TYPE = "REQUEST_TYPE" ;
	private static final String AUDIO_FILE = "AUDIO_FILE" ;
	private static final String MODIFY_AUDIO = "MODIFY_AUDIO" ;
	private static final String PLAYBACK_REQUEST = "PLAYBACK_REQUEST" ;
	private static final String ID_REQUEST = "ID_REQUEST" ;
	private static final String ID = "ID" ;

	private int numID = 0 ;

	protected final static SoundManager soundManager = new SoundManager() ;

	private final HashMap<Integer, ActiveSound> sounds = new HashMap<Integer, ActiveSound>() ;
	private final ArrayList<ActiveSound> activeSounds = new ArrayList<ActiveSound>() ;
	private final ArrayList<ActiveSound> removeSounds = new ArrayList<ActiveSound>() ;

	private final EventMessenger messenger = new EventMessenger() ;
	private SourceGenerator sourceGenerator = null ;					// Used to create the Source from a Sound Buffer
	private AddEventInterface eventSystem = null ;					// Used to pass Events to designated EventSystem.

	public AudioSystem() {}

	public AudioSystem( final SourceGenerator _generator )
	{
		sourceGenerator = _generator ;
	}

	public AudioSystem( final AddEventInterface _eventSystem, final SourceGenerator _generator )
	{
		eventSystem = _eventSystem ;
		sourceGenerator = _generator ;
	}

	public void setSourceGenerator( final SourceGenerator _generator )
	{
		sourceGenerator = _generator ;
	}

	public void update( final float _dt )
	{
		updateEvents() ;
		updateActiveSounds() ;
		removeActiveSounds() ;
	}

	protected void updateEvents()
	{
		messenger.refreshEvents() ;
		final int eventSize = messenger.size() ;
		Event event = null ;

		for( int i = 0; i < eventSize; ++i )
		{
			event = messenger.getAt( i ) ;
			useEventInAudio( event ) ;
		}
	}

	protected void updateActiveSounds()
	{
		final int size = activeSounds.size() ;
		ActiveSound sound = null ;

		for( int i = 0; i < size; ++i )
		{
			sound = activeSounds.get( i ) ;
			// If ActiveSound has finished playing,
			// place in removal pool
			if( sound.update() == true )
			{
				removeSounds.add( sound ) ;
			}
		}
	}

	protected void useEventInAudio( final Event _event )
	{
		final Settings audio = ( Settings )_event.getVariable() ;
		final int type = audio.getInteger( REQUEST_TYPE, -1 ) ;

		switch( type )
		{
			case RequestType.CREATE_AUDIO :
			{
				creatAudio( audio ) ;
				break ;
			}
			case RequestType.MODIFY_EXISTING_AUDIO :
			{
				final int id = audio.getInteger( "ID", -1 ) ;
				if( sounds.containsKey( id ) == true )
				{
					modifyAudio( audio, sounds.get( id ) ) ;
				}
				break ;
			}
		}
	}

	protected void creatAudio( final Settings _audio )
	{
		final String file = _audio.getString( AUDIO_FILE, null ) ;
		if( file != null )
		{
			final ActiveSound sound = createActiveSound( file ) ;
			if( sound != null )
			{
				passIDToCallback( sound.id, _audio ) ;
				storeActiveSound( sound ) ;
				sound.source.play() ;
			}
			return ;
		}
	}

	/**
		Modify the settings of a running AudioSource.
	**/
	protected void modifyAudio( final Settings _settings, final ActiveSound _sound )
	{
		final AudioSource source = _sound.source ;
		final int type = _settings.getInteger( MODIFY_AUDIO, -1 ) ;
		switch( type )
		{
			case ModifyAudio.PLAY :
			{
				source.play() ;
				break ;
			}
			case ModifyAudio.STOP :
			{
				source.stop() ;
				break ;
			}
			case ModifyAudio.PAUSE :
			{
				source.pause() ;
				break ;
			}
			case ModifyAudio.LOOP_CONTINUOSLY :
			{
				source.playLoop() ;
				break ;
			}
			case ModifyAudio.LOOP_SET :
			{
				// Specify the amount of Loops to go through
				// before stopping.
				break ;
			}
			case ModifyAudio.ADD_PLAYBACK :
			{
				final PlaybackInterface playback = _settings.getObject( PLAYBACK_REQUEST, PlaybackInterface.class, null ) ;
				if( playback != null )
				{
					_sound.addPlayback( playback ) ;
				}
				break ;
			}
			case ModifyAudio.REMOVE_PLAYBACK :
			{
				final PlaybackInterface playback = _settings.getObject( PLAYBACK_REQUEST, PlaybackInterface.class, null ) ;
				if( playback != null )
				{
					_sound.removePlayback( playback ) ;
				}
				break ;
			}
		}
	}

	protected void storeActiveSound( final ActiveSound _sound )
	{
		activeSounds.add( _sound ) ;
		sounds.put( _sound.id, _sound ) ;
	}

	protected ActiveSound createActiveSound( final String _file )
	{
		//final ResourceManager resource = ResourceManager.getResourceManager() ;
		final Sound sound = ( Sound )soundManager.get( _file ) ;

		final AudioSource source = sourceGenerator.createAudioSource( sound ) ;
		if( source != null )
		{
			final ActiveSound active = new ActiveSound( numID, source, sound ) ;
			return active ;
		}

		return null ;
	}

	/**
		Pass the ActiveSound ID to the IDInterface provided.
		Currently called when ActiveSound is created
	**/
	protected void passIDToCallback( final int _id, final Settings _audio )
	{
		final IDInterface idInterface = _audio.getObject( ID_REQUEST, IDInterface.class, null ) ;
		if( idInterface != null )
		{
			idInterface.recievedID( _id ) ;
		}
	}

	/**
		Remove ActiveSounds from the removeSounds array.
	**/
	protected void removeActiveSounds()
	{
		// Remove Completed Sounds
		for( final ActiveSound remove : removeSounds )
		{
			remove.destroy() ;
			activeSounds.remove( remove ) ;
		}

		removeSounds.clear() ;
	}

	@Override
	public final void processEvent( final Event _event )
	{
		// Only add the Event to the message pool,
		// if an AudioSource can be created.
		if( sourceGenerator != null )
		{
			messenger.addEvent( _event ) ;
		}
	}

	@Override
	public final void passEvent( final Event _event )
	{
		if( eventSystem != null )
		{
			eventSystem.addEvent( _event ) ;
		}
	}

	@Override
	public final String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}

	private class ActiveSound
	{
		private ArrayList<PlaybackInterface> playbacks = new ArrayList<PlaybackInterface>() ;
		public AudioSource source = null ;
		public Sound sound = null ;
		public int id = -1 ;

		public ActiveSound( final int _id, final AudioSource _source, final Sound _sound )
		{
			id = _id ;
			source = _source ;
			sound = _sound ;
		}

		public void addPlayback( final PlaybackInterface _playback )
		{
			if( playbacks.contains( _playback ) == false )
			{
				playbacks.add( _playback ) ;
			}
		}

		public void removePlayback( final PlaybackInterface _playback )
		{
			if( playbacks.contains( _playback ) == true )
			{
				playbacks.remove( _playback ) ;
			}
		}
		
		public boolean update()
		{
			//System.out.println( source.getCurrentTime() ) ;
			return !( source.isPlaying() ) ;
		}

		public void destroy()
		{
			source.destroySource() ;
			sound.unregister() ;
		}
	}
}