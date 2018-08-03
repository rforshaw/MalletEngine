package com.linxonline.mallet.core.desktop ;

import com.linxonline.mallet.core.test.GameTestLoader ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public final class DesktopTestMain
{
	private DesktopTestMain() {}

	public static void main( final String _args[] )
	{
		final DesktopStarter starter = new DesktopStarter( new GameTestLoader() ) ;
		starter.run() ;
	}
}
