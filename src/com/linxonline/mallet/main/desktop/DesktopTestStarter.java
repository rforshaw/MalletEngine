package com.linxonline.mallet.main.desktop ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;

import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;

import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.test.GameTestLoader ;

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

		final GLDefaultSystem backend = ( GLDefaultSystem )backendSystem ;
		// Some applications may not want to pause the entire application 
		// while the user is not using it.
		// The below implementation will stop the application as soon 
		// as focus is lost. Focus is lost when the window is minimised 
		// or the user click on another window.
		backend.getFrame().addWindowListener( new WindowListener()
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
				backendSystem.shutdownSystem() ;
			}

			public void windowClosed( final WindowEvent _event ) {}
			public void windowDeiconified( final WindowEvent _event ) {}
			public void windowIconified( final WindowEvent _event ) {}
			public void windowOpened( final WindowEvent _event ) {}
		} ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}