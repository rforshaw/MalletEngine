package com.linxonline.mallet.audio.desktop.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.util.SourceCallback ;

/**
	Provides the entry point in manipulating and playing 
	an audio-stream.
*/
public final class ALSASource implements ISource
{
	// By default OpenAL only allows for 256 sources.
	// If we allocate an AL source for each of our ISource's
	// we run the risk of running out quickly.
	// To resolve this matter we'll allocate all available AL
	// sources up front, we'll then share them to our ISource
	// when play(), playLoop(), pause() operation is called.
	// stop() will trigger the relinquishing of the AL source ID.
	private static final int MAX_SOURCE_NUM = 256 ;
	private static final int[] SOURCES = new int[MAX_SOURCE_NUM] ;					// Pool of all sources.
	private static final boolean[] SOURCE_USED = new boolean[MAX_SOURCE_NUM] ;		// true == source is currently used.
	private static int NEXT_SOURCE_INDEX = 0 ;										// Next source to use

	private final AL openAL ;
	private final ALSABuffer buffer ;

	private int sourceIndex = -1 ;

	private final int[] state = new int[1] ;			// Current State of the Audio: Playing, Pause, etc..
	private final int[] size = new int[1] ;
	private final int[] bits = new int[1] ;
	private final int[] channels = new int[1] ;
	private final int[] freq = new int[1] ;
	private final int[] bufferOffset = new int[1] ;

	private final Vector3 position = new Vector3() ;
	private boolean relative = false ;
	private int volume = 100 ;

	private State currentState = State.UNKNOWN ;
	private SourceCallback callback ;

	public ALSASource( final AL _openAL, final ALSABuffer _buffer )
	{
		openAL = _openAL ;
		buffer = _buffer ;
	}

	public static void initSourceIDs( final AL _openAL )
	{
		_openAL.alGenSources( SOURCES.length, SOURCES, 0 ) ;	
		final int error = _openAL.alGetError() ;
		if( error != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Source: " + ALSAGenerator.getALErrorString( error ) ) ;
			return ;
		}
	}

	/**
		Return the index of the next available source.
		NOTE: This will crash if no sources are available.
	*/
	public static int getNextIndex()
	{
		final int index = NEXT_SOURCE_INDEX % MAX_SOURCE_NUM ;
		if( SOURCE_USED[index] == true )
		{
			// We've reached a point where the next source is not
			// the next one in the list. We'll start from 0 and
			// brute force find an empty source.
			for( int i = 0; i < MAX_SOURCE_NUM; ++i )
			{
				if( SOURCE_USED[i] == false )
				{
					NEXT_SOURCE_INDEX = i + 1 ;
					SOURCE_USED[i] = true ;
					return i ;
				}
			}
			return -1 ;
		}

		++NEXT_SOURCE_INDEX ;
		SOURCE_USED[index] = true ;
		return index ;
	}

	/**
		Allow the source to claim an AL source ID.
	*/
	private static boolean claimSourceID( final ALSASource _source )
	{
		if( _source.sourceIndex >= 0 )
		{
			return true ;
		}

		_source.sourceIndex = getNextIndex() ;
		if( _source.sourceIndex <= -1 )
		{
			return false ;
		}

		final AL openAL = _source.openAL ;
		final int[] buffer = _source.buffer.getBufferID() ;
		final Vector3 position = _source.position ;
		final int index = _source.sourceIndex ;
		final float vol = _source.volume / 100.0f ;
		final boolean relative = _source.relative ;

		openAL.alSourcei( SOURCES[index], AL.AL_BUFFER, buffer[0] ) ;		// Bind Buffer to Source
		openAL.alSourcef( SOURCES[index], AL.AL_PITCH, 1.0f ) ;
		openAL.alSourcef( SOURCES[index], AL.AL_GAIN, vol ) ;
		openAL.alSource3f( SOURCES[index], AL.AL_POSITION, position.x, position.y, position.z ) ;
		openAL.alSourcei( SOURCES[index], AL.AL_SOURCE_RELATIVE, ( relative == true ) ? AL.AL_TRUE : AL.AL_FALSE ) ;

		// Not looping by default
		openAL.alSourcei( SOURCES[index], AL.AL_LOOPING, AL.AL_FALSE ) ;

		final int error = openAL.alGetError() ;
		if( error != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Configure Source: " + ALSAGenerator.getALErrorString( error ) ) ;
			return false ;
		}

		return true ;
	}

