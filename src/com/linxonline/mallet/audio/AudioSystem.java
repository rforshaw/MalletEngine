package com.linxonline.mallet.audio ;

import java.util.List ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Set ;
import java.util.HashSet ;

import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

// Play Sound
	// Set callback on Sound
	// Get Sound ID
// Modify running sound

public final class AudioSystem
{
	private final Map<Category, Volume> channelTable = MalletMap.<Category, Volume>newMap() ;
	private float masterVolume = 1.0f ;

	private final Map<Emitter, ISource> sources = MalletMap.<Emitter, ISource>newMap() ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>( 200 ) ;

	private final List<ISource> active = MalletList.<ISource>newList() ;
	private final List<ISource> paused = MalletList.<ISource>newList() ;			// Used when Game-State has been paused, move playing audio to here.

	private final EventController controller = new EventController() ;
	protected IGenerator generator = null ;										// Used to create the Source from a Sound Buffer

	public AudioSystem()
	{
		this( null ) ;
	}

	public AudioSystem( final IGenerator _generator )
	{
		generator = _generator ;
		controller.addProcessor( "AUDIO_DELEGATE", ( final AudioDelegateCallback _callback ) ->
		{
			_callback.callback( constructAudioDelegate() ) ;
		} ) ;

		controller.addProcessor( "AUDIO_CLEAN", ( final Object _null ) ->
		{
			if( generator == null )
			{
				return ;
			}

			final Set<String> activeKeys = new HashSet<String>() ;
			final Set<Emitter> emitters = sources.keySet() ;
			for( final Emitter emitter : emitters )
			{
				activeKeys.add( emitter.getFilepath() ) ;
			}

			generator.clean( activeKeys ) ;
		} ) ;

		controller.addProcessor( "CHANGE_VOLUME", ( final Volume _volume ) ->
		{
			updateVolume( _volume ) ;
		} ) ;

		AudioAssist.setAssist( new Assist() ) ;

		initGlobalConfig() ;
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( generator == null )
		{
			Logger.println( "No source-generator set for audio system.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		updateExecutions() ;

		for( final ISource source : active )
		{
			final SourceCallback callback = source.getCallback() ;

			switch( source.getState() )
			{
				case UNCHANGED :
				{
					break ;
				}
				case PAUSED    :
				{
					callback.pause() ;
					break ;
				}
				case STOPPED   :
				{
					callback.finished() ;
					// Once the source state is flagged as STOPPED
					// remove it from the active list.
					invokeLater( () -> active.remove( source ) ) ;
					break ;
				}
				case PLAYING   :
				{
					callback.tick( source.getCurrentTime() ) ;
					break ;
				}
			}
		}
	}

	private void updateExecutions()
	{
		executions.update() ;
		final List<Runnable> runnables = executions.getCurrentData() ;
		if( runnables.isEmpty() )
		{
			return ;
		}

		final int size = runnables.size() ;
		for( int i = 0; i < size; i++ )
		{
			runnables.get( i ).run() ;
		}
		runnables.clear() ;
	}

	private void updateVolume( final Volume _volume )
	{
		final Category category = _volume.getCategory() ;
		channelTable.put( category, new Volume( category, _volume.getVolume() ) ) ;

		if( category.getChannel() == Category.Channel.MASTER )
		{
			masterVolume = _volume.getVolume() / 100.0f ;
			for( final ISource source : active )
			{
				// If master is being updated all 
				// sources must be changed.
				setVolumeOnSource( _volume, source ) ;
			}
			return ;
		}

		/*for( final AudioData audio : active )
		{
			if( category.equals( audio.getCategory() ) )
			{
				// Only change the volumes for sources 
				// that match out category
				setVolumeOnSource( _volume, audio.getSource() ) ;
			}
		}*/
	}

	private void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	private void setVolumeOnSource( final Volume _volume, final ISource _source )
	{
		switch( _volume.getCategory().getChannel() )
		{
			default    : _source.setRelative( false ) ; break ;
			case MUSIC : _source.setRelative( true ) ; break ;
		}

		_source.setVolume( ( int )( _volume.getVolume() * masterVolume ) ) ;
	}

	public void setGenerator( final IGenerator _generator )
	{
		generator = _generator ;
	}

	/**
		Continue playing sources that had previously been 
		playing before the Audio System was paused.
	*/
	public void resumeSystem()
	{
		for( final ISource source : paused )
		{
			source.play() ;
		}
		paused.clear() ;
	}

	/**
		Pause currently playing sources, and store them 
		in a list to be resumed when Audio System is active again.
	*/
	public void pauseSystem()
	{
		for( final ISource source : active )
		{
			switch( source.getState() )
			{
				case PLAYING :
				{
					paused.add( source ) ;
					source.pause() ;
					break ;
				}
			}
		}
	}

	public void clear()
	{
		for( final ISource source : active )
		{
			source.destroy() ;
		}
		active.clear() ;
		sources.clear() ;
	}

	public AudioSystem.Assist createAudioAssist()
	{
		return new Assist() ;
	}

	public EventController getEventController()
	{
		return controller ;
	}

	public String getName()
	{
		return "Audio System" ;
	}

	private AudioDelegate constructAudioDelegate()
	{
		return new AudioDelegate()
		{
			private final List<Emitter> emitters = MalletList.<Emitter>newList() ;
			private final Vector3 position = new Vector3() ;

			private boolean disable = false ;

			@Override
			public Emitter add( final Emitter _emitter )
			{
				if( disable == true )
				{
					return _emitter ;
				}

				AudioSystem.this.invokeLater( () ->
				{
					if( emitters.contains( _emitter ) == false )
					{
						emitters.add( _emitter ) ;
						final ISource source = loadSource( _emitter ) ;
						if( source == null )
						{
							Logger.println( "Failed to generate source for emitter.", Logger.Verbosity.NORMAL ) ;
							return ;
						}

						sources.put( _emitter, source ) ;
						update( _emitter ) ;
					}
				} ) ;

				return _emitter ;
			}

			@Override
			public Emitter remove( final Emitter _emitter )
			{
				if( disable == true )
				{
					return _emitter ;
				}

				AudioSystem.this.invokeLater( () ->
				{
					emitters.remove( _emitter ) ;
					final ISource source = sources.remove( _emitter ) ;
					if( source == null )
					{
						Logger.println( "Failed to find source for emitter.", Logger.Verbosity.NORMAL ) ;
						return ;
					}

					active.remove( source ) ;

					source.stop() ;
					source.destroy() ;
				} ) ;

				return _emitter ;
			}

			@Override
			public Emitter play( final Emitter _emitter )
			{
				AudioSystem.this.invokeLater( () ->
				{
					final ISource source = sources.get( _emitter ) ;
					if( source == null )
					{
						Logger.println( "Attempting to play emitter with no source.", Logger.Verbosity.NORMAL ) ;
						return ;
					}

					if( source.play() == false )
					{
						Logger.println( "Failed to play: " + _emitter.getFilepath(), Logger.Verbosity.NORMAL ) ;
						return ;
					}
				
					if( active.contains( source ) == false )
					{
						active.add( source ) ;
					}
				} ) ;

				return _emitter ;
			}

			@Override
			public Emitter stop( final Emitter _emitter )
			{
				AudioSystem.this.invokeLater( () ->
				{
					final ISource source = sources.get( _emitter ) ;
					if( source == null )
					{
						Logger.println( "Attempting to stop emitter with no source.", Logger.Verbosity.NORMAL ) ;
						return ;
					}

					if( source.stop() == false )
					{
						Logger.println( "Failed to stop: " + _emitter.getFilepath(), Logger.Verbosity.NORMAL ) ;
					}

					// Don't remove the source from the active pool
					// As it will get removed on the next getState() call.
				} ) ;

				return _emitter ;
			}

			@Override
			public Emitter pause( final Emitter _emitter )
			{
				AudioSystem.this.invokeLater( () ->
				{
					final ISource source = sources.get( _emitter ) ;
					if( source == null )
					{
						Logger.println( "Attempting to pause emitter with no source.", Logger.Verbosity.NORMAL ) ;
						return ;
					}

					if( source.pause() == false )
					{
						Logger.println( "Failed to pause: " + _emitter.getFilepath(), Logger.Verbosity.NORMAL ) ;
					}

					if( active.contains( source ) == false )
					{
						active.add( source ) ;
					}
				} ) ;

				return _emitter ;
			}

			@Override
			public Emitter update( final Emitter _emitter )
			{
				final ISource source = sources.get( _emitter ) ;
				if( source == null )
				{
					Logger.println( "Attempting to update emitter with no source.", Logger.Verbosity.NORMAL ) ;
					return _emitter ;
				}

				_emitter.getPosition( position ) ;
				if( source.setPosition( position.x, position.y, position.z ) == false )
				{
					Logger.println( "Failed to set position: " + _emitter.getFilepath(), Logger.Verbosity.NORMAL ) ;
				}

				source.setCallback( _emitter.getCallback() ) ;
				return _emitter ;
			}

			@Override
			public void shutdown()
			{
				if( disable == true )
				{
					return ;
				}

				AudioSystem.this.invokeLater( () ->
				{
					for( final Emitter emitter : emitters  )
					{
						final ISource source = sources.remove( emitter ) ;
						if( source == null )
						{
							Logger.println( "Failed to find source for emitter.", Logger.Verbosity.NORMAL ) ;
							continue ;
						}

						source.destroy() ;
					}
					emitters.clear() ;
				} ) ;

				disable = true ;
			}
		} ;
	}

	private void initGlobalConfig()
	{
		createVolume( Category.Channel.MASTER, GlobalConfig.getInteger( "MASTERVOLUME", 100 ) ) ;
		createVolume( Category.Channel.MUSIC,  GlobalConfig.getInteger( "MUSICVOLUME", 100 ) ) ;
		createVolume( Category.Channel.VOCAL,  GlobalConfig.getInteger( "VOCALVOLUME", 100 ) ) ;
		createVolume( Category.Channel.EFFECT, GlobalConfig.getInteger( "EFFECTVOLUME", 100 ) ) ;

		final Notify<String> notify = new Notify<String>()
		{
			@Override
			public void inform( final String _data )
			{
				switch( _data )
				{
					case "MASTERVOLUME" : createVolume( Category.Channel.MASTER, GlobalConfig.getInteger( _data, 100 ) ) ; break ;
					case "MUSICVOLUME"  : createVolume( Category.Channel.MUSIC, GlobalConfig.getInteger( _data, 100 ) ) ;  break ;
					case "VOCALVOLUME"  : createVolume( Category.Channel.VOCAL, GlobalConfig.getInteger( _data, 100 ) ) ;  break ;
					case "EFFECTVOLUME" : createVolume( Category.Channel.EFFECT, GlobalConfig.getInteger( _data, 100 ) ) ; break ;
					default             :
					{
						Logger.println( "Registered to " + _data  + " that is not handled.", Logger.Verbosity.MINOR ) ;
						break ;
					}
				}
			}
		} ;

		GlobalConfig.addNotify( "MASTERVOLUME", notify ) ;
		GlobalConfig.addNotify( "MUSICVOLUME", notify ) ;
		GlobalConfig.addNotify( "VOCALVOLUME", notify ) ;
		GlobalConfig.addNotify( "EFFECTVOLUME", notify ) ;
	}

	private void createVolume( final Category.Channel _channel, final int _volume )
	{
		if( _volume >= 0 )
		{
			updateVolume( new Volume( new Category( _channel ), _volume ) ) ;
		}
	}

	private ISource loadSource( final Emitter _emitter )
	{
		final String path = _emitter.getFilepath() ;
		final StreamType type = _emitter.getStreamType() ;
		final ISource source = generator.create( path, type ) ;

		final Volume volume = channelTable.get( _emitter.getCategory() ) ;
		setVolumeOnSource( volume, source ) ;

		return source ;
	}

	private class Assist implements AudioAssist.Assist
	{
		@Override
		public void setListenerPosition( final float _x, final float _y, final float _z )
		{
			AudioSystem.this.invokeLater( () ->
			{
				generator.setListenerPosition( _x, _y, _z ) ;
			} ) ;
		}
	}
}
