package com.linxonline.mallet.audio.android ;

import android.media.* ;

import com.linxonline.mallet.io.formats.wav.WAVHeader ;
import com.linxonline.mallet.audio.android.AndroidSound ; 
import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.util.SourceCallback ;

/**
	Provides the entry point in manipulating and playing 
	an audio-stream.
*/
public class AndroidSource implements AudioSource
{
	private final WAVHeader header ;
	private final AudioTrack track ;
	private final int bufferLength ;

	private SourceCallback callback ;

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

	@Override
	public void play()
	{
		pause() ;
		track.setPlaybackHeadPosition( getBufferFrameOffset() < getBufferSize() ? getBufferFrameOffset() : 0 ) ;
		track.play() ;
	}

	@Override
	public void playLoop()
	{
		pause() ;
		track.setLoopPoints( getBufferFrameOffset(), getBufferSize(), -1 ) ;
		track.play() ;
	}

	@Override
	public void pause()
	{
		if( track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING )
		{
			track.pause() ;
		}
	}

	@Override
	public void stop()
	{
		if( track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING )
		{
			track.pause() ;
			track.flush() ;

			track.stop() ;
			track.setPlaybackHeadPosition( 0 ) ;
		}
	}

	@Override
	public void setPosition( final float _x, final float _y, final float _z )
	{

	}

	@Override
	public void setRelative( final boolean _relative )
	{

	}

	@Override
	public State getState()
	{
		switch( track.getPlayState() )
		{
			default                           : return State.UNKNOWN ;
			case AudioTrack.PLAYSTATE_PLAYING : return State.PLAYING ; // && getCurrentTime() < getDuration()
			case AudioTrack.PLAYSTATE_PAUSED  : return State.PAUSED ;
			case AudioTrack.PLAYSTATE_STOPPED : return State.STOPPED ;
		}
	}

	@Override
	public float getCurrentTime()
	{
		final float offset = getBufferFrameOffset() ;
		final float channels = getBufferChannels() ;
		final float freq = getBufferFreq() ;

		return offset / channels / freq ;
	}

	@Override
	public float getDuration()
	{
		final float s = getBufferSize() ;
		final float c = getBufferChannels() ;
		final float f = getBufferFreq() ;

		return s / c / f / getBytesPerSample() ;
	}

	@Override
	public void setVolume( final int _volume )
	{
		final float volume = _volume / 100.0f ;
		track.setStereoVolume( volume, volume ) ;
	}

	@Override
	public void setCallback( final SourceCallback _callback )
	{
		callback = _callback ;
	}

	@Override
	public SourceCallback getCallback()
	{
		return callback ;
	}

	@Override
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

	private int getBufferFrameOffset()
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
	
	private int getBytesPerSample()
	{
		return header.bitPerSample / 8 ;
	}
}
