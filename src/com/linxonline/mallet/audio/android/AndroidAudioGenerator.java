package com.linxonline.mallet.audio.android ;

import java.nio.* ;

import android.media.* ;
import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.audio.android.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.util.settings.Settings ;

public class AndroidAudioGenerator implements AudioGenerator<AndroidSound>
{
	private final SoundManager staticSoundManager = new SoundManager( this ) ;

	public boolean startGenerator()
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
				final byte[] buffer = ByteReader.readBytes( _file ) ;
				if( buffer == null )
				{
					return null ;
				}

				return new AudioBuffer<AndroidSound>( new AndroidSound( buffer ) ) ;
			}
		} ) ;

		return true ;
	}

	public boolean shutdownGenerator()
	{
		clear() ;
		return true ;
	}

	/**
		Creates an AudioSource which can be to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final String _file, final StreamType _type )
	{
		final AudioBuffer<AndroidSound> buffer = ( AudioBuffer<AndroidSound> )staticSoundManager.get( _file ) ;
		if( buffer == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		final AndroidSound sound = buffer.getBuffer() ;
		return new AndroidSource( sound.getBuffer() ) ;
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