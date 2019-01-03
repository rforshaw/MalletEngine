package template.main ;

import com.linxonline.mallet.core.* ;

public class TemplateLoader implements IGameLoader
{
	public TemplateLoader() {}

	@Override
	public void loadGame( final IGameSystem _system )
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

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Engine - Template" ) ;
	}
}
