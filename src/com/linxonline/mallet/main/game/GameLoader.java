package com.linxonline.mallet.main.game ;

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

	public abstract void loadGame( final GameSystem _system ) ;
}
