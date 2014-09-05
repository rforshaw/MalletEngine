package com.linxonline.mallet.game ;

import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.game.statemachine.* ;
import com.linxonline.mallet.util.time.* ;

public final class GameSystem
{
	private SystemInterface system = null ;
	private final StateMachine stateMachine = new StateMachine() ;
	private boolean running = false ;

	public GameSystem() {}

	public GameSystem( final SystemInterface _system )
	{
		setSystem( _system ) ;
	}

	public final void runSystem()
	{
		double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
		running = true ;

		while( running == true )
		{
			dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
			running = update( dt ) ;
		}
	}

	public void stopSystem()
	{
		running = false ;
	}

	public boolean update( final double _dt )
	{
		stateMachine.update( _dt ) ;					// Update Game State
		return running ;
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