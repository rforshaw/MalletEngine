package com.linxonline.mallet.resources.android ;

import java.nio.* ;

import android.media.* ;
import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.audio.android.* ;
import com.linxonline.mallet.audio.* ;

public class AndroidAudioGenerator implements AudioGenerator<AndroidSound>
{
	public boolean startGenerator()
	{
		return true ;
	}

	public boolean shutdownGenerator()
	{
		return true ;
	}

	public AudioBuffer<AndroidSound> createAudioBuffer( final String _file )
	{
		System.out.println( "Creating Audio Buffer for: " + _file ) ;
		final byte[] buffer = ByteReader.readBytes( _file ) ;
		if( buffer == null )
		{
			return null ;
		}

		return new AudioBuffer<AndroidSound>( new AndroidSound( buffer ) ) ;
	}

	/**
		Creates an AudioSource which can be to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final AudioBuffer<AndroidSound> _sound )
	{
		if( _sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		final AndroidSound sound = _sound.getBuffer() ;
		return new AndroidSource( sound.getBuffer() ) ;
	}
}