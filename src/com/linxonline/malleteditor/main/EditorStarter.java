package com.linxonline.malleteditor.main ;

import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;

import com.linxonline.mallet.main.game.GameSettings ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.desktop.DesktopStarter ;
import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;

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
		super( new GLDefaultSystem() ) ;

		/*final GLDefaultSystem backend = ( GLDefaultSystem )backendSystem ;
		// Some applications may not want to pause the entire application 
		// while the user is not using it.
		// The below implementation will stop the application as soon 
		// as focus is lost. Focus is lost when the window is minimised 
		// or the user click on another window.
		backend.getWindow().addWindowListener( new WindowListener()
		{
			private boolean windowDeactivated = false ;
		
			public void windowGainedFocus( final WindowEvent _event )
			{
				if( windowDeactivated == true )
				{
					windowDeactivated = false ;
					run() ;
				}
			}

			public void windowLostFocus( final WindowEvent _event )
			{
				if( windowDeactivated == false )
				{
					windowDeactivated = true ;
					stop() ;
				}
			}

			public void windowDestroyNotify( final WindowEvent _event )
			{
				backendSystem.shutdownSystem() ;
			}

			public void windowRepaint( final WindowUpdateEvent _event ) {}
			public void windowDestroyed( final WindowEvent _event ) {}
			public void windowMoved( final WindowEvent _event ) {}
			public void windowResized( final WindowEvent _event ) {}
		} ) ;*/
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	@Override
	public void setRenderSettings( final SystemInterface _system )
	{
		super.setRenderSettings( _system ) ;
		final RenderInterface render = _system.getRenderInterface() ;

		// Override config setting and prevent ratio from being adhered to
		// Force Display and Render size parity.
		render.getRenderInfo().setKeepRenderRatio( false ) ;
		GlobalConfig.addBoolean( "DISPLAYRENDERPARITY", true ) ;
	}

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Editor" ) ;
	}

	@Override
	public GameLoader getGameLoader()
	{
		return new EditorLoader() ;
	}
}
