package template.main ;

import com.linxonline.mallet.game.* ;

public class TemplateLoader extends GameLoader
{
	public TemplateLoader() {}

	@Override
	public void loadGame( final GameSystem _system )
	{
		_system.addGameState( new GameState( "DEFAULT" )
		{
			public void initGame()
			{
				// Start Here
			}
		} ) ;

		_system.setDefaultGameState( "DEFAULT" ) ;		// Define what Game State should be run first
	}
}