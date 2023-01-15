package com.linxonline.mallet.core ;

import java.util.List ;

import com.linxonline.mallet.core.statemachine.* ;
import com.linxonline.mallet.util.time.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Debounce ;

/**
	Used by Dektop and Android.
*/
public class GameSystem implements IGameSystem
{
	private final StateUpdater state = new StateUpdater() ;
	private final List<IUpdate> updates = MalletList.<IUpdate>newList() ;

	private boolean running = false ;
	private ISystem system ;

	public GameSystem()
	{
		addUpdate( state ) ;
		// For debounce to work it must be tied into the
		// main-loop, this allows it to track when the 
		// runnable can be triggered.
		addUpdate( new Debounce() ) ;
	}

	public void setMainSystem( final ISystem _system )
	{
		system = _system ;
	}

	@Override
	public void addUpdate( final IUpdate _update )
	{
		updates.add( _update ) ;
	}

	@Override
	public void runSystem()
	{
		// Call to ensure we don't have a massive delta
		// just before the game-loop actually starts.
		ElapsedTimer.getElapsedTimeInNanoSeconds() ;
		running = true ;

		state.resume() ;
		while( running == true )
		{
			final double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
			for( final IUpdate update : updates )
			{
				update.update( dt ) ;
			}
		}

		state.pause() ;
	}

	@Override
	public void stopSystem()
	{
		running = false ;
	}

	@Override
	public final void addGameState( final GameState _state )
	{
		assert _state != null ;
		_state.setSystem( system ) ;
		state.addState( _state ) ;
	}

	/**
		Specify the Game State that will be used if another, 
		State is not available when called.
	**/
	@Override
	public final void setDefaultGameState( final String _name )
	{
		state.setDefaultState( _name ) ;
	}

	public final static class StateUpdater implements IUpdate
	{
		private final StateMachine machine = new StateMachine() ;

		public void resume()
		{
			machine.resume() ;
		}

		public void pause()
		{
			machine.pause() ;
		}

		public void addState( final GameState _state )
		{
			machine.addState( _state ) ;
		}

		public void setDefaultState( final String _name )
		{
			machine.setDefaultState( _name ) ;
		}

		@Override
		public void update( final double _dt )
		{
			machine.update( _dt ) ;		// Update Game State
		}
	}
}
