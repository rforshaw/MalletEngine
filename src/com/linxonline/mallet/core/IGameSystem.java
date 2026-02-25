package com.linxonline.mallet.core ;

public interface IGameSystem
{
	public void run() ;

	public void addUpdate( final IUpdate _update ) ;

	public void addGameState( final GameState _state ) ;
	public void setDefaultGameState( final String _name ) ;

	public interface IUpdate
	{
		/**
			Add an additional process to the main-loop
			unbound by the restricted update rate of a GameState.
			A return of non-zero informs the game-system
			to begin the shutdown process.
		*/
		public int update( final double _dt ) ;
	}
}
