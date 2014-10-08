package com.linxonline.mallet.renderer.font ;

public abstract class Font<T>
{
	protected final T font ;

	public Font( T _font )
	{
		font = _font ;
	}

	public abstract int getHeight() ;
	public abstract int stringWidth( final String _text ) ;

	public T getFont()
	{
		return font ;
	}
}