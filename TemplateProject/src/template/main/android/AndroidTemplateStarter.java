package template.main.android ;

import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.android.AndroidActivity ;
import com.linxonline.mallet.main.android.AndroidStarter ;
import com.linxonline.mallet.util.notification.Notification ;

import template.main.TemplateLoader ;

public class AndroidTemplateStarter extends AndroidStarter
{
	/**
		Specify the backend systems to be used.
		Use WebGL & teaVM Audio - GLDefaultSystem.
		Use the WebFileSystem for the file system.
	*/
	public AndroidTemplateStarter( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		super( _activity, _notify ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new TemplateLoader() ;
	}
}
