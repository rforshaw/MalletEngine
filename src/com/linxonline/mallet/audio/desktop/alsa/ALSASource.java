package com.linxonline.mallet.audio.desktop.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.util.SourceCallback ;

/**
	Provides the entry point in manipulating and playing 
	an audio-stream.
*/
public class ALSASource implements AudioSource
{
	private final AL openAL ;
	private final AudioBuffer<ALSASound> buffer ;
	private final int[] source ;

	private final int[] state = new int[1] ;			// Current State of the Audio: Playing, Pause, etc..
	private final int[] size = new int[1] ;
	private final int[] bits = new int[1] ;
	private final int[] channels = new int[1] ;
	private final int[] freq = new int[1] ;
	private final int[] bufferOffset = new int[1] ;

	private State currentState = State.UNKNOWN ;
	private SourceCallback callback ;

	public ALSASource( final AL _openAL, final AudioBuffer<ALSASound> _buffer, final int[] _source )
	{
		openAL = _openAL ;
		buffer = _buffer ;
		source = _source ;
	}

	@Override
	public boolean play()
	{
		openAL.alSourcePlay( source[0] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean playLoop()
	{
		openAL.alSourcei( source[0], AL.AL_LOOPING,  AL.AL_TRUE ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			return false ;
		}
		return play() ;
	}

	@Override
	public boolean pause()
	{
		openAL.alSourcePause( source[0] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean stop()
	{
		openAL.alSourceStop( source[0] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean setPosition( final float _x, final float _y, final float _z )
	{
		openAL.alSource3f( source[0], AL.AL_POSITION, _x, _y, _z ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean setRelative( final boolean _relative )
	{
		openAL.alSourcei( source[0], AL.AL_SOURCE_RELATIVE, ( _relative == true ) ? AL.AL_TRUE : AL.AL_FALSE ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public State getState()
	{
		openAL.alGetSourcei( source[0], AL.AL_SOURCE_STATE, state, 0 ) ;
		final State temp = currentState ;

		switch( state[0] )
		{
			default            :
			{
				currentState = State.UNKNOWN ;
				break ;
			}
			case AL.AL_PLAYING :
			{
				currentState = State.PLAYING ;
				return currentState ;
			}
			case AL.AL_PAUSED  :
			{
				currentState = State.PAUSED ;
				break ;
			}
			case AL.AL_STOPPED :
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
		return getCurrentBufferTime() ;
	}

	@Override
	public float getDuration()
	{
		return getBufferTime() ;
	}

	@Override
	public void setVolume( final int _volume )
	{
		final float vol = _volume / 100.0f ;
		openAL.alSourcef( source[0], AL.AL_GAIN, vol ) ;
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

	/**
		Destory OpenAL source.
		Doesn't destroy OpenAL buffer.
	**/
	@Override
	public void destroySource()
	{
		stop() ;
		openAL.alSourcei( source[0], AL.AL_BUFFER,  AL.AL_NONE ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to reset source" ) ;
		}

		openAL.alDeleteSources( 1, source, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to delete source" ) ;
		}
	}

	private int getBufferSize()
	{
		final int[] temp = buffer.getBuffer().getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_SIZE, size, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer size from source" ) ;
		}
		return size[0] ;
	}

	private int getBufferOffset()
	{
		openAL.alGetSourcei( source[0], AL.AL_BYTE_OFFSET, bufferOffset, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer offset from source" ) ;
		}
		return bufferOffset[0] ;
	}

	private int getBufferBits()
	{
		final int[] temp = buffer.getBuffer().getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_BITS, bits, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer bits from source" ) ;
		}
		return bits[0] ;
	}

	private int getBufferChannels()
	{
		final int[] temp = buffer.getBuffer().getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_CHANNELS, channels, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get channels from source" ) ;
		}
		return channels[0] ;
	}

	private int getBufferFreq()
	{
		final int[] temp = buffer.getBuffer().getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_FREQUENCY, freq, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get frequency from source" ) ;
		}
		return freq[0] ;
	}

	private float getCurrentBufferTime()
	{
		final float offset = getBufferOffset() ;
		final float bits = ( getBufferBits() / ( float )8 ) ; 	// Change to bytes
		final float channels = getBufferChannels() ;
		final float freq = getBufferFreq() ;
		return ( offset / channels / bits ) / freq ;
	}

	private float getBufferTime()
	{
		final float s = getBufferSize() ;
		final float b = ( getBufferBits() / ( float )8 ) ; 	// Change to bytes
		final float c = getBufferChannels() ;
		final float f = getBufferFreq() ;

		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			return -1.0f ;
		}

		return ( s / c / b ) / f ;
	}
}
