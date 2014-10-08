package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.renderer.font.FontAssist ;
import com.linxonline.mallet.renderer.font.Font ;

public class MalletFont
{
	public static final int PLAIN = 5 ;

	public final Font font ;
	public final String fontName ;
	public final int style ;
	public final int size ;

	public MalletFont( final String _name, final int _style, final int _size )
	{
		fontName = _name ;
		style = _style ;
		size = _size ;

		font = FontAssist.createFont( fontName, style, size ) ;
	}

	public MalletFont( final String _name, final int _size )
	{
		fontName = _name ;
		size = _size ;
		style = PLAIN ;

		font = FontAssist.createFont( fontName, style, size ) ;
	}

	public String getFontName()
	{
		return fontName ;
	}

	public int getHeight()
	{
		return font.getHeight() ;
	}

	public int stringWidth( final String _text )
	{
		return font.stringWidth( _text ) ;
	}

	public Font getFont()
	{
		return font ;
	}
}
