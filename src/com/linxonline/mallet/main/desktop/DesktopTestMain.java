package com.linxonline.mallet.main.desktop ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public final class DesktopTestMain
{
	private DesktopTestMain() {}

	public static void main( final String _args[] )
	{
		final DesktopTestStarter starter = new DesktopTestStarter() ;
		starter.init() ;
		starter.run() ;
	}
}
