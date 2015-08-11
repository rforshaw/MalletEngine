package com.linxonline.mallet.game.statemachine ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.* ;

public final class StateMachine
{
	private final ArrayList<State> states = new ArrayList<State>() ;
	private State currentState = null ;
	private State defaultState = null ;

	public StateMachine() {}
	
	public final void addState( final State _state )
	{
		if( exists( _state ) == true )
		{
			return ;
		}
		
		states.add( _state ) ;
	}

	/**
		Fallback state, if a state has been requested yet 
		doesn't exist, this state will take its place.
	*/
	public final void setDefaultState( final String _name )
	{
		for( State state : states )
		{
			if( _name.equals( state.name ) == true )
			{
				defaultState = state ;
				return ;
			}
		}
	}

	/**
		Called when the application is being shutdown.
		A shutdown can be caused by the user closing the 
		application, or for Android onDestroy() being called.
	*/
	public final void shutdown()
	{
		for( State state : states )
		{
			state.shutdownState() ;
		}
	}

	public final void update( final double _dt )
	{
		if( currentState == null )
		{
			if( defaultState == null )
			{
				return ;
			}

			currentState = defaultState ;
			currentState.startState( null ) ;
		}

		currentState.update( _dt ) ;
		checkTransition( currentState ) ;
	}
	
	private final void checkTransition( final State _state )
	{
		final int transition = _state.checkTransition() ;
		Settings pack = null ;
		
		if( transition != State.NONE )
		{
			String name = _state.getTransition() ;
			if( exists( name ) == false )
			{
				currentState = defaultState ;
				currentState.startState( null ) ;
			}
			else
			{
				currentState = getState( name ) ;
			}
			
			if( transition == State.TRANSIST_SHUTDOWN )
			{
				pack = _state.shutdownState() ;
			}
			else
			{
				pack = _state.pauseState() ;
			}

			// Reset State, so it doesn't go in endless loop.
			currentState.transitionType = State.NONE ;
			currentState.startState( pack ) ;
		}
	}

	private final State getState( final String _name )
	{
		for( State state : states )
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
		return getState( _name ) != null ? true : false ;
	}

	private final boolean exists( State _state )
	{
		assert _state != null ;
		return states.contains( _state ) ;
	}
}
