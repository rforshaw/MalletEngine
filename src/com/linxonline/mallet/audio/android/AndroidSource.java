package com.linxonline.mallet.audio.android ;

import android.media.* ;

import com.linxonline.mallet.io.formats.wav.WAVHeader ;
import com.linxonline.mallet.audio.android.AndroidSound ; 
import com.linxonline.mallet.audio.* ;

/**
	Provides the entry point in manipulating and playing 
	an audio-stream.
*/
public class AndroidSource implements AudioSource
{
	private final WAVHeader header ;
	private final AudioTrack track ;
	private final int bufferLength ;

	public AndroidSource( final byte[] _buffer )
	{
		header = WAVHeader.getHeader( _buffer ) ;
		bufferLength = _buffer.length - header.startPoint ;

		int channelType = AudioFormat.CHANNEL_CONFIGURATION_MONO ;
		if( header.channels > 1 )
		{
			channelType = AudioFormat.CHANNEL_CONFIGURATION_STEREO ;
		}

		int bitsPerSample = AudioFormat.ENCODING_PCM_8BIT ;
		if( header.bitPerSample >= 16 )
		{
			bitsPerSample = AudioFormat.ENCODING_PCM_16BIT ;
		}

		track = new AudioTrack( AudioManager.STREAM_MUSIC,
								header.samplerate,
								channelType,
								bitsPerSample,
								getBufferSize(),
								AudioTrack.MODE_STATIC ) ;
		track.write( _buffer, header.startPoint, getBufferSize() ) ;
		track.setPlaybackHeadPosition( 0 ) ;
	}

	public void play()
	{
		pause() ;
		track.setPlaybackHeadPosition( getBufferOffset() < getBufferSize() ? getBufferOffset() : 0 ) ;
		track.play() ;
	}

	public void playLoop()
	{
		pause() ;
		track.setLoopPoints( getBufferOffset(), getBufferSize(), -1 ) ;
		track.play() ;
	}

	public void pause()
	{
		if( isPlaying() == true )
		{
			track.pause() ;
		}
	}

	public void stop()
	{
		if( isPlaying() == true )
		{
			track.stop() ;
			track.setPlaybackHeadPosition( 0 ) ;
		}
	}

	public boolean isPlaying()
	{
		return track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING ;
	}

	public float getCurrentTime()
	{
		final float offset = getBufferOffset() ;
		final float channels = getBufferChannels() ;
		final float freq = getBufferFreq() ;

		return offset / channels / freq ;
	}

	public float getDuration()
	{
		final float s = getBufferSize() ;
		final float c = getBufferChannels() ;
		final float f = getBufferFreq() ;

		return s / c / f ;
	}

	public void destroySource()
	{
		track.stop() ;
		track.flush() ;
		track.release() ;
	}

	private int getBufferSize()
	{
		return bufferLength ;
	}

	private int getBufferOffset()
	{
		return track.getPlaybackHeadPosition() ;
	}

	private int getBufferBits()
	{
		return getBufferSize() * 8 ;
	}

	private int getBufferChannels()
	{
		return header.channels ;
	}

	private int getBufferFreq()
	{
		return header.samplerate ;
	}
}
