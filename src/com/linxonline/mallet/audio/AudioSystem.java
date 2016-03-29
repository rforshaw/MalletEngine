package com.linxonline.mallet.audio ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.sound.* ;
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

	private final ArrayList<AudioData> audio         = new ArrayList<AudioData>() ;
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
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( toAddAudio.isEmpty() == false )
		{
			if( sourceGenerator != null )
			{
				for( final AudioData a : toAddAudio )
				{
					final AudioSource source = sourceGenerator.createAudioSource( a.getFile(), a.getStreamType() ) ;
					if( source != null )
					{
						a.setSource( source ) ;
						a.play() ;
					}
				}
				toAddAudio.clear() ;
			}
		}

		if( toRemoveAudio.isEmpty() == false )
		{
			for( final AudioData a : toRemoveAudio )
			{
				audio.remove( a ) ;
				a.reset() ;
			}
			toRemoveAudio.clear() ;
		}

		final int size = audio.size() ;
		for( int i = 0; i < size; i++ )
		{
			audio.get( i ).update( _dt ) ;
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
		for( final AudioData a : paused )
		{
			a.play() ;
		}
		paused.clear() ;
	}

	/**
		Pause currently playing sources, and store them 
		in a list to be resumed when Audio System is active again.
	*/
	public void pauseSystem()
	{
		for( final AudioData a : audio )
		{
			if( a.isPlaying() == true )
			{
				paused.add( a ) ;
				a.pause() ;
			}
		}
	}

	public void clear()
	{
		toAddAudio.clear() ;		// Never added not hooked in
		toRemoveAudio.clear() ;		// Will be removed from audio anyway

		for( final AudioData a : audio )
		{
			a.reset() ;
		}
		audio.clear() ;
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
			private final ArrayList<AudioData> data = new ArrayList<AudioData>() ;

			@Override
			public void addAudio( final Audio _audio )
			{
				if( _audio != null && _audio instanceof AudioData )
				{
					if( data.contains( _audio ) == false )
					{
						data.add( ( AudioData )_audio ) ;
						toAddAudio.add( ( AudioData )_audio ) ;
					}
				}
			}

			@Override
			public void removeAudio( final Audio _audio )
			{
				if( _audio != null && _audio instanceof AudioData )
				{
					data.remove( ( AudioData )_audio ) ;
					toRemoveAudio.add( ( AudioData )_audio ) ;
				}
			}

			@Override
			public void start() {}

			@Override
			public void shutdown()
			{
				for( final AudioData anim : data  )
				{
					toRemoveAudio.add( anim ) ;
				}
				data.clear() ;
			}
		} ;
	}
}