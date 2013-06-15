package com.linxonline.mallet.audio.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.sound.* ;

public class ALSASourceGenerator implements SourceGenerator
{
	public ALSASourceGenerator() {}

	public AudioSource createAudioSource( final Sound _sound )
	{
		if( _sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		final ALSASound alsaSound = _sound.getSoundBuffer( ALSASound.class ) ;	// Assumes Sound contains an ALSASound reference
		final AL openAL = alsaSound.getOpenAL() ;
		final int[] buffer = alsaSound.getBufferID() ;

		int[] source = new int[1] ;
		openAL.alGenSources( 1, source, 0 ) ;	
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Source" ) ;
			return null ;
		}

		openAL.alSourcei( source[0], AL.AL_BUFFER, buffer[0] ) ;		// Bind Buffer to Source
		openAL.alSourcef( source[0], AL.AL_PITCH, 1.0f ) ;
		openAL.alSourcef( source[0], AL.AL_GAIN, 1.0f ) ;
		openAL.alSource3f( source[0], AL.AL_POSITION, 0.0f, 0.0f, 0.0f ) ;

		// Not looping by default
		openAL.alSourcei( source[0], AL.AL_LOOPING, AL.AL_FALSE ) ;

		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Configure Source" ) ;
			return null ;
		}

		return new ALSASource( openAL, buffer, source ) ;
	}
}
