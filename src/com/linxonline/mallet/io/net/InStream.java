package com.linxonline.mallet.io.net ;

public class InStream
{
	private final byte[] buffer ;
	private int length ;

	private Address sender = new Address() ;

	public InStream( final int _length )
	{
		buffer = new byte[_length] ;
		length = _length ;
	}

	public void setSender( final Address _address )
	{
		sender.set( _address ) ;
	}

	public Address getSender()
	{
		return getSender( new Address() ) ;
	}

	public Address getSender( final Address _fill )
	{
		_fill.set( sender ) ;
		return _fill ;
	}

	public void setDataLength( final int _length )
	{
		length = _length ;
	}

	public int getDataLength()
	{
		return length ;
	}

	public byte[] getBuffer()
	{
		return buffer ;
	}
}
