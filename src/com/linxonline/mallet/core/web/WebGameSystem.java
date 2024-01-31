package com.linxonline.mallet.core.web ;

import java.util.List ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.browser.AnimationFrameCallback ;

import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.time.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Debounce ;

public class WebGameSystem implements IGameSystem
{
	private final List<GameState> states = MalletList.<GameState>newList() ;
	private GameState currentState = null ;
	private GameState defaultState = null ;

	private final List<IUpdate> updates = MalletList.<IUpdate>newList() ;

	private boolean running = false ;
	private ISystem system ;

	public WebGameSystem()
	{
		addUpdate( this::updateState ) ;
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

		final AnimationFrameCallback callback = new AnimationFrameCallback()
		{
			@Override
			public void onAnimationFrame( final double _timestamp )
			{
				final Thread thread = new Thread( () ->
				{
					final double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
					final int size = updates.size() ;
					for( int i = 0; i < size; ++i )
					{
						final IUpdate update = updates.get( i ) ;
						update.update( dt ) ;
					}
				} ) ;
				thread.start() ;
				Window.requestAnimationFrame( this ) ;
			}
		} ;

		if( currentState != null )
		{
			currentState.pauseState() ;
		}
		Window.requestAnimationFrame( callback ) ;
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
		final int transition = currentState.updateMain( _dt ) ;
		if( transition == GameState.NONE )
		{
			currentState.updateDraw( _dt ) ;
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
