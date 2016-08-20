package template.main.desktop ;

import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;

import com.linxonline.mallet.main.desktop.DesktopStarter ;

import com.linxonline.mallet.main.game.GameLoader ;
import template.main.TemplateLoader ;

public class TemplateStarter extends DesktopStarter
{
	/**
		Specify the backend systems to be used.
		Use OpenGL & OpenAL - GLDefaultSystem.
		Use the DesktopFileSystem for the file system.
	*/
	public TemplateStarter()
	{
		super( new GLDefaultSystem(), new DesktopFileSystem() ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new TemplateLoader() ;
	}
}
