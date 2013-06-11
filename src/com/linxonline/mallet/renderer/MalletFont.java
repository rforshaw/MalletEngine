package com.linxonline.mallet.renderer ;

public class MalletFont
{
	public static final int PLAIN = 5 ;

	public Object font = null ;
	public String fontName ;
	public int style = PLAIN ;
	public int size = 12 ;
	
	public MalletFont( final String _name, final int _style, final int _size )
	{
		fontName = _name ;
		style = _style ;
		size = _size ;
	}
	
	public String getFontName()
	{
		return fontName ;
	}
	
	public void setFont( final Object _font )
	{
		font = _font ;
	}
	
	public Object getFont()
	{
		return font ;
	}
}
