package com.linxonline.mallet.audio ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.SourceCallback ;

public class AudioAssist
{
	public static Event<AudioDelegateCallback> constructAudioDelegate( final AudioDelegateCallback _callback )
	{
		return new Event<AudioDelegateCallback>( "AUDIO_DELEGATE", _callback ) ;
	}

	public static Audio createAudio( final String _file, final StreamType _type )
	{
		return new AudioData( _file, _type ) ;
	}

	public static Audio play( final Audio _audio )
	{
		( ( AudioData )_audio ).play() ;
		return _audio ;
	}

	public static Audio stop( final Audio _audio )
	{
		( ( AudioData )_audio ).stop() ;
		return _audio ;
	}

	public static Audio pause( final Audio _audio )
	{
		( ( AudioData )_audio ).pause() ;
		return _audio ;
	}

	public static Audio addCallback( final Audio _audio, final SourceCallback _callback )
	{
		( ( AudioData )_audio ).addCallback( _callback ) ;
		return _audio ;
	}

	public static Audio removeCallback( final Audio _audio, final SourceCallback _callback )
	{
		( ( AudioData )_audio ).removeCallback( _callback ) ;
		return _audio ;
	}
}