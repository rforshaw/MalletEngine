package com.linxonline.mallet.audio ;

import java.util.List ;

import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.SourceCallback ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

// Play Sound
	// Set callback on Sound
	// Get Sound ID
// Modify running sound

public class AudioSystem
{
	private final List<Volume> volumes = MalletList.<Volume>newList() ;
	private float masterVolume = 1.0f ;

	private final List<AudioData> toAddAudio    = MalletList.<AudioData>newList() ;
	private final List<AudioData> toRemoveAudio = MalletList.<AudioData>newList() ;

	private final List<AudioData> active        = MalletList.<AudioData>newList() ;
	private final List<AudioData> paused        = MalletList.<AudioData>newList() ;			// Used when Game-State has been paused, move playing audio to here.

	private final EventController controller = new EventController() ;
	protected AudioGenerator sourceGenerator = null ;										// Used to create the Source from a Sound Buffer

	public AudioSystem()
	{
		this( null ) ;
	}

	public AudioSystem( final AudioGenerator _generator )
	{
		sourceGenerator = _generator ;
		controller.addEventProcessor( new EventProcessor<AudioDelegateCallback>( "AUDIO_DELEGATE", "AUDIO_DELEGATE" )
		{
			public void processEvent( final Event<AudioDelegateCallback> _event )
			{
				final AudioDelegateCallback callback = _event.getVariable() ;
				callback.callback( constructAudioDelegate() ) ;
			}
		} ) ;

		controller.addEventProcessor( new EventProcessor( "AUDIO_CLEAN", "AUDIO_CLEAN" )
		{
			public void processEvent( final Event _event )
			{
				if( sourceGenerator != null )
				{
					sourceGenerator.clean() ;
				}
			}
		} ) ;

		controller.addEventProcessor( new EventProcessor<Volume>( "CHANGE_VOLUME", "CHANGE_VOLUME" )
		{
			public void processEvent( final Event<Volume> _event )
			{
				volumes.add( new Volume( _event.getVariable() ) ) ;
			}
		} ) ;

		AudioAssist.setAssist( new AudioAssist.Assist()
		{
			@Override
			public Audio createAudio( final String _file, final StreamType _type, final Category.Channel _channel )
			{
				return new AudioData( _file, _type, new Category( _channel ) ) ;
			}

			@Override
			public Audio amendCallback( final Audio _audio, final SourceCallback _callback )
			{
				final AudioData audio = ( AudioData )_audio ;
				audio.amendCallback( _callback ) ;
				return _audio ;
			}

			@Override
			public Audio play( Audio _audio )
			{
				final AudioData audio = ( AudioData )_audio ;
				audio.play = true ;
				audio.dirty = true ;
				return _audio ;
			}

			@Override
			public Audio stop( Audio _audio )
			{
				final AudioData audio = ( AudioData )_audio ;
				audio.stop = true ;
				audio.dirty = true ;
				return _audio ;
			}

			@Override
			public Audio pause( Audio _audio )
			{
				final AudioData audio = ( AudioData )_audio ;
				audio.play = false ;
				audio.dirty = true ;
				return _audio ;
			}
		} ) ;

		initGlobalConfig() ;
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( toAddAudio.isEmpty() == false )
		{
			if( sourceGenerator != null )
			{
				for( final AudioData audio : toAddAudio )
				{
					final AudioSource source = loadSource( audio ) ;
					if( source != null )
					{
						audio.setSource( source ) ;
						active.add( audio ) ;
					}
				}
				toAddAudio.clear() ;
			}
		}

		if( toRemoveAudio.isEmpty() == false )
		{
			for( final AudioData audio : toRemoveAudio )
			{
				final AudioSource source = audio.getSource() ;
				if( source != null )
				{
					source.stop() ;
					source.destroySource() ;
				}
				active.remove( audio ) ;
			}
			toRemoveAudio.clear() ;
		}

		{
			final int size = active.size() ;
			for( int i = 0; i < size; i++ )
			{
				final AudioData audio = active.get( i ) ;
				AudioSource source = audio.getSource() ;
				if( source == null )
				{
					source = loadSource( audio ) ;
					if( source == null )
					{
						// The source has yet to be loaded or 
						// doesn't exist, we'll keep trying to load.
						break ;
					}

					audio.dirty = true ;
					audio.setSource( source ) ;
					active.add( audio ) ;
				}

				final SourceCallback callback = audio.getCallback() ;

				if( audio.dirty == true )
				{
					// We only want to play/pause/stop if 
					// it has been flagged as dirty.
					// Dirty signifies the state has been changed.
					audio.dirty = false ;
					if( audio.stop == true )
					{
						audio.stop = false ;

						source.stop() ;
						callback.stop() ;
					}

					final boolean isPlaying = source.isPlaying() ;

					if( audio.play == true )
					{
						// Start playing if currently not playing.
						if( isPlaying == false )
						{
							source.play() ;
							callback.start() ;
						}
					}
					else if( audio.play == false )
					{
						// Pause if current playing
						if( isPlaying == true )
						{
							source.pause() ;
							callback.pause() ;
						}
					}
				}

				if( source.isPlaying() == true )
				{
					// Only call tick if the source is playing.
					callback.tick( source.getCurrentTime() ) ;
				}
				else if( audio.play == true )
				{
					// If the source is not playing but play 
					// is true then we expect it to be playing.
					// If not the source has finished and we should inform 
					// the user.
					source.stop() ;
					audio.play = false ;
					callback.finished() ;
				}
			}
		}

		if( volumes.isEmpty() == false )
		{
			final int size = volumes.size() ;
			for( int i = 0; i < size; i++ )
			{
				updateVolume( volumes.get( i ) ) ;
				
			}
			volumes.clear() ;
		}
	}

