package com.linxonline.mallet.audio.web ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLSourceElement ;
import org.teavm.jso.dom.events.* ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.web.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;

public class AudioSourceGenerator implements AudioGenerator<WebSound>
{
	private static final Window window = Window.current() ;
	private static final HTMLDocument document = window.getDocument() ;

	private final SoundManager staticSoundManager = new SoundManager( this ) ;
	private final AudioBuffer<WebSound> PLACEHOLDER = new AudioBuffer<WebSound>( null ) ;

	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean startGenerator()
	{
		final ManagerInterface.ResourceLoader<AudioBuffer> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ManagerInterface.ResourceDelegate<AudioBuffer>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public AudioBuffer load( final String _file, final Settings _settings )
			{
				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _file ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create AudioBufer: " + _file, Logger.Verbosity.NORMAL ) ;
					return null ;
				}

				final HTMLSourceElement source = file.getHTMLSource() ;
				final WebSound sound = new WebSound( source ) ;
				final AudioBuffer<WebSound> buffer = new AudioBuffer<WebSound>( sound ) ;

				window.getDocument().getBody().appendChild( source ) ;

				source.getStyle().setProperty( "display", "none" ) ;
				source.setSrc( _file ) ;
				source.addEventListener( "load", new EventListener()
				{
					@Override
					public void handleEvent( Event _event )
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
		final AudioBuffer<WebSound> sound = ( AudioBuffer<WebSound> )staticSoundManager.get( _file ) ;
		if( sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		return null ;
	}

	public void clean()
	{
		staticSoundManager.clean() ;
	}

	public void clear()
	{
		staticSoundManager.clear() ;
	}
}