package com.linxonline.mallet.audio ;


public class Category
{
	public enum Channel
	{
		MUSIC,
		EFFECT,
		VOCAL,
		MASTER ;
	}

	private Category.Channel channel ;
	private int subChannel ;

	public Category()
	{
		this( Category.Channel.MUSIC, 0 ) ;
	}

	public Category( final Category.Channel _channel )
	{
		this( _channel, 0 ) ;
	}

	public Category( final Category.Channel _channel, final int _subChannel )
	{
		channel = _channel ;
		subChannel = _subChannel ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof Category )
		{
			final Category cat = ( Category )_obj ;
			if( cat.channel == channel &&
				cat.subChannel == subChannel )
			{
				return true ;
			}
		}

		return false ;
	}

	@Override
	public int hashCode()
	{
		return channel.hashCode() & subChannel ;
	}
}
