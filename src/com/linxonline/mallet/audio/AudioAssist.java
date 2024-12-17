package com.linxonline.mallet.audio ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public final class AudioAssist
{
	private static final Event<Object> AUDIO_CLEAN = Event.create( "AUDIO_CLEAN" ) ;

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
	public static void getAudioDelegate( final AudioDelegateCallback _callback )
	{
		assist.getAudioDelegate( _callback ) ;
	}

	/**
		Request the active audio system to clean-up any 
		unused resources it may still be referencing.
	*/
	public static void cleanAudio()
	{
		assist.cleanAudio() ;
	}

	public static void changeVolume( final Category.Channel _channel, final int _volume )
	{
		assist.changeVolume( new Volume( new Category( _channel ), _volume ) ) ;
	}

	public static void setListenerPosition( final float _x, final float _y, final float _z )
	{
		assist.setListenerPosition( _x, _y, _z ) ;
	}

	/**
		Required to be implemented by the active audio-system.
	*/
	public interface Assist
	{
		public void getAudioDelegate( final AudioDelegateCallback _callback ) ;

		public void changeVolume( final Volume _volume ) ;

		public void setListenerPosition( final float _x, final float _y, final float _z ) ;

		public void cleanAudio() ;
	}
}
