package com.linxonline.mallet.game.statemachine ;

import com.linxonline.mallet.util.settings.* ;

public abstract class State
{
	public static final int NONE = 0 ;
	public static final int TRANSIST_SHUTDOWN = 1 ;
	public static final int TRANSIST_PAUSE = 2 ;

	public String name ;
	protected String transition ;
	protected int transitionType = NONE ;

	public State( final String _name )
	{
		name = _name ;
	}

	public abstract void update( final double _dt ) ;

	public abstract void startState( final Settings _package ) ;

	public abstract Settings shutdownState() ;

	public abstract Settings pauseState() ;

	protected final void setTransition( final String _transition,
									   final int _transitionType )
	{
		transition = _transition ;
		transitionType = _transitionType ;
	}

	/**
		Called by StateMachine if transitioning.
		returns name of the State you wish to transition to
	**/
	public final String getTransition()
	{
		return transition ;
	}

	/** 
		checkTransition
		return NONE : if state isn't going to change
		return SHUTDOWN : if state will change and will clean up
		return PAUSE : if state will change but doesn't clean up
	**/
	public final int checkTransition()
	{
		return transitionType ;
	}
}