	private void updateVolume( final Volume _volume )
	{
		final Category category = _volume.getCategory() ;
		if( category.getChannel() == Category.Channel.MASTER )
		{
			masterVolume = _volume.getVolume() / 100.0f ;
			updateMasterVolume( masterVolume ) ;
			return ;
		}

		final int size = active.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AudioData audio = active.get( i ) ;
			final Volume audioVolume = audio.getVolume() ;

			if( category.equals( audioVolume.getCategory() ) )
			{
				audioVolume.setVolume( _volume.getVolume() ) ;

				final AudioSource source = audio.getSource() ;
				source.setVolume( ( int )( audioVolume.getVolume() * masterVolume ) ) ;
			}
		}
	}

	private void updateMasterVolume( final float _masterVolume )
	{
		final int size = active.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AudioData audio = active.get( i ) ;
			final Volume audioVolume = audio.getVolume() ;

			final AudioSource source = audio.getSource() ;
			source.setVolume( ( int )( audioVolume.getVolume() * _masterVolume ) ) ;
		}
	}
	
	public void setAudioGenerator( final AudioGenerator _generator )
	{
		sourceGenerator = _generator ;
	}

	/**
		Continue playing sources that had previously been 
		playing before the Audio System was paused.
	*/
	public void resumeSystem()
	{
		for( final AudioData audio : paused )
		{
			final AudioSource source = audio.getSource() ;
			final SourceCallback callback = audio.getCallback() ;

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
		for( final AudioData audio : active )
		{
			final AudioSource source = audio.getSource() ;
			final SourceCallback callback = audio.getCallback() ;

			if( source.isPlaying() == true )
			{
				paused.add( audio ) ;
				source.pause() ;
			}
		}
	}

	public void clear()
	{
		toAddAudio.clear() ;		// Never added not hooked in
		toRemoveAudio.clear() ;		// Will be removed from audio anyway

		/*for( final AudioData audio : active )
		{
			final AudioSource source = audio.getSource() ;
			final SourceCallback callback = audio.getCallback() ;

			if( source.isPlaying() == true )
			{
				paused.add( audio ) ;
				source.pause() ;
			}
		}*/
		active.clear() ;
	}

	public EventController getEventController()
	{
		return controller ;
	}

	public String getName()
	{
		return "Audio System" ;
	}

	protected AudioDelegate constructAudioDelegate()
	{
		return new AudioDelegate()
		{
			private final List<Audio> data = MalletList.<Audio>newList() ;

			@Override
			public void addAudio( final Audio _audio )
			{
				if( _audio instanceof AudioData )
				{
					if( data.contains( _audio ) == false )
					{
						data.add( _audio ) ;
						toAddAudio.add( ( AudioData )_audio ) ;
					}
				}
			}

			@Override
			public void removeAudio( final Audio _audio )
			{
				if( _audio != null && _audio instanceof AudioData )
				{
					data.remove( _audio ) ;
					toRemoveAudio.add( ( AudioData )_audio ) ;
				}
			}

			@Override
			public void shutdown()
			{
				for( final Audio audio : data  )
				{
					toRemoveAudio.add( ( AudioData )audio ) ;
				}
				data.clear() ;
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
			volumes.add( new Volume( new Category( _channel ), _volume ) ) ;
		}
	}

	private AudioSource loadSource( final AudioData _audio )
	{
		final String path = _audio.getFilePath() ;
		final StreamType type = _audio.getStreamType() ;
		return sourceGenerator.createAudioSource( path, type ) ;
	}
}
