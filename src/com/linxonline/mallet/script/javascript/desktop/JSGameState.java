package com.linxonline.mallet.script.javascript ;

import com.linxonline.mallet.core.GameState ;

public final class JSGameState
{
	private final GameState state ;

	private JSEntitySystem entitySystem ;

	public JSGameState( final GameState _state )
	{
		state = _state ;
	}

	public JSEntitySystem getEntitySystem()
	{
		if( entitySystem == null )
		{
			entitySystem = new JSEntitySystem( state.getEntitySystem() ) ;
		}
		return entitySystem ;
	}
}
