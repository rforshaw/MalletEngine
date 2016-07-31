package com.linxonline.mallet.main.android ;

import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.test.GameTestLoader ;
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
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}