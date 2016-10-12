package com.linxonline.mallet.main.web ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLElement ;
import org.teavm.jso.dom.html.HTMLSourceElement ;

import com.linxonline.mallet.main.web.WebTestStarter ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;

import com.linxonline.mallet.io.filesystem.web.WebFile ;

public final class Client
{
	public static void main( final String[] _args )
	{
		final WebTestStarter starter = new WebTestStarter() ;
		starter.init() ;
		starter.run() ;
	}
}
