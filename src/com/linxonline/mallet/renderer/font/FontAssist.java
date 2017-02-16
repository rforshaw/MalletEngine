package com.linxonline.mallet.renderer.font ;

public class FontAssist
{
	private static FontAssist.Assist inter ;

	private FontAssist() {}

	/**
		This should be set by the Renderer.
	*/
	public static void setAssist( final FontAssist.Assist _interface )
	{
		assert _interface != null ;
		inter = _interface ;
	}

	/**
		Called by MalletFont constructor.
		Create a valid font that can be used by the active 
		rendering system.
		
		@param _name is the name of the font.
		@param _style Plain, Bold, Italics
		@param _size point size
	*/
	public static Font createFont( final String _name, final int _style, final int _size )
	{
		return inter.createFont( _name, _style, _size ) ;
	}

	/**
		Load a font into the font system.

		@param the relative path to the specified font file.

		The default Desktop implementation will use Java's built 
		in font loading system.
		The default Android implementation will use Android's
		loading system.
	*/
	public static boolean loadFont( final String _path )
	{
		return inter.loadFont( _path ) ;
	}

	public static interface Assist
	{
		public Font createFont( final String _font, final int _style, final int _size ) ;
		public boolean loadFont( final String _path ) ;
	}
}
