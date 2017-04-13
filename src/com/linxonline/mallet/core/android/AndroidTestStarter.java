package com.linxonline.mallet.core.android ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.test.GameTestLoader ;
import com.linxonline.mallet.util.notification.Notification ;

public class AndroidTestStarter extends AndroidStarter
{
	/**
		Specify the backend systems to be used.
		Use WebGL & teaVM Audio - GLDefaultSystem.
		Use the WebFileSystem for the file system.
	*/
	public AndroidTestStarter( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		super( _activity, _notify ) ;
	}

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Engine - Test" ) ;
	}

	@Override
	public GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}
