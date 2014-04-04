package com.linxonline.malleteditor.main ;

import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.main.DesktopStarter ;
import com.linxonline.mallet.system.GLDefaultSystem ;
import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;

import com.linxonline.malleteditor.system.GLEditorSystem ;

public class EditorStarter extends DesktopStarter
{
	/**
		Specify the backend systems to be used.
		Use OpenGL & OpenAL - GLDefaultSystem.
		Use the DesktopFileSystem for the file system.
	*/
	public EditorStarter()
	{
		super( new GLEditorSystem(), new DesktopFileSystem() ) ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new EditorLoader() ;
	}
}