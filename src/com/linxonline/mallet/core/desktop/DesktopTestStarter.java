package com.linxonline.mallet.core.desktop ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.test.GameTestLoader ;

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
