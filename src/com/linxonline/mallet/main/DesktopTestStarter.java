package com.linxonline.mallet.main ;

import com.linxonline.mallet.system.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;

import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.game.GameTestLoader ;

public class DesktopTestStarter extends DesktopStarter
{
	/**
		Specify the backend systems to be used.
		Use OpenGL & OpenAL - GLDefaultSystem.
		Use the DesktopFileSystem for the file system.
	*/
	public DesktopTestStarter()
	{
		super( new GLDefaultSystem(), new DesktopFileSystem() ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}