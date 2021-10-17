package com.linxonline.mallet.audio.desktop.alsa ;

import java.io.* ;
import java.nio.* ;
import java.util.Set ;

import com.jogamp.openal.* ;
import com.jogamp.openal.util.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.io.ILoader ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.util.settings.Settings ;

public class ALSASourceGenerator implements AudioGenerator
{
	private final SoundManager<ALSASound> staticSoundManager = new SoundManager<ALSASound>() ;
	private AL openAL = null ;

	public ALSASourceGenerator() {}

	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	*/
	@Override
	public boolean startGenerator()
	{
		try
		{
			ALut.alutInit() ;
			openAL = ALFactory.getAL() ;
			initStaticLoaders() ;

			openAL.alDistanceModel( AL.AL_LINEAR_DISTANCE_CLAMPED ) ;
		}
		catch( ALException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}

		return true ;
	}

	private void initStaticLoaders()
	{
		final ILoader.ResourceLoader<String, AudioBuffer<ALSASound>> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ILoader.ResourceDelegate<String, AudioBuffer<ALSASound>>()
		{
			@Override
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".wav", ".WAV" ) ;
			}

			@Override
			public AudioBuffer<ALSASound> load( final String _file )
			{
				final byte[] wav = ByteReader.readBytes( _file ) ;
				if( wav == null )
				{
					System.out.println( "Error loading file: " + _file ) ;
					return null ;
				}

				final AL openAL = ALSASourceGenerator.this.openAL ;
				final int[] buffer = new int[1] ;

				openAL.alGenBuffers( 1, buffer, 0 ) ;
				if( openAL.alGetError() != AL.AL_NO_ERROR )
				{
					System.out.println( "Failed to Generate Buffer." ) ;
					return null ;
				}

				final int size = wav.length - 44 ;								// - 44, exclude header from wav file length
				final int freq = WAVHeader.getSampleRate( wav ) ;
				final ByteBuffer data = ByteBuffer.wrap( wav, 44, size ) ;		// 44 offset, bypass wav header
				final int format = getALFormat( WAVHeader.getChannels( wav ),
												WAVHeader.getBitsPerSample( wav ) ) ;

				openAL.alBufferData( buffer[0], format, data, size, freq ) ;
				if( openAL.alGetError() != AL.AL_NO_ERROR )
				{
					System.out.println( "Failed to upload data to buffer." ) ;
					return null ;
				}

				return new AudioBuffer<ALSASound>( new ALSASound( buffer, size, openAL ) ) ;
			}

			private int getALFormat( final int _channels, final int _bitsPerSample )
			{
				switch( _channels )
				{
					case 1 :
					{
						switch( _bitsPerSample )
						{
							case 8  : return AL.AL_FORMAT_MONO8 ;
							case 16 : return AL.AL_FORMAT_MONO16 ;
						}
						break ;
					}
					case 2 :
					{
						switch( _bitsPerSample )
						{
							case 8  : return AL.AL_FORMAT_STEREO8 ;
							case 16 : return AL.AL_FORMAT_STEREO16 ;
						}
						break ;
					}
				}

				return -1 ;
			}
		} ) ;
	}

	@Override
	public boolean shutdownGenerator()
	{
		try
		{
			System.out.println( "Shutting down audio." ) ;
			clear() ;
			ALut.alutExit() ;
		}
		catch( ALException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}

		return true ;
	}

	@Override
	public void setListenerPosition( final float _x, final float _y, final float _z )
	{
		openAL.alListener3f( AL.AL_POSITION, _x, _y, _z ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to set position" ) ;
		}
	}

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	@Override
	public AudioSource createAudioSource( final String _file, final StreamType _type )
	{
		final AudioBuffer<ALSASound> sound = staticSoundManager.get( _file ) ;
		if( sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		final int[] source = new int[1] ;
		openAL.alGenSources( 1, source, 0 ) ;	
		int error = openAL.alGetError() ;
		if( error != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Source: " + getALErrorString( error ) ) ;
			return null ;
		}

		final int[] buffer = sound.getBuffer().getBufferID() ;
		openAL.alSourcei( source[0], AL.AL_BUFFER, buffer[0] ) ;		// Bind Buffer to Source
		openAL.alSourcef( source[0], AL.AL_PITCH, 1.0f ) ;
		openAL.alSourcef( source[0], AL.AL_GAIN, 1.0f ) ;
		openAL.alSource3f( source[0], AL.AL_POSITION, 0.0f, 0.0f, 0.0f ) ;

		// Not looping by default
		openAL.alSourcei( source[0], AL.AL_LOOPING, AL.AL_FALSE ) ;

		error = openAL.alGetError() ;
		if( error != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Configure Source: " + getALErrorString( error ) ) ;
			return null ;
		}

		return new ALSASource( openAL, sound, source ) ;
	}

	@Override
	public void clean( final Set<String> _activeKeys )
	{
		staticSoundManager.clean( _activeKeys ) ;
	}

	@Override
	public void clear()
	{
		staticSoundManager.clear() ;
	}

	private static String getALErrorString( final int _error )
	{
		switch( _error )
		{
			case AL.AL_NO_ERROR          : return "AL_NO_ERROR" ;
			case AL.AL_INVALID_NAME      : return "AL_INVALID_NAME" ;
			case AL.AL_INVALID_ENUM      : return "AL_INVALID_ENUM" ;
			case AL.AL_INVALID_VALUE     : return "AL_INVALID_VALUE" ;
			case AL.AL_INVALID_OPERATION : return "AL_INVALID_OPERATION" ;
			case AL.AL_OUT_OF_MEMORY     : return "AL_OUT_OF_MEMORY" ;
			default                        : return "No such error code";
		}
	}
}
