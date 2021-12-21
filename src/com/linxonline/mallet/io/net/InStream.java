package com.linxonline.mallet.io.net ;

import com.linxonline.mallet.io.serialisation.Serialise ;

public class InStream
{
	private final byte[] buffer ;
	private int length ;

	private Address address ;

	public InStream( final int _length )
	{
		buffer = new byte[_length] ;
		length = _length ;
	}

	public void setSender( final Address _address )
	{
		address = _address ;
	}

	public Address getSender()
	{
		return address ;
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
