package com.linxonline.mallet.core ;

public interface IGameSystem
{
	public void runSystem() ;
	public void stopSystem() ;

	public void addUpdate( final IUpdate _update ) ;

	public void addGameState( final GameState _state ) ;
	public void setDefaultGameState( final String _name ) ;

	public interface IUpdate
	{
		public void update( final double _dt ) ;
	}
}
