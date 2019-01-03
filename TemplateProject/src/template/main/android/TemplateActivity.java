package template.main.android ;

import com.linxonline.mallet.main.android.AndroidActivity ;
import com.linxonline.mallet.main.android.AndroidStarter ;
import com.linxonline.mallet.util.notification.Notification ;

import template.main.TemplateLoader ;

public class TemplateActivity extends AndroidActivity
{
	public TemplateActivity()
	{
		super() ;
	}

	@Override
	public String getName()
	{
		return "TEMPLATE_ACTIVITY" ;
	}

	public AndroidStarter constructStarter( final AndroidActivity _activity, final Notification.Notify<Object> _notify )
	{
		return new AndroidStarter( _activity, _notify, new TemplateLoader() ) ;
	}
}
