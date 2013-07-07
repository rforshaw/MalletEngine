package com.linxonline.mallet.audio ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.audio.ModifyAudio ;

public class AudioFactory
{
	public AudioFactory() {}

	public static Event createAudio( final String _file, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.CREATE_AUDIO ) ;
		if( _file != null ) { audio.addString( "AUDIO_FILE", _file ) ; }
		if( _callback != null ) { audio.addObject( "CALLBACK", _callback ) ; }
		return new Event( "AUDIO", audio ) ;
	}

	public static Event removeAudio( final int _id )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.REMOVE_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		return new Event( "AUDIO", audio ) ;
	}
	
	public static Event createAddCallback( final int _id, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.MODIFY_EXISTING_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		audio.addInteger( "MODIFY_AUDIO", ModifyAudio.ADD_CALLBACK ) ;
		if( _callback != null ) { audio.addObject( "CALLBACK", _callback ) ; }
		return new Event( "AUDIO", audio ) ;
	}

	public static Event createRemoveCallback( final int _id, final SourceCallback _callback )
	{
		final Settings audio = new Settings() ;
		audio.addInteger( "REQUEST_TYPE", RequestType.MODIFY_EXISTING_AUDIO ) ;
		audio.addInteger( "ID", _id ) ;
		audio.addInteger( "MODIFY_AUDIO", ModifyAudio.REMOVE_CALLBACK ) ;
		if( _callback != null ) { audio.addObject( "CALLBACK", _callback ) ; }
		return new Event( "AUDIO", audio ) ;
	}
}