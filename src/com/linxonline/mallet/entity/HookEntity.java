package com.linxonline.mallet.entity ;

/**
	Provides a simple interface to allow the Entity System to hookup entities to 
	a game state, without having complete control over the State.
**/
public interface HookEntity
{
	public void hookEntity( final Entity _entity ) ;
	public void unhookEntity( final Entity _entity ) ;
}