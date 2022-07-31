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

public class AudioSourceGenerator implements IGenerator
{
	private static final Window window = Window.current() ;
	private static final HTMLDocument document = window.getDocument() ;

	private final SoundManager<WebSound> staticSoundManager = new SoundManager<WebSound>() ;

	public AudioSourceGenerator() {}

	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	*/
	@Override
	public boolean start()
	{
		final ILoader.ResourceLoader<String, WebSound> loader = staticSoundManager.getResourceLoader() ;
		loader.add( new ILoader.ResourceDelegate<String, WebSound>()
		{
			@Override
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			@Override
			public WebSound load( final String _file )
			{
				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _file ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create AudioBufer: " + _file, Logger.Verbosity.NORMAL ) ;
					return null ;
				}

				final HTMLSourceElement source = file.getHTMLSource() ;
				final WebSound sound = new WebSound( source, 0 ) ;

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

				return sound ; 
			}
		} ) ;
	
		return false ;
	}

	@Override
	public boolean shutdown()
	{
		clear() ;
		return true ;
	}

	@Override
	public void setListenerPosition( final float _x, final float _y, final float _z )
	{
	}

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	@Override
	public ISource create( final String _file, final StreamType _type )
	{
		final WebSound sound = staticSoundManager.get( _file ) ;
		if( sound == null )
		{
			System.out.println( "Sound Doesn't exist." ) ;
			return null ;
		}

		return new WebAudioSource( sound ) ;
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
