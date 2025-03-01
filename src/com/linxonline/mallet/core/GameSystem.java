package com.linxonline.mallet.core ;

import java.util.List ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.time.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Debounce ;

/**
	Used by Dektop and Android.
*/
public final class GameSystem implements IGameSystem
{
	private final List<GameState> states = MalletList.<GameState>newList() ;
	private GameState currentState = null ;
	private GameState defaultState = null ;

	private final List<IUpdate> updates = MalletList.<IUpdate>newList() ;

	private boolean running = false ;

	public GameSystem()
	{
		addUpdate( this::updateState ) ;

		// For debounce to work it must be tied into the
		// main-loop, this allows it to track when the 
		// runnable can be triggered.
		addUpdate( new Debounce() ) ;
	}

	@Override
	public void addUpdate( final IUpdate _update )
	{
		updates.add( _update ) ;
	}

	@Override
	public void run()
	{
		if( defaultState == null )
		{
			return ;
		}

		// Call to ensure we don't have a massive delta
		// just before the game-loop actually starts.
		ElapsedTimer.getElapsedTimeInNanoSeconds() ;
		running = true ;

		currentState = defaultState ;
		currentState.startState( null ) ;

		while( running == true )
		{
			final double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
			final int size = updates.size() ;
			for( int i = 0; i < size; ++i )
			{
				final IUpdate update = updates.get( i ) ;
				update.update( dt ) ;
			}
		}

		if( currentState != null )
		{
			currentState.pauseState() ;
		}
	}

	@Override
	public void stop()
	{
		running = false ;
	}

	@Override
	public final void addGameState( final GameState _state )
	{
		states.add( _state ) ;
	}

	/**
		Specify the Game State that will be used if another, 
		State is not available when called.
	**/
	@Override
	public final void setDefaultGameState( final String _name )
	{
		for( final GameState state : states )
		{
			if( _name.equals( state.name ) == true )
			{
				defaultState = state ;
				return ;
			}
		}
	}

	private final void updateState( final double _dt )
	{
		final int transition = currentState.update( _dt ) ;
		if( transition == GameState.NONE )
		{
			return ;
		}

		final GameState previous = currentState ;
		final String name = previous.getTransition() ;
		currentState = exists( name ) ? getGameState( name ) : defaultState ;

		switch( transition )
		{
			case GameState.TRANSIST_SHUTDOWN :
			{
				currentState.startState( previous.shutdownState() ) ;
				break ;
			}
			case GameState.TRANSIST_PAUSE    :
			{
				currentState.startState( previous.pauseState() ) ;
				break ;
			}
		}
	}

	private final GameState getGameState( final String _name )
	{
		for( final GameState state : states )
		{
			if( _name.equals( state.name ) == true )
			{
				return state ;
			}
		}

		return null ;
	}

	private final boolean exists( final String _name )
	{
		return getGameState( _name ) != null ? true : false ;
	}
}
