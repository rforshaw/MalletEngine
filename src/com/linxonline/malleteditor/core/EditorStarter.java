package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.core.IGameLoader ;
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
		super( new GLDefaultSystem(), new EditorLoader() ) ;
	}

	@Override
	public void init()
	{
		super.init() ;
		GlobalConfig.addBoolean( "DISPLAYRENDERPARITY", true ) ;
	}
}
