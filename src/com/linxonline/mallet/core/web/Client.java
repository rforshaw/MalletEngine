package com.linxonline.mallet.main.web ;

import com.linxonline.mallet.core.test.GameTestLoader ;

public final class Client
{
	public static void main( final String[] _args )
	{
		final WebStarter starter = new WebStarter( new GameTestLoader() ) ;
		starter.run() ;
	}
}
