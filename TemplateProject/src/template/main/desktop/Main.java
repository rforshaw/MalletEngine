package template.main.desktop ;

import com.linxonline.mallet.core.desktop.DesktopStarter ;
import template.main.TemplateLoader ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	public static void main( String _args[] )
	{
		final DesktopStarter starter = new DesktopStarter( new TemplateLoader() ) ;
		starter.run() ;
	}
}
