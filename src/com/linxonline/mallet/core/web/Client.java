package com.linxonline.mallet.core.web ;

import com.linxonline.mallet.core.test.GameTestLoader ;

public class Client
{
	public static void main( final String[] _args )
	{
		final WebStarter starter = new WebStarter( new GameTestLoader() ) ;
		starter.run() ;
	}
}
