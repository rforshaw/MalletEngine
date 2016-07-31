package com.linxonline.malleteditor.main ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;
import java.awt.event.ComponentEvent ;

import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.desktop.DesktopStarter ;
import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;

import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

public class EditorStarter extends DesktopStarter
{
	/**
		Specify the backend systems to be used.
		Use OpenGL & OpenAL - GLDefaultSystem.
		Use the DesktopFileSystem for the file system.
	*/
	public EditorStarter()
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

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	@Override
	protected void setRenderSettings( final SystemInterface _system )
	{
		super.setRenderSettings( _system ) ;
		final RenderInterface render = _system.getRenderInterface() ;

		// Override config setting and prevent ratio from being adhered to
		// Force Display and Render size parity.
		render.getRenderInfo().setKeepRenderRatio( false ) ;
		GlobalConfig.addBoolean( "DISPLAYRENDERPARITY", true ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new EditorLoader() ;
	}
}