package com.linxonline.mallet.main.desktop ;

import com.linxonline.mallet.main.game.GameSettings ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.test.GameTestLoader ;

public class DesktopTestStarter extends DesktopStarter
{
	/**
		Specify the backend systems to be used.
		Use OpenGL & OpenAL - GLDefaultSystem.
		Use the DesktopFileSystem for the file system.
	*/
	public DesktopTestStarter() {}

	@Override
	public GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}
