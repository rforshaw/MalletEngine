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

	private State currentState = State.UNKNOWN ;
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
	public boolean play()
	{
		pause() ;
		track.setPlaybackHeadPosition( getBufferFrameOffset() < getBufferSize() ? getBufferFrameOffset() : 0 ) ;
		track.play() ;
		return true ;
	}

	@Override
	public boolean playLoop()
	{
		pause() ;
		track.setLoopPoints( getBufferFrameOffset(), getBufferSize(), -1 ) ;
		track.play() ;
		return true ;
	}

	@Override
	public boolean pause()
	{
		if( track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING )
		{
			track.pause() ;
		}
		return true ;
	}

	@Override
	public boolean stop()
	{
		if( track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING )
		{
			track.pause() ;
			track.flush() ;

			track.stop() ;
			track.setPlaybackHeadPosition( 0 ) ;
		}
		return true ;
	}

	@Override
	public boolean setPosition( final float _x, final float _y, final float _z )
	{
		return true ;
	}

	@Override
	public boolean setRelative( final boolean _relative )
	{
		return true ;
	}

	@Override
	public State getState()
	{
		final int state = track.getPlayState() ;
		final State temp = currentState ;

		switch( state )
		{
			default                           :
			{
				currentState = State.UNKNOWN ;
				break ;
			}
			case AudioTrack.PLAYSTATE_PLAYING :
			{
				currentState = State.PLAYING ;
				return currentState ;
			}
			case AudioTrack.PLAYSTATE_PAUSED  :
			{
				currentState = State.PAUSED ;
				break ;
			}
			case AudioTrack.PLAYSTATE_STOPPED :
			{
				currentState = State.STOPPED ;
				break ;
			}
		}

		// If the state hasn't changed from UNKNOWN, PAUSED, or STOPPED
		// return UNCHANGED, this means the AudioSystem doesn't have to
		// track the state.
		return ( temp != currentState ) ? currentState : State.UNCHANGED ;
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
