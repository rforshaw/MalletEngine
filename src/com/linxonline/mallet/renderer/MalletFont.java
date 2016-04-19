package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.renderer.font.FontAssist ;
import com.linxonline.mallet.renderer.font.Font ;

public class MalletFont
{
	public static final int PLAIN = 5 ;

	public final Font font ;			// Wrapper around platform specific implementation
	public final String fontName ;		// The Family Font Name
	public final int style ;			// Italic, Bold, Plain, currently only Plain is supported
	public final int size ;				// Text point size

	public MalletFont( final String _name, final int _style, final int _size )
	{
		fontName = _name ;
		style = _style ;
		size = _size ;

		font = FontAssist.createFont( fontName, style, size ) ;
	}

	public MalletFont( final String _name, final int _size )
	{
		this( _name, PLAIN, _size ) ;
	}

	public MalletFont( final String _name )
	{
		this( _name, 12 ) ;
	}

	public String getFontName()
	{
		return fontName ;
	}

	public int getHeight()
	{
		return font.getHeight() ;
	}

	public int stringWidth( final StringBuilder _text )
	{
		return font.stringWidth( _text ) ;
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
