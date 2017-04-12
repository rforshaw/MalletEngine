package com.linxonline.mallet.audio.android ;

import java.nio.* ;
import java.util.Set ;

import android.media.* ;
import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.io.ILoader ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.util.settings.Settings ;

public class AndroidAudioGenerator implements AudioGenerator<AndroidSound>
{
	private final SoundManager staticSoundManager = new SoundManager() ;

	public boolean startGenerator()
	{
		final ILoader.ResourceLoader<AudioBuffer<AndroidSound>> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ILoader.ResourceDelegate<AudioBuffer<AndroidSound>>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".wav", ".WAV" ) ;
			}

			public AudioBuffer<AndroidSound> load( final String _file )
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

	@Override
	public void clean( final Set<String> _activeKeys )
	{
		staticSoundManager.clean( _activeKeys ) ;
	}

	public void clear()
	{
		staticSoundManager.clear() ;
	}
}
