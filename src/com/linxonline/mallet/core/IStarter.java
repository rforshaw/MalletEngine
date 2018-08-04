package com.linxonline.mallet.core ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.ISystem ;

/**
	Each platform supported should implement this interface.
	Allows the developer to define what and how things 
	should be loaded on start. Direct implementations of this 
	class should be abstract.
	Example: DesktopStarter.java
*/
public interface IStarter
{
	public IGameLoader getGameLoader() ;

	/**
		Will return the GameSettings defined by the IGameLoader.
		Can be overridden to implement platform specific requirements.
	*/
	public GameSettings getGameSettings() ;

	public ISystem getMainSystem() ;
}
