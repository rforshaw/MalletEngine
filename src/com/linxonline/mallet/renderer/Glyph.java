package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.ISort ;

public class Glyph implements ISort
{
	public final char character ;
	public final float width ;

	public Glyph( final char _char, final float _width )
	{
		character = _char ;
		width = _width ;
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
	public int sortValue()
	{
		return ( int )character ;
	}
}
