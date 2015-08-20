package com.linxonline.mallet.audio.desktop.alsa ;

import java.io.* ;
import java.nio.* ;

import com.jogamp.openal.* ;
import com.jogamp.openal.util.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.util.settings.Settings ;

public class ALSASourceGenerator implements AudioGenerator<ALSASound>
{
	private final SoundManager staticSoundManager = new SoundManager( this ) ;
	private AL openAL = null ;

	public ALSASourceGenerator() {}

	public boolean startGenerator()
	{
		try
		{
			ALut.alutInit() ;
			openAL = ALFactory.getAL() ;
			initStaticLoaders() ;
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
		final ManagerInterface.ResourceLoader<AudioBuffer> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ManagerInterface.ResourceDelegate<AudioBuffer>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".wav", ".WAV" ) ;
			}

			public AudioBuffer load( final String _file, final Settings _settings )
			{
				final byte[] wav = ByteReader.readBytes( _file ) ;
				if( wav == null )
				{
					System.out.println( "Error loading file: " + _file ) ;
					return null ;
				}

				final AL openAL = ALSASourceGenerator.this.openAL ;
				int[] buffer = new int[1] ;

				openAL.alGenBuffers( 1, buffer, 0 ) ;
				if( openAL.alGetError() != AL.AL_NO_ERROR )
				{
					System.out.println( "Failed to Generate Buffer" ) ;
					return null ;
				}

				final int size = wav.length - 44 ;								// - 44, exclude header from wav file length
				final int freq = WAVHeader.getSampleRate( wav ) ;
				final ByteBuffer data = ByteBuffer.wrap( wav, 44, size ) ;		// 44 offset, bypass wav header
				final int format = getALFormat( WAVHeader.getChannels( wav ),
												WAVHeader.getBitsPerSample( wav ) ) ;

				openAL.alBufferData( buffer[0], format, data, size, freq ) ;
				return new AudioBuffer<ALSASound>( new ALSASound( buffer, openAL ) ) ;
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

	public boolean shutdownGenerator()
	{
		try
		{
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

	public AudioSource createAudioSource( final String _file, final StreamType _type )
	{
		final AudioBuffer<ALSASound> sound = ( AudioBuffer<ALSASound> )staticSoundManager.get( _file ) ;
		if( sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		int[] source = new int[1] ;
		openAL.alGenSources( 1, source, 0 ) ;	
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Source" ) ;
			return null ;
		}

		final int[] buffer = sound.getBuffer().getBufferID() ;
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

		return new ALSASource( openAL, sound, source ) ;
	}

	public void clean()
	{
		staticSoundManager.clean() ;
	}

	public void clear()
	{
		staticSoundManager.clear() ;
	}
}
