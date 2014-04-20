package com.linxonline.mallet.main.desktop ;

import com.linxonline.mallet.system.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;

import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.game.test.GameTestLoader ;

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