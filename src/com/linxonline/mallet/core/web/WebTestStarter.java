package com.linxonline.mallet.main.web ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.test.GameTestLoader ;

public class WebTestStarter extends WebStarter
{
	/**
		Specify the backend systems to be used.
		Use WebGL & teaVM Audio - GLDefaultSystem.
		Use the WebFileSystem for the file system.
	*/
	public WebTestStarter() {}

	@Override
	public GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}
