package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.GameSystem ;

public class EditorLoader implements IGameLoader
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
