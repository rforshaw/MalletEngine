package com.linxonline.mallet.renderer.font ;

import com.linxonline.mallet.util.sort.SortInterface ;

public class Glyph implements SortInterface
{
	public final char character ;
	public float start ;
	public float advance ;

	public Glyph( final char _char, final float _start, final float _advance )
	{
		character = _char ;
		start = _start ;
		advance = _advance ;
	}

	@Override
	public int sortValue()
	{
		return ( int )character ;
	}
}