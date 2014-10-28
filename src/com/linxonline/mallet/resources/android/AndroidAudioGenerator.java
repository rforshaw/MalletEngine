package com.linxonline.mallet.resources.android ;

import java.nio.* ;

import android.media.* ;
import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.formats.wav.* ;
import com.linxonline.mallet.resources.sound.* ;
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
		final byte[] soundBuffer = ByteReader.readBytes( _file ) ;
		if( soundBuffer != null )
		{
			return null ;
		}

		final int sampleRate = WAVHeader.getSampleRate( soundBuffer ) ;
		final short channels = WAVHeader.getChannels( soundBuffer ) ;
		final short sample = WAVHeader.getBitsPerSample( soundBuffer ) ;

		int channelType = AudioFormat.CHANNEL_CONFIGURATION_MONO ;
		if( channels > 1 )
		{
			channelType = AudioFormat.CHANNEL_CONFIGURATION_STEREO ;
		}

		int bitsPerSample = AudioFormat.ENCODING_PCM_8BIT ;
		if( sample >= 16 )
		{
			bitsPerSample = AudioFormat.ENCODING_PCM_16BIT ;
		}

		final AudioTrack track = new AudioTrack( AudioManager.STREAM_MUSIC,
												sampleRate,
												channelType,
												bitsPerSample,
												soundBuffer.length,
												AudioTrack.MODE_STATIC ) ;

		track.write( soundBuffer, 0, soundBuffer.length ) ;
		return new AudioBuffer<AndroidSound>( new AndroidSound( track, soundBuffer.length ) ) ;
	}

	/**
		Creates an AudioSource which can be to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final AudioBuffer<AndroidSound> _sound )
	{
		return null ;
	}
}