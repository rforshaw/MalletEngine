package com.linxonline.malleteditor.core ;

import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;

import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.desktop.DesktopStarter ;
import com.linxonline.mallet.core.desktop.gl.GLDefaultSystem ;

import com.linxonline.mallet.renderer.IRender ;
import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.GlobalConfig ;

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
	public void setRenderSettings( final ISystem _system )
	{
		super.setRenderSettings( _system ) ;
		final IRender render = _system.getRenderer() ;

		// Override config setting and prevent ratio from being adhered to
		// Force Display and Render size parity.
		//render.getRenderInfo().setKeepRenderRatio( false ) ;
		GlobalConfig.addBoolean( "DISPLAYRENDERPARITY", true ) ;
	}

	@Override
	public GameLoader getGameLoader()
	{
		return new EditorLoader() ;
	}
}
