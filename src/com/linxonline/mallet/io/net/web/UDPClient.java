package com.linxonline.mallet.io.net ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPClient is designed to send information to 
	the specified address and port passed into init().
	
*/
public class UDPClient implements AutoCloseable
{
	private final Serialise.ByteOut out = new Serialise.ByteOut( null ) ;

	public UDPClient() {}

	public boolean init( final Address _target, final int _timeout )
	{
		try
		{
			return true ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	public boolean send( final IOutStream _out )
	{
		return true ;
	}

	public InStream receive( final InStream _stream  )
	{
		return _stream ;
	}

	@Override
	public void close() throws Exception
	{
	}
}
