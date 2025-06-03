package com.linxonline.mallet.io ;

import java.awt.Toolkit ;
import java.awt.datatransfer.DataFlavor ;
import java.awt.datatransfer.StringSelection ;
import java.awt.datatransfer.Clipboard ;

public class GlobalClipboard
{
	private GlobalClipboard() {}

	public static void store( final String _txt )
	{
		getClipboard().setContents( new StringSelection( _txt ), null ) ;
	}

	public static String get()
	{
		final Clipboard clipboard = getClipboard() ;
		final DataFlavor flavour = DataFlavor.stringFlavor ;

		if( clipboard.isDataFlavorAvailable( flavour ) )
		{
			try
			{
				final String text = ( String )clipboard.getData( flavour ) ;
				return text;
			}
			catch( final Exception ex )
			{
				ex.printStackTrace() ;
			}
		}

		return "" ;
	}

	private static Clipboard getClipboard()
	{
		return Toolkit.getDefaultToolkit().getSystemClipboard()  ;
	}
}
