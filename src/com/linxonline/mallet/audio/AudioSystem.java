package com.linxonline.mallet.audio ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.SourceCallback ;

// Play Sound
	// Set callback on Sound
	// Get Sound ID
// Modify running sound

public class AudioSystem extends SystemRoot<ActiveSound>
{
	protected final ArrayList<ActiveSound> pausedSources = new ArrayList<ActiveSound>() ;		// Used when Audio System has been paused
	protected AudioGenerator sourceGenerator = null ;											// Used to create the Source from a Sound Buffer
	protected int numID = 0 ;

	public AudioSystem() {}

	public AudioSystem( final AudioGenerator _generator )
	{
		assert _generator != null ;
		sourceGenerator = _generator ;
	}

	public AudioSystem( final AddEventInterface _eventSystem, final AudioGenerator _generator )
	{
		assert _eventSystem != null ;
		assert _generator != null ;

		eventSystem = _eventSystem ;
		sourceGenerator = _generator ;
	}

	public void setAudioGenerator( final AudioGenerator _generator )
	{
		sourceGenerator = _generator ;
	}

	@Override
	protected void updateSource( final ActiveSound _source, final float _dt )
	{
		_source.update() ;
	}

	@Override
	protected void destroySource( final ActiveSound _source )
	{
		_source.destroy() ;
	}

	/**
		Continue playing sources that had previously been 
		playing before the Audio System was paused.
	*/
	public void resumeSystem()
	{
		for( final ActiveSound sound : pausedSources )
		{
			sound.play() ;
		}
		pausedSources.clear() ;
	}

	/**
		Pause currently playing sources, and store them 
		in a list to be resumed when Audio System is active again.
	*/
	public void pauseSystem()
	{
		for( final ActiveSound sound : activeSources )
		{
			if( sound.isPlaying() == true )
			{
				pausedSources.add( sound ) ;
				sound.pause() ;
			}
		}
	}

	@Override
	protected void useEvent( final Event<?> _event )
	{
		final Settings audio = ( Settings )_event.getVariable() ;
		final RequestType type = audio.getObject( "REQUEST_TYPE", null ) ;

		switch( type )
		{
			case GARBAGE_COLLECT_AUDIO : sourceGenerator.clean() ; break ;
			case CREATE_AUDIO          : creatAudio( audio ) ;  break ;
			case MODIFY_EXISTING_AUDIO :
			{
				final ActiveSound sound = getSource( audio.getInteger( "ID", -1 ) ) ;
				if( sound != null )
				{
					modifyAudio( audio, sound ) ;
				}
				break ;
			}
			case REMOVE_AUDIO :
			{
				final int id = audio.getInteger( "ID", -1 ) ;
				final ActiveSound sound = getSource( id ) ;
				if( sound != null )
				{
					removeSources.add( new RemoveSource( id, sound ) ) ;
				}
				break ;
			}
		}
	}

	protected void creatAudio( final Settings _audio )
	{
		final String file = _audio.getString( "AUDIO_FILE", null ) ;
		final StreamType type = _audio.getObject( "STREAM_TYPE", StreamType.STATIC ) ;
		if( file != null )
		{
			final ActiveSound sound = createActiveSound( file, type ) ;
			if( sound != null )
			{
				addCallbackToSound( sound, _audio ) ;
				storeSource( sound, sound.id ) ;
				sound.play() ;						// Assumed that sound will want to be played immediately.
			}
			return ;
		}
	}

	/**
		Modify the settings of a running AudioSource.
	**/
	protected void modifyAudio( final Settings _settings, final ActiveSound _sound )
	{
		final ModifyAudio type = _settings.getObject( "MODIFY_AUDIO", null ) ;
		switch( type )
		{
			case PLAY             : _sound.play() ;     break ;
			case STOP             : _sound.stop() ;     break ;
			case PAUSE            : _sound.pause() ;    break ;
			case LOOP_CONTINUOSLY : _sound.playLoop() ; break ;
			case LOOP_SET :
			{
				// Specify the amount of Loops to go through
				// before stopping.
				break ;
			}
			case ADD_CALLBACK :
			{
				final SourceCallback callback = _settings.getObject( "CALLBACK", null ) ;
				if( callback != null )
				{
					_sound.addCallback( callback ) ;
				}
				break ;
			}
			case REMOVE_CALLBACK :
			{
				final SourceCallback callback = _settings.getObject( "CALLBACK", null ) ;
				if( callback != null )
				{
					_sound.removeCallback( callback ) ;
				}
				break ;
			}
		}
	}

	protected ActiveSound createActiveSound( final String _file, final StreamType _type )
	{
		final AudioSource source = sourceGenerator.createAudioSource( _file, _type ) ;
		if( source != null )
		{
			// Increment numID, so next ActiveSource will have a unique ID
			final ActiveSound active = new ActiveSound( numID++, source ) ;
			return active ;
		}

		return null ;
	}

	/**
		Pass the ActiveSound ID to the IDInterface provided.
		Currently called when ActiveSound is created
	**/
	protected void addCallbackToSound( final ActiveSound _sound, final Settings _audio )
	{
		final SourceCallback callback = _audio.getObject( "CALLBACK", null ) ;
		if( callback != null )
		{
			_sound.addCallback( callback ) ;
		}
	}

	@Override
	public final void processEvent( final Event _event )
	{
		if( sourceGenerator != null )
		{
			super.processEvent( _event ) ;
		}
	}

	@Override
	public String getName()
	{
		return "Audio System" ;
	}

	@Override
	public final ArrayList<EventType> getWantedEventTypes()
	{
		final ArrayList<EventType> types = new ArrayList<EventType>() ;
		types.add( EventType.get( "AUDIO" ) ) ;
		return types ;
	}
}