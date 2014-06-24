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
	private static final String[] EVENT_TYPES = { "AUDIO" } ;

	protected final static SoundManager soundManager = new SoundManager() ;
	protected AudioGenerator sourceGenerator = null ;					// Used to create the Source from a Sound Buffer
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
		soundManager.setAudioGenerator( _generator ) ;
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

	@Override
	protected void useEvent( final Event _event )
	{
		final Settings audio = ( Settings )_event.getVariable() ;
		final int type = audio.getInteger( "REQUEST_TYPE", -1 ) ;

		switch( type )
		{
			case RequestType.CREATE_AUDIO :
			{
				creatAudio( audio ) ;
				break ;
			}
			case RequestType.MODIFY_EXISTING_AUDIO :
			{
				final ActiveSound sound = getSource( audio.getInteger( "ID", -1 ) ) ;
				if( sound != null )
				{
					modifyAudio( audio, sound ) ;
				}
				break ;
			}
			case RequestType.REMOVE_AUDIO :
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
		if( file != null )
		{
			final ActiveSound sound = createActiveSound( file ) ;
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
		final int type = _settings.getInteger( "MODIFY_AUDIO", -1 ) ;
		switch( type )
		{
			case ModifyAudio.PLAY :
			{
				_sound.play() ;
				break ;
			}
			case ModifyAudio.STOP :
			{
				_sound.stop() ;
				break ;
			}
			case ModifyAudio.PAUSE :
			{
				_sound.pause() ;
				break ;
			}
			case ModifyAudio.LOOP_CONTINUOSLY :
			{
				_sound.playLoop() ;
				break ;
			}
			case ModifyAudio.LOOP_SET :
			{
				// Specify the amount of Loops to go through
				// before stopping.
				break ;
			}
			case ModifyAudio.ADD_CALLBACK :
			{
				final SourceCallback callback = _settings.getObject( "CALLBACK", null ) ;
				if( callback != null )
				{
					_sound.addCallback( callback ) ;
				}
				break ;
			}
			case ModifyAudio.REMOVE_CALLBACK :
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

	protected ActiveSound createActiveSound( final String _file )
	{
		final AudioBuffer sound = ( AudioBuffer )soundManager.get( _file ) ;
		final AudioSource source = sourceGenerator.createAudioSource( sound ) ;
		if( source != null )
		{
			// Increment numID, so next ActiveSource will have a unique ID
			final ActiveSound active = new ActiveSound( numID++, source, sound ) ;
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
	public final String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}
}