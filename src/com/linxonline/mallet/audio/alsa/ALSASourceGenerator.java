package com.linxonline.mallet.audio.alsa ;

import java.io.* ;
import java.nio.* ;

import com.jogamp.openal.* ;
import com.jogamp.openal.util.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;

public class ALSASourceGenerator implements AudioGenerator<ALSASound>
{
	private AL openAL = null ;

	public ALSASourceGenerator() {}

	public boolean startGenerator()
	{
		try
		{
			ALut.alutInit() ;
			openAL = ALFactory.getAL() ;
		}
		catch( ALException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}

		return true ;
	}
	
	public boolean shutdownGenerator()
	{
		try
		{
			ALut.alutExit() ;
		}
		catch( ALException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}

		return true ;
	}

	public AudioBuffer<ALSASound> createAudioBuffer( final String _file )
	{
		int[] format = new int[1] ;
		ByteBuffer[] data = new ByteBuffer[1] ;
		int[] size = new int[1] ;
		int[] freq = new int[1] ;
		int[] loop = new int[1] ;
		
		try
		{
			ALut.alutLoadWAVFile( _file, format, data, size, freq, loop ) ;
		}
		catch( ALException _ex )
		{
			System.out.println( "Error loading wav file: " + _file ) ;
			return null ;
		}
		
		int[] buffer = new int[1] ;
		openAL.alGenBuffers( 1, buffer, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Buffer" ) ;
			return null ;
		}
		
		openAL.alBufferData( buffer[0], format[0], data[0], size[0], freq[0] ) ;
		return new AudioBuffer<ALSASound>( new ALSASound( buffer, openAL ) ) ;
	}

	public AudioSource createAudioSource( final AudioBuffer<ALSASound> _sound )
	{
		if( _sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		final ALSASound alsaSound = _sound.getBuffer() ;	// Assumes Sound contains an ALSASound reference
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
