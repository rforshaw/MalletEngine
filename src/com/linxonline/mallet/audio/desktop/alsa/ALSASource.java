package com.linxonline.mallet.audio.desktop.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.audio.* ;

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

	public ALSASource( final AL _openAL, final AudioBuffer<ALSASound> _buffer, final int[] _source )
	{
		openAL = _openAL ;
		buffer = _buffer ;
		source = _source ;
	}

	public void play()
	{
		openAL.alSourcePlay( source[0] ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to play source" ) ;
		}
	}

	public void playLoop()
	{
		openAL.alSourcei( source[0], AL.AL_LOOPING,  AL.AL_TRUE ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to configure source" ) ;
		}
		play() ;
	}

	public void pause()
	{
		openAL.alSourcePause( source[0] ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to pause source" ) ;
		}
	}

	public void stop()
	{
		openAL.alSourceStop( source[0] ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to stop source" ) ;
		}
	}

	public boolean isPlaying()
	{
		openAL.alGetSourcei( source[0], AL.AL_SOURCE_STATE, state, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to determine playable state source" ) ;
			return false ;
		}
		return ( state[0] == AL.AL_PLAYING ) ;
	}

	public float getCurrentTime()
	{
		return getCurrentBufferTime() ;
	}

	public float getDuration()
	{
		return getBufferTime() ;
	}

	public void setVolume( final int _volume )
	{
		final float vol = _volume / 100.0f ;
		openAL.alSourcef( source[0], AL.AL_GAIN, vol ) ;
	}

	/**
		Destory OpenAL source.
		Doesn't destroy OpenAL buffer.
	**/
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

		buffer.unregister() ;
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
		final int offset = getBufferOffset() ;
		final int bits = ( getBufferBits() / 8 ) ; 	// Change to bytes
		final int channels = getBufferChannels() ;
		final float freq = getBufferFreq() ;
		return ( float )( ( offset / channels / bits ) / freq ) ;
	}

	private float getBufferTime()
	{
		final int s = getBufferSize() ;
		final int b = ( getBufferBits() / 8 ) ; 	// Change to bytes
		final int c = getBufferChannels() ;
		final float f = getBufferFreq() ;

		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			return -1.0f ;
		}

		return ( float )( ( s / c / b ) / f ) ;
	}
}
