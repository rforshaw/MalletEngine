package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.ISystem ;

public class EditorLoader implements IGameLoader
{
	public EditorLoader() {}

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Editor" ) ;
	}

	@Override
	public void loadGame( final ISystem _main, final IGameSystem _system )
	{
		_system.addGameState( new EditorState( "EDITOR", _main ) ) ;
		_system.addGameState( new UIEditorState( "UIEDITOR", _main ) ) ;

		_system.setDefaultGameState( "UIEDITOR" ) ;
	}
}
