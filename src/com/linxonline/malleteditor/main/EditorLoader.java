package com.linxonline.malleteditor.main ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.GameSystem ;

import com.linxonline.malleteditor.system.EditorState ;
import com.linxonline.malleteditor.system.UIEditorState ;

public class EditorLoader extends GameLoader
{
	public EditorLoader() {}

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Editor" ) ;
	}

	@Override
	public void loadGame( final GameSystem _system )
	{
		_system.addGameState( new EditorState( "EDITOR" ) ) ;
		_system.addGameState( new UIEditorState( "UIEDITOR" ) ) ;

		_system.setDefaultGameState( "UIEDITOR" ) ;
	}
}
