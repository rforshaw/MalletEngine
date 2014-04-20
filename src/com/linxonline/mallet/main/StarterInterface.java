package com.linxonline.mallet.main ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.io.filesystem.FileSystem ;

/**
	Each platform supported should implement this interface.
	Allows the developer to define what and how things 
	should be loaded on start. Direct implementations of this 
	class should be abstract.
	Example: DesktopStarter.java
*/
public abstract class StarterInterface
{
	public abstract void init() ;																		// Starts the whole process

	protected abstract GameLoader getGameLoader() ;														// Return the Game States for the game
	protected abstract boolean loadGame( final GameSystem _system, final GameLoader _loader ) ;			// Load the 
	
	protected abstract void loadFileSystem( final FileSystem _fileSystem ) ;							// Set GlobalFileSystem and do any heavy initialisation
	protected abstract void loadConfig() ;																// Load the configuration file, Define the Config Parser and set GlobalConfig

	protected abstract void setRenderSettings( final SystemInterface _system ) ;						// Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
}