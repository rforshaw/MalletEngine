package com.linxonline.mallet.renderer.font ;

public abstract class Font<T>
{
	protected T font ;

	public Font( T _font )
	{
		font = _font ;
	}

	public abstract int getHeight() ;
	public abstract int stringWidth( final StringBuilder _text ) ;
	public abstract int stringWidth( final String _text ) ;

	public void setFont( final T _font )
	{
		font = _font ;
	}

	public T getFont()
	{
		return font ;
	}
}