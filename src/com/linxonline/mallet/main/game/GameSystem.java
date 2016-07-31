package com.linxonline.mallet.main.game ;

import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.main.game.statemachine.* ;
import com.linxonline.mallet.util.time.* ;

public class GameSystem
{
	protected SystemInterface system = null ;
	protected final StateMachine stateMachine = new StateMachine() ;
	protected boolean running = false ;

	public GameSystem() {}

	public GameSystem( final SystemInterface _system )
	{
		setSystem( _system ) ;
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
		setSystem provides access to the low level systems, for 
		example the FileSystem, Rendering, etc.
		This must be set BEFORE any GameStates are added!
	**/
	public final void setSystem( final SystemInterface _system )
	{
		assert _system != null ;
		system = _system ;
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