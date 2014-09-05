package com.linxonline.mallet.game.android ;

public class GameThread extends Thread
{
	private boolean started = false ;

	public GameThread()
	{
		super() ;
	}

	public GameThread( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void start()
	{
		if( started == false )
		{
			super.start() ;
			started = true ;
		}
	}
}