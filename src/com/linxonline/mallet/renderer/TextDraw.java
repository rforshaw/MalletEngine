package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.caches.Cacheable ;

public class TextDraw implements Cacheable
{
	private StringBuilder text = null ;
	private int textStart = 0 ;
	private int textEnd = 0 ;

	public TextDraw() {}

	public void setText( final StringBuilder _text )
	{
		text = _text ;
		setTextStart( 0 ) ;
		setTextEnd( text.length() ) ;
	}

	public void setTextStart( final int _start )
	{
		textStart = _start ;
	}

	public void setTextEnd( final int _end )
	{
		textEnd = _end ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public int getTextStart()
	{
		return textStart ;
	}

	public int getTextEnd()
	{
		return textEnd ;
	}

	@Override
	public void reset()
	{
		text = null ;
		textStart = 0 ;
		textEnd = 0 ;
	}
}
