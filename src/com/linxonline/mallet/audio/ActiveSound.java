package com.linxonline.mallet.audio ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.sound.Sound ;
import com.linxonline.mallet.util.SourceCallback ;

public class ActiveSound
{
	private final ArrayList<SourceCallback> callbacks = new ArrayList<SourceCallback>() ;
	private final AudioSource source ;
	private final Sound sound ;
	public final int id ;

	public ActiveSound( final int _id, final AudioSource _source, final Sound _sound )
	{
		id = _id ;
		source = _source ;
		sound = _sound ;
	}

	public void addCallback( final SourceCallback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
			_callback.recieveID( id ) ;
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

	public boolean update()
	{
		updateCallbacks() ;
		final boolean isPlaying = source.isPlaying() ;
		if( isPlaying == false )
		{
			finished() ;
		}

		return !isPlaying ;
	}

	private void updateCallbacks()
	{
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).update( source.getCurrentTime() ) ;
		}
	}
	
	private void finished()
	{
		final int length = callbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			callbacks.get( i ).finished() ;
		}
	}

	public void destroy()
	{
		callbacks.clear() ;
		source.destroySource() ;
		sound.unregister() ;
	}
}