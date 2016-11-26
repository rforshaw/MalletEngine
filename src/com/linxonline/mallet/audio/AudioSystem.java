package com.linxonline.mallet.audio ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.SourceCallback ;

// Play Sound
	// Set callback on Sound
	// Get Sound ID
// Modify running sound

public class AudioSystem
{
	private final ArrayList<AudioData> toAddAudio    = new ArrayList<AudioData>() ;
	private final ArrayList<AudioData> toRemoveAudio = new ArrayList<AudioData>() ;

	private final ArrayList<AudioData> active        = new ArrayList<AudioData>() ;
	private final ArrayList<AudioData> paused        = new ArrayList<AudioData>() ;			// Used when Game-State has been paused, move playing audio to here.

	private final EventController controller = new EventController() ;
	protected AudioGenerator sourceGenerator = null ;											// Used to create the Source from a Sound Buffer

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

		AudioAssist.setAssist( new AudioAssist.Assist()
		{
			@Override
			public Audio createAudio( final String _file, final StreamType _type )
			{
				return new AudioData( _file, _type ) ;
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
					final String path = audio.getFilePath() ;
					final StreamType type = audio.getStreamType() ;
				
					final AudioSource source = sourceGenerator.createAudioSource( path, type ) ;
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

		final int size = active.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AudioData audio = active.get( i ) ;
			final AudioSource source = audio.getSource() ;
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
			private final ArrayList<Audio> data = new ArrayList<Audio>() ;

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
}