	private static void relinquishSourceID( final ALSASource _source )
	{
		final int index = _source.sourceIndex ;
		if( index <= -1 )
		{
			return ;
		}

		final AL openAL = _source.openAL ;

		openAL.alSourcei( SOURCES[index], AL.AL_BUFFER,  AL.AL_NONE ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to reset source" ) ;
		}

		SOURCE_USED[index] = false ;
		_source.sourceIndex = -1 ;
	}

	/**
		Return the number of currently used sources.
		Mostly used for debugging to determine if there
		is a 'source' leak.
	*/
	private static int getNumOfUsedSources()
	{
		int num = 0 ;
		for( int i = 0; i < SOURCE_USED.length; ++i )
		{
			num += ( SOURCE_USED[i] ) ? 1 : 0 ;
		}
		return num ;
	}

	@Override
	public boolean play()
	{
		if( claimSourceID( this ) == false )
		{
			return false ;
		}

		openAL.alSourcePlay( SOURCES[sourceIndex] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean playLoop()
	{
		if( claimSourceID( this ) == false )
		{
			return false ;
		}

		openAL.alSourcei( SOURCES[sourceIndex], AL.AL_LOOPING,  AL.AL_TRUE ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			return false ;
		}
		return play() ;
	}

	@Override
	public boolean pause()
	{
		if( claimSourceID( this ) == false )
		{
			return false ;
		}

		openAL.alSourcePause( SOURCES[sourceIndex] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean stop()
	{
		if( claimSourceID( this ) == false )
		{
			return false ;
		}

		openAL.alSourceStop( SOURCES[sourceIndex] ) ;
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;

		// We only want to apply if we have already claimed a source.
		if( sourceIndex >= 0 )
		{
			openAL.alSource3f( SOURCES[sourceIndex], AL.AL_POSITION, _x, _y, _z ) ;
		}
		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public boolean setRelative( final boolean _relative )
	{
		relative = _relative ;

		// We only want to apply if we have already claimed a source.
		if( sourceIndex >= 0 )
		{
			openAL.alSourcei( SOURCES[sourceIndex], AL.AL_SOURCE_RELATIVE, ( _relative == true ) ? AL.AL_TRUE : AL.AL_FALSE ) ;
		}

		return openAL.alGetError() == AL.AL_NO_ERROR ;
	}

	@Override
	public void setVolume( final int _volume )
	{
		volume = _volume ;

		// We only want to apply if we have already claimed a source.
		if( sourceIndex >= 0 )
		{
			final float vol = _volume / 100.0f ;
			openAL.alSourcef( SOURCES[sourceIndex], AL.AL_GAIN, vol ) ;
		}
	}

	@Override
	public State getState()
	{
		final State temp = currentState ;
		if( sourceIndex <= -1 )
		{
			currentState = State.UNKNOWN ;
			return ( temp != currentState ) ? currentState : State.UNCHANGED ;
		}

		openAL.alGetSourcei( SOURCES[sourceIndex], AL.AL_SOURCE_STATE, state, 0 ) ;
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
				relinquishSourceID( this ) ;
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
	public void destroy()
	{
		stop() ;
		relinquishSourceID( this ) ;
	}

	private int getBufferSize()
	{
		final int[] temp = buffer.getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_SIZE, size, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer size from source" ) ;
		}
		return size[0] ;
	}

	private int getBufferOffset()
	{
		openAL.alGetSourcei( SOURCES[sourceIndex], AL.AL_BYTE_OFFSET, bufferOffset, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer offset from source" ) ;
		}
		return bufferOffset[0] ;
	}

	private int getBufferBits()
	{
		final int[] temp = buffer.getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_BITS, bits, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get buffer bits from source" ) ;
		}
		return bits[0] ;
	}

	private int getBufferChannels()
	{
		final int[] temp = buffer.getBufferID() ;
		openAL.alGetBufferi( temp[0], AL.AL_CHANNELS, channels, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to get channels from source" ) ;
		}
		return channels[0] ;
	}

	private int getBufferFreq()
	{
		final int[] temp = buffer.getBufferID() ;
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
