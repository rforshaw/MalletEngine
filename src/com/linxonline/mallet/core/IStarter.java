package com.linxonline.mallet.core ;

import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.core.GameSystem ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.io.filesystem.FileSystem ;

/**
	Each platform supported should implement this interface.
	Allows the developer to define what and how things 
	should be loaded on start. Direct implementations of this 
	class should be abstract.
	Example: DesktopStarter.java
*/
public interface IStarter
{
	public void init() ;																		// Starts the whole process

	public GameSettings getGameSettings() ;
	public GameLoader getGameLoader() ;														// Return the Game States for the game

	public boolean loadGame( final GameSystem _system, final GameLoader _loader ) ;			// Load the 

	public void loadFileSystem( final FileSystem _fileSystem ) ;							// Set GlobalFileSystem and do any heavy initialisation
	public void loadConfig() ;																// Load the configuration file, Define the Config Parser and set GlobalConfig

	public void setRenderSettings( final ISystem _system ) ;						// Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
}
