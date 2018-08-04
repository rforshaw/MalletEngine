package com.linxonline.mallet.core ;

import com.linxonline.mallet.core.statemachine.* ;
import com.linxonline.mallet.util.time.* ;

public class GameSystem implements IGameSystem
{
	private final StateMachine stateMachine = new StateMachine() ;
	private boolean running = false ;
	private ISystem system ;

	public GameSystem() {}

	public void setMainSystem( final ISystem _system )
	{
		system = _system ;
	}

	public void runSystem()
	{
		double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
		running = true ;

		stateMachine.resume() ;
		while( running == true )
		{
			dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
			stateMachine.update( dt ) ;						// Update Game State
		}

		stateMachine.pause() ;
	}

	public void stopSystem()
	{
		running = false ;
	}

	public final void addGameState( final GameState _state )
	{
		assert _state != null ;
		_state.setSystem( system ) ;
		stateMachine.addState( _state ) ;
	}

	/**
		Specify the Game State that will be used if another, 
		State is not available when called.
	**/
	public final void setDefaultGameState( final String _name )
	{
		stateMachine.setDefaultState( _name ) ;
	}
}
