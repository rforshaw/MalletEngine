package com.linxonline.mallet.main.web ;

import com.linxonline.mallet.system.web.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.web.WebFileSystem ;

import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.game.test.GameTestLoader ;

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

		//final GLDefaultSystem backend = ( GLDefaultSystem )backendSystem ;
		// Some applications may not want to pause the entire application 
		// while the user is not using it.
		// The below implementation will stop the application as soon 
		// as focus is lost. Focus is lost when the window is minimised 
		// or the user click on another window.
		/*backend.getFrame().addWindowListener( new WindowListener()
		{
			private boolean windowDeactivated = false ;
		
			public void windowActivated( final WindowEvent _event )
			{
				if( windowDeactivated == true )
				{
					windowDeactivated = false ;
					run() ;
				}
			}

			public void windowDeactivated( final WindowEvent _event )
			{
				if( windowDeactivated == false )
				{
					windowDeactivated = true ;
					stop() ;
				}
			}

			public void windowClosing( final WindowEvent _event )
			{
				stop() ;
				backendSystem.shutdownSystem() ;
			}

			public void windowClosed( final WindowEvent _event ) {}
			public void windowDeiconified( final WindowEvent _event ) {}
			public void windowIconified( final WindowEvent _event ) {}
			public void windowOpened( final WindowEvent _event ) {}
		} ) ;*/
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}