package com.linxonline.mallet.renderer.font ;

public class FontAssist
{
	private static FontAssist.Assist inter ;

	private FontAssist() {}

	/**
		This should be set by the Renderer.
		Allows developers outside to determine 
		the width & height of text outside the renderer.
	*/
	public static void setFontWrapper( final FontAssist.Assist _interface )
	{
		assert _interface != null ;
		inter = _interface ;
	}

	public static Font createFont( final String _name, final int _style, final int _size )
	{
		return inter.createFont( _name, _style, _size ) ;
	}
	
	public static interface Assist
	{
		public Font createFont( final String _font, final int _style, final int _size ) ;
	}
}