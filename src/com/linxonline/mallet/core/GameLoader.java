package com.linxonline.mallet.core ;

import com.linxonline.mallet.maths.Vector2 ;

/**
	Provides a robust means of defining what Game States
	should be loaded into the Game System.
	This enables a developer to initialise their Game States 
	once for all platforms.
*/
public abstract class GameLoader
{
	public GameLoader() {}

	/**
		Game Settings that should be applied across all platforms.
		Platform specific values can be defined by implementing 
		getGameSettings in their Platform starter.
	*/
	public abstract GameSettings getGameSettings() ;

	/**
		Load the game states into the game-system. 
	*/
	public abstract void loadGame( final GameSystem _system ) ;
}
