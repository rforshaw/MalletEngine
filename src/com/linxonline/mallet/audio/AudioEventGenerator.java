package com.linxonline.mallet.audio ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Convience Factory that creates Events for the Developer 
	to use when making request to the AudioSystem. 
**/
public class AudioEventGenerator
{
	private static final String AUDIO = "AUDIO" ;

	public AudioEventGenerator() {}

	/**
		Used to inform the AudioSystem to create a new AudioSource and play it.
	**/
	public static final Event createAudioRequest( final String _audioFile, 
												final IDInterface _idInterface, 
												final PlaybackInterface _playback )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.CREATE_AUDIO ) ;
		audio.addString( "AUDIO_FILE", _audioFile ) ;
		audio.addObject( "ID_REQUEST", _idInterface ) ;
		audio.addObject( "PLAYBACK_REQUEST", _playback ) ;

		return new Event( AUDIO, audio ) ;
	}

	/**
		Used to inform the AudioSystem to modify an existing AudioSource.
		_id refers to the AudioSource identifier
		_modify refers to how it AudioSource should be modified, either:
		PLAY, LOOP, STOP, PAUSE, etc.
	**/
	public static final Event modifyAudioRequest( final int _id, final int _modify )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.MODIFY_EXISTING_AUDIO ) ;
		audio.addInteger( "MODIFY_AUDIO", _modify ) ;
		audio.addInteger( "ID", _id ) ;
		return new Event( AUDIO, audio ) ;
	}
}
