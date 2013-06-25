package com.linxonline.mallet.audio ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.sound.Sound ;

public class ActiveSound
{
	private final ArrayList<PlaybackInterface> playbacks = new ArrayList<PlaybackInterface>() ;
	private final AudioSource source ;
	private final Sound sound ;
	public final int id ;

	public ActiveSound( final int _id, final AudioSource _source, final Sound _sound )
	{
		id = _id ;
		source = _source ;
		sound = _sound ;
	}

	public void addPlayback( final PlaybackInterface _playback )
	{
		if( playbacks.contains( _playback ) == false )
		{
			playbacks.add( _playback ) ;
		}
	}

	public void removePlayback( final PlaybackInterface _playback )
	{
		if( playbacks.contains( _playback ) == true )
		{
			playbacks.remove( _playback ) ;
		}
	}

	public void play()
	{
		source.play() ;
	}

	public void playLoop()
	{
		source.playLoop() ;
	}

	public void pause()
	{
		final int length = playbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			playbacks.get( i ).endPlayback( PlaybackInterface.PAUSE_PLAYBACK ) ;
		}

		source.pause() ;
	}

	public void stop()
	{
		final int length = playbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			playbacks.get( i ).endPlayback( PlaybackInterface.STOP_PLAYBACK ) ;
		}

		source.stop() ;
	}

	public boolean update()
	{
		updatePlaybackTimes() ;
		final boolean isPlaying = source.isPlaying() ;
		if( isPlaying == false )
		{
			finishPlaybacks() ;
		}

		return !isPlaying ;
	}

	private void updatePlaybackTimes()
	{
		final int length = playbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			playbacks.get( i ).updatePlayback( source.getCurrentTime() ) ;
		}
	}
	
	private void finishPlaybacks()
	{
		final int length = playbacks.size() ;
		for( int i = 0; i < length; ++i )
		{
			playbacks.get( i ).endPlayback( PlaybackInterface.FINISHED_PLAYBACK ) ;
		}
	}

	public void destroy()
	{
		source.destroySource() ;
		sound.unregister() ;
	}
}