package com.linxonline.malleteditor.main ;

import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.game.GameSystem ;

import com.linxonline.malleteditor.system.EditorState ;

public class EditorLoader extends GameLoader
{
	public EditorLoader() {}

	public void loadGame( final GameSystem _system )
	{
		_system.addGameState( new EditorState( "DEFAULT" ) ) ;
		_system.setDefaultGameState( "DEFAULT" ) ;
	}
}