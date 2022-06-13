package com.linxonline.mallet.renderer ;

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
	public static MalletFont.Metrics createMetrics( final MalletFont _font )
	{
		return inter.createMetrics( _font ) ;
	}

	public static Glyph createGlyph( final MalletFont _font, final int _code ) 
	{
		return inter.createGlyph( _font, _code ) ;
	}

	/**
		Load a font into the font system.

		@param the relative path to the specified font file.

		The default Desktop implementation will use Java's built 
		in font loading system.
		The default Android implementation will use Android's
		loading system.
	*/
	public static String[] loadFont( final String _path )
	{
		return inter.loadFont( _path ) ;
	}

	public interface Assist
	{
		public MalletFont.Metrics createMetrics( final MalletFont _font ) ;
		public Glyph createGlyph( final MalletFont _font, final int _code ) ;
		public String[] loadFont( final String _path ) ;
	}
}
