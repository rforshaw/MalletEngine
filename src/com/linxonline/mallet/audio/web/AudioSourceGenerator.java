package com.linxonline.mallet.audio.web ;

import java.util.Set ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLSourceElement ;
import org.teavm.jso.dom.events.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.io.ILoader ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.web.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.settings.Settings ;

public class AudioSourceGenerator implements AudioGenerator<WebSound>
{
	private static final Window window = Window.current() ;
	private static final HTMLDocument document = window.getDocument() ;

	private final SoundManager<WebSound> staticSoundManager = new SoundManager<WebSound>() ;
	private final AudioBuffer<WebSound> PLACEHOLDER = new AudioBuffer<WebSound>( null ) ;

	public AudioSourceGenerator() {}

	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	*/
	public boolean startGenerator()
	{
		final ILoader.ResourceLoader<AudioBuffer<WebSound>> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ILoader.ResourceDelegate<AudioBuffer<WebSound>>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public AudioBuffer load( final String _file )
			{
				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _file ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create AudioBufer: " + _file, Logger.Verbosity.NORMAL ) ;
					return null ;
				}

				final HTMLSourceElement source = file.getHTMLSource() ;
				final WebSound sound = new WebSound( source, 0 ) ;
				final AudioBuffer<WebSound> buffer = new AudioBuffer<WebSound>( sound ) ;

				window.getDocument().getBody().appendChild( source ) ;

				source.getStyle().setProperty( "display", "none" ) ;
				source.setSrc( _file ) ;
				source.addEventListener( "load", new EventListener()
				{
					@Override
					public void handleEvent( final Event _event )
					{
						
					}
				} ) ;

				return buffer ; 
			}
		} ) ;
	
		return false ;
	}

	public boolean shutdownGenerator()
	{
		clear() ;
		return true ;
	}

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final String _file, final StreamType _type )
	{
		final AudioBuffer<WebSound> sound = staticSoundManager.get( _file ) ;
		if( sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		return null ;
	}

	@Override
	public void clean( final Set<String> _activeKeys )
	{
		staticSoundManager.clean( _activeKeys ) ;
	}

	@Override
	public void clear()
	{
		staticSoundManager.clear() ;
	}
}
