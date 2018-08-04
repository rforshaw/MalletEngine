package com.linxonline.mallet.core ;

/**
	Provides a robust means of defining what Game States
	should be loaded into the Game System.
	This enables a developer to initialise their Game States 
	once for all platforms.
*/
public interface IGameLoader
{
	/**
		Game Settings that should be applied across all platforms.
		Platform specific values can be defined by implementing 
		getGameSettings in their Platform starter.
	*/
	public GameSettings getGameSettings() ;

	/**
		Load the game states into the game-system. 
	*/
	public void loadGame( final IGameSystem _system ) ;
}
