package com.linxonline.mallet.game.android ;

public class GameThread extends Thread
{
	private boolean started = false ;

	public GameThread() {}
	
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