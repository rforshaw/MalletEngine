package com.linxonline.malleteditor.main ;

import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;

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
	public GameLoader getGameLoader()
	{
		return new EditorLoader() ;
	}
}
