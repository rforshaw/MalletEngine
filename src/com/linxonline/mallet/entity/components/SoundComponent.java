package com.linxonline.mallet.entity.components ;

import java.util.Collection ;
import java.util.List ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.audio.AudioDelegateCallback ;
import com.linxonline.mallet.audio.AudioDelegate ;
import com.linxonline.mallet.audio.AudioAssist ;
import com.linxonline.mallet.audio.Audio ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public class SoundComponent extends EventComponent implements SourceCallback
{
	private final HashMap<String, Audio> sounds = new HashMap<String, Audio>() ;

	private String defaultAudio = null ;					// Name of the default audio, used as a fallback if all else fails.
	private Audio currentAudio  = null ;					// Name of the current audio that is playing

	private AudioDelegate delegate            = null ;
	private Component.ReadyCallback toDestroy = null ;
	private SourceCallback callback           = null ;

	public SoundComponent()
	{
		this( "SOUND" ) ;
	}

	public SoundComponent( final String _name )
	{
		super( _name ) ;
	}

	/**
		Add the audio to the Audio sub-system.
		The audio will not be played until it has been requested.
	*/
	public void addAudio( final String _name, final Audio _audio )
	{
		sounds.put( _name, _audio ) ;
		AudioAssist.amendCallback( _audio, this ) ;
		if( delegate != null )
		{
			delegate.addAudio( _audio ) ;
		}
	}

	public void removeAudio( final String _name )
	{
		final Audio audio = sounds.remove( _name ) ;
		if( delegate != null )
		{
			delegate.removeAudio( audio ) ;
		}
	}

	public Audio getAudio( final String _name )
	{
		return sounds.get( _name ) ;
	}

	public void setDefaultAudio( final String _name )
	{
		defaultAudio = _name ;
	}

	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		if( delegate != null )
		{
			delegate.shutdown() ;
			delegate = null ;
		}

		toDestroy = _callback ;
		super.readyToDestroy( _callback ) ;
	}

	public void playAudio( final String _name, final SourceCallback _callback )
	{
		playAudio( _name ) ;
		callback = _callback ;
	}

	public void playAudio( final String _name )
	{
		if( toDestroy != null )
		{
			return ;
		}

		stopAudio() ;
		final Audio audio = sounds.get( _name ) ;
		if( audio != null )
		{
			AudioAssist.play( audio ) ;
			currentAudio = audio ;
		}
	}

	/**
		Remove the current audio from the Audio System.
	**/
	public void stopAudio()
	{
		if( toDestroy != null )
		{
			return ;
		}

		if( currentAudio != null )
		{
			AudioAssist.stop( currentAudio ) ;
		}
	}

	public void callbackRemoved() {}

	@Override
	public void start()
	{
		if( callback != null )
		{
			callback.start() ;
		}
	}

	@Override
	public void pause()
	{
		if( callback != null )
		{
			callback.pause() ;
		}
	}

	@Override
	public void stop()
	{
		if( callback != null )
		{
			callback.stop() ;
		}
	}

	@Override
	public void finished()
	{
		if( callback != null )
		{
			callback.finished() ;
		}
	}

	@Override
	public void tick( final float _dt )
	{
		if( callback != null )
		{
			callback.tick( _dt ) ;
		}
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		_events.add( AudioAssist.constructAudioDelegate( new AudioDelegateCallback()
		{
			public void callback( final AudioDelegate _delegate )
			{
				delegate = _delegate ;
				final Collection<Audio> audio = sounds.values() ;
				for( final Audio a : audio )
				{
					delegate.addAudio( a ) ;
				}
			}
		} ) ) ;
		super.passInitialEvents( _events ) ;
	}
}
