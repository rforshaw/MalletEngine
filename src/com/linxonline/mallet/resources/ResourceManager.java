package com.linxonline.mallet.resources ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.resources.texture.* ;

public class ResourceManager
{
	protected static ResourceManager resourceManager = null ;				// Singleton class, should be the only one!
	protected final LanguageManager languageManager = new LanguageManager() ;
	protected Settings config = new Settings() ;							// Global Game Settings.

	// Should specify at Start up - Main
	public FileSystem fileSystem = null ;
	
	// Must be set by the System - DefaultSystem etc.
	public ManagerInterface spriteManager = null ;
	
	protected ResourceManager() {}

	public static synchronized ResourceManager getResourceManager()
	{
		if( resourceManager == null )
		{
			resourceManager = new ResourceManager() ;
		}
	
		return resourceManager ;
	}

	/* Retrieve Resource */

	public Sprite getSprite( final String _file )
	{
		if( spriteManager == null )
		{
			System.out.println( "Sprite Manager doesn't exist" ) ;
			return null ;
		}

		final Sprite sprite = ( Sprite )spriteManager.get( _file ) ;
		return sprite ;
	}

	/* Language Manager - Enables multi-langauge games */

	public String getText( final String _keyword )
	{
		return languageManager.getText( _keyword ) ;
	}

	public void setLanguage( final String _language )
	{
		languageManager.setLanguage( _language ) ;
	}
	
	public boolean loadLanguageFile( final String _file )
	{
		return languageManager.loadLanguageFile( _file ) ;
	}
	
	public boolean containsLanguageFile( final String _file )
	{
		return languageManager.containsLanguageFile( _file ) ;
	}

	/* Game System Settings */

	public Settings getConfig()
	{
		return config ;
	}

	public void setConfig( Settings _config )
	{
		config = _config ;
	}

	/* File System */
	public void setFileSystem( final FileSystem _fileSystem )
	{
		fileSystem = _fileSystem ;
	}
	
	public FileSystem getFileSystem()
	{
		return fileSystem ;
	}

	/**
		Clear all allocated resources
		irrelevant of references.
	**/
	public void clear()
	{
		spriteManager.clear() ;
		languageManager.clear() ;
	}

	/**
		Remove unused resources
	**/
	public void clean()
	{
		spriteManager.clean() ;
	}
	
	/**
		Clear resources and shutdown connections with Sound System
	**/
	public void shutdown()
	{
		clear() ;
		//soundManager.shutdown() ;
	}

	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException() ;
	}
}
