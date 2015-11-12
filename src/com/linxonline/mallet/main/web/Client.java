package com.linxonline.mallet.main.web ;

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.util.settings.Settings ;

public class Client
{
	public static void main(String[] args)
	{
		final Shape shape = new Shape( 10, 10 ) ;
		System.out.println( shape ) ;
	
		final Settings test = new Settings() ;
		test.addString( "TEST", "BOOM!!!!" ) ;
		System.out.println( test ) ;

		HTMLDocument document = HTMLDocument.current();
		HTMLElement div = document.createElement("div");
		div.appendChild(document.createTextNode("TeaVM generated element"));
		document.getBody().appendChild(div);
	}
}
