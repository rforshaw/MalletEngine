package com.linxonline.mallet.audio ;

public final class Volume
{
	private Category category ;
	private int volume ;

	public Volume()
	{
		this( new Category(), 100 ) ;
	}

	public Volume( final Category _cat )
	{
		this( _cat, 100 ) ;
	}

	public Volume( final Category _cat, final int _volume )
	{
		setCategory( _cat ) ;
		setVolume( _volume ) ;
	}

	public Volume( final Volume _volume )
	{
		this( _volume.category, _volume.volume ) ;
	}

	public void setCategory( final Category _cat )
	{
		category = _cat ;
	}

	public void setVolume( final int _volume )
	{
		volume = Volume.clamp( _volume, 0, 100 ) ;
	}

	public int getVolume()
	{
		return volume ;
	}

	public Category getCategory()
	{
		return category ;
	}

	@Override
	public String toString()
	{
		return "[" + category.toString() + ", " + volume + "]" ;
	}

	private static int clamp( final int _val, final int _min, final int _max )
	{
		return Math.max( _min, Math.min( _max, _val ) ) ;
	}
}
