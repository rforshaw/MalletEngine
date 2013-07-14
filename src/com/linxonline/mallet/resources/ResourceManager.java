package com.linxonline.mallet.resources ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.sound.* ;
import com.linxonline.mallet.resources.texture.* ;

public class ResourceManager
{
	protected static final ResourceManager resourceManager = new ResourceManager() ;
	protected final LanguageManager languageManager = new LanguageManager() ;
	protected Settings config = new Settings() ;							// Global Game Settings.

	public FileSystem fileSystem = null ;									// Should specify at Start up - Main

	protected ResourceManager() {}

	public static synchronized ResourceManager getResourceManager()
	{
		return resourceManager ;
	}

	/* Language Manager - Enables multi-langauge games */

	public static String getText( final String _keyword )
	{
		return resourceManager.languageManager.getText( _keyword ) ;
	}

	public static void setLanguage( final String _language )
	{
		resourceManager.languageManager.setLanguage( _language ) ;
	}
	
	public static boolean loadLanguageFile( final String _file )
	{
		return resourceManager.languageManager.loadLanguageFile( _file ) ;
	}
	
	public static boolean containsLanguageFile( final String _file )
	{
		return resourceManager.languageManager.containsLanguageFile( _file ) ;
	}

	/* Game System Settings */

	public static Settings getConfig()
	{
		return resourceManager.config ;
	}

	public static void setConfig( Settings _config )
	{
		resourceManager.config = _config ;
	}

	/* File System */
	public static void setFileSystem( final FileSystem _fileSystem )
	{
		resourceManager.fileSystem = _fileSystem ;
	}
	
	public static FileSystem getFileSystem()
	{
		return resourceManager.fileSystem ;
	}

	/**
		Clear all allocated resources
		irrelevant of references.
	**/
	public static void clear()
	{
		resourceManager.languageManager.clear() ;
	}

	/**
		Remove unused resources
	**/
	public static void clean() {}

	/**
		Clear resources and shutdown connections with Sound System
	**/
	public static void shutdown()
	{
		clear() ;
	}

	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException() ;
	}
}
