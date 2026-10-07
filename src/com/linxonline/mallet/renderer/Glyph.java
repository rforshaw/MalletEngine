package com.linxonline.mallet.renderer ;

public final class Glyph
{
	public final char character ;
	public final float width ;

	public Glyph( final char _char, final float _width )
	{
		character = _char ;
		width = _width ;
	}

	public static int compare( final Glyph _a, final Glyph _b )
	{
		return _a.character - _b.character ;
	}

	public char getCharacter()
	{
		return character ;
	}

	public boolean isCharacter( final char _char )
	{
		return character == _char ;
	}

	public float getWidth()
	{
		return width ;
	}

	@Override
	public String toString()
	{
		return "Character: " + character + " Width: " + width ;
	}
}
