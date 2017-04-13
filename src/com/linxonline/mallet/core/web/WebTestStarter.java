package com.linxonline.mallet.main.web ;

import com.linxonline.mallet.system.web.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.web.WebFileSystem ;

import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.test.GameTestLoader ;

public class WebTestStarter extends WebStarter
{
	/**
		Specify the backend systems to be used.
		Use WebGL & teaVM Audio - GLDefaultSystem.
		Use the WebFileSystem for the file system.
	*/
	public WebTestStarter()
	{
		super( new GLDefaultSystem(), new WebFileSystem() ) ;
	}

	@Override
	protected String getApplicationName()
	{
		return "test" ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}
