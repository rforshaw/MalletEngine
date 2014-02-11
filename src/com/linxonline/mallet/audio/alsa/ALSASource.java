package com.linxonline.mallet.audio.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.audio.* ;

/**
	Provides the entry point in manipulating and playing 
	an audio-stream.
*/
public class ALSASource implements AudioSource
{
	private AL openAL = null ;
	private int[] buffer = null ;
	private int[] source = null ;

	private int[] state = new int[1] ;			// Current State of the Audio: Playing, Pause, etc..
	private int[] size = new int[1] ;
	private int[] bits = new int[1] ;
	private int[] channels = new int[1] ;
	private int[] freq = new int[1] ;
	private int[] bufferOffset = new int[1] ;

	public ALSASource( final AL _openAL, final int[] _buffer, final int[] _source )
	{
		openAL = _openAL ;
		buffer = _buffer ;
		source = _source ;
	}

	public void play()
	{
		openAL.alSourcePlay( source[0] ) ;
	}

	public void playLoop()
	{
		openAL.alSourcei( source[0], AL.AL_LOOPING,  AL.AL_TRUE ) ;
		openAL.alSourcePlay( source[0] ) ;
	}

	public void pause()
	{
		openAL.alSourcePause( source[0] ) ;
	}

	public void stop()
	{
		openAL.alSourceStop( source[0] ) ;
	}

	public boolean isPlaying()
	{
		openAL.alGetSourcei( source[0], AL.AL_SOURCE_STATE, state, 0 ) ;
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

	/**
		Destory OpenAL source.
		Doesn't destroy OpenAL buffer.
	**/
	public void destroySource()
	{
		stop() ;
		openAL.alSourcei( source[0], AL.AL_BUFFER,  AL.AL_NONE ) ;
		openAL.alDeleteSources( 1, source, 0 ) ;
	}

	private int getBufferSize()
	{
		openAL.alGetBufferi( buffer[0], AL.AL_SIZE, size, 0 ) ;
		return size[0] ;
	}

	private int getBufferOffset()
	{
		openAL.alGetSourcei( source[0], AL.AL_BYTE_OFFSET, bufferOffset, 0 ) ;
		return bufferOffset[0] ;
	}

	private int getBufferBits()
	{
		openAL.alGetBufferi( buffer[0], AL.AL_BITS, bits, 0 ) ;
		return bits[0] ;
	}

	private int getBufferChannels()
	{
		openAL.alGetBufferi( buffer[0], AL.AL_CHANNELS, channels, 0 ) ;
		return channels[0] ;
	}

	private int getBufferFreq()
	{
		openAL.alGetBufferi( buffer[0], AL.AL_FREQUENCY, freq, 0 ) ;
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
