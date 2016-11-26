package com.linxonline.mallet.audio ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public final class AudioAssist
{
	private static final Event AUDIO_CLEAN = new Event( "AUDIO_CLEAN", null ) ;

	private static Assist assist ;
	
	private AudioAssist() {}

	/**
		Called by the active audio system.
		If swapping audio-systems all previous Audio objects 
		will become invalid.
	*/
	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	/**
		Request an AudioDelegate from the active audio system.
		The AudioDelegate allows the user to add/remove Audio objects
		from being played.

		An AudioDelegate is not required for constructing an Audio object, 
		but is required for playing it.
	*/
	public static Event<AudioDelegateCallback> constructAudioDelegate( final AudioDelegateCallback _callback )
	{
		return new Event<AudioDelegateCallback>( "AUDIO_DELEGATE", _callback ) ;
	}

	/**
		Request the active audio system to clean-up any 
		unused resources it may still be referencing.
	*/
	public static Event constructAudioClean()
	{
		return AUDIO_CLEAN ;
	}

	public static Audio createAudio( final String _file, final StreamType _type )
	{
		return assist.createAudio( _file, _type ) ;
	}

	public static Audio amendCallback( final Audio _audio, final SourceCallback _callback )
	{
		return assist.amendCallback( _audio, _callback ) ;
	}

	public static Audio play( Audio _audio )
	{
		return assist.play( _audio ) ;
	}

	public static Audio stop( Audio _audio )
	{
		return assist.stop( _audio ) ;
	}

	public static Audio pause( Audio _audio )
	{
		return assist.pause( _audio ) ;
	}

	/**
		Required to be implemented by the active audio-system.
	*/
	public interface Assist
	{
		public Audio createAudio( final String _file, final StreamType _type ) ;
		public Audio amendCallback( final Audio _audio, final SourceCallback _callback ) ;

		public Audio play( Audio _audio ) ;
		public Audio stop( Audio _audio ) ;
		public Audio pause( Audio _audio ) ;
	}
}
