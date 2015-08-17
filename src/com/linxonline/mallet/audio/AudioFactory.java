package com.linxonline.mallet.audio ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.audio.ModifyAudio ;

public final class AudioFactory
{
	private AudioFactory() {}

	public static Event<Settings> createAudio( final String _file, final SourceCallback _callback )
	{
		return createAudio( _file, StreamType.STATIC, _callback ) ;
	}

	public static Event<Settings> createAudio( final String _file, final StreamType _type, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addObject( "REQUEST_TYPE", RequestType.CREATE_AUDIO ) ;
		if( _file != null )
		{
			audio.addString( "AUDIO_FILE", _file ) ;
		}

		if( _callback != null )
		{
			audio.addObject( "CALLBACK", _callback ) ;
		}

		if( _type != null )
		{
			audio.addObject( "STREAM_TYPE", _type ) ;
		}

		return new Event<Settings>( "AUDIO", audio ) ;
	}
	
	public static Event removeAudio( final int _id )
	{
		final Settings audio = new Settings() ;
		audio.addObject( "REQUEST_TYPE", RequestType.REMOVE_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		return new Event<Settings>( "AUDIO", audio ) ;
	}

	public static Event<Settings> createGarbageCollect()
	{
		final Settings audio = new Settings() ;
		audio.addObject( "REQUEST_TYPE", RequestType.GARBAGE_COLLECT_AUDIO ) ;
		return new Event<Settings>( "AUDIO", audio ) ;
	}

	public static Event<Settings> createAddCallback( final int _id, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addObject( "REQUEST_TYPE", RequestType.MODIFY_EXISTING_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		audio.addObject( "MODIFY_AUDIO", ModifyAudio.ADD_CALLBACK ) ;
		if( _callback != null ) { audio.addObject( "CALLBACK", _callback ) ; }
		return new Event( "AUDIO", audio ) ;
	}

	public static Event<Settings> createRemoveCallback( final int _id, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addObject( "REQUEST_TYPE", RequestType.MODIFY_EXISTING_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		audio.addObject( "MODIFY_AUDIO", ModifyAudio.REMOVE_CALLBACK ) ;
		if( _callback != null ) { audio.addObject( "CALLBACK", _callback ) ; }
		return new Event<Settings>( "AUDIO", audio ) ;
	}
}