package com.linxonline.mallet.audio ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.resources.sound.AudioBuffer ;
import com.linxonline.mallet.util.SourceCallback ;

public class AudioData<T extends AudioData> implements Audio<T>, Cacheable
{
	private final ArrayList<SourceCallback> callbacks = new ArrayList<SourceCallback>() ;

	private String file = null ;
	private StreamType type = null ;
	private AudioSource source = null ;

	public AudioData( final String _file, final StreamType _type )
	{
		file = _file ;
		type = _type ;
	}

	public void setSource( final AudioSource _source )
	{
		source = _source ;
	}

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
		}
	}

	public void removeCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == true )
		{
			callbacks.remove( _callback ) ;
			_callback.callbackRemoved() ;
		}
	}

	public void play()
	{
		source.play() ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).start() ;
		}
	}

	public void playLoop()
	{
		source.playLoop() ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).start() ;
		}
	}

	public void pause()
	{
		source.pause() ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).pause() ;
		}
	}

	public void stop()
	{
		source.stop() ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).stop() ;
		}
	}

	public String getFile()
	{
		return file ;
	}

	public StreamType getStreamType()
	{
		return type ;
	}

	public boolean isPlaying()
	{
		return source.isPlaying() ;
	}

	public boolean update( final float _dt )
	{
		updateCallbacks() ;
		final boolean isPlaying = source.isPlaying() ;
		if( isPlaying == false )
		{
			finished() ;
		}

		return !isPlaying ;
	}

	/**
		Update registered callbacks with the sources current time position.
	**/
	private void updateCallbacks()
	{
		final float sourceDT = source.getCurrentTime() ;
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).tick( sourceDT ) ;
		}
	}

	/**
		Called when Audio Source has finished playing.
	**/
	private void finished()
	{
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).finished() ;
		}
	}

	/**
		Call when Audio Source is to be cleaned up and resources unregistered.
	**/
	public void destroy()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).callbackRemoved() ;
		}

		callbacks.clear() ;
		source.destroySource() ;
	}

	@Override
	public void reset()
	{
		final int size = callbacks.size() ;
		for( int i = 0; i < size; ++i )
		{
			callbacks.get( i ).callbackRemoved() ;
		}
		callbacks.clear() ;

		if( source != null )
		{
			source.destroySource() ;
		}

		file   = null ;
		source = null ;
	}
}
