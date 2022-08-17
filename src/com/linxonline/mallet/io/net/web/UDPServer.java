package com.linxonline.mallet.io.net ;

import java.util.Map ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPServer is designed to receive information from 
	the specified address and port. 
	It can then send a response to the sender using the 
	address and port provided by the sender.
*/
public class UDPServer implements AutoCloseable
{
	private byte[] sendBuffers = new byte[200] ;
	private final Address sourceAddress = new Address() ;

	private final Serialise.ByteOut out = new Serialise.ByteOut( null ) ;
	
	public UDPServer() {}

	public boolean init( final Address _address, final int _timeout )
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

	public boolean send( final Address _address, final IOutStream _out )
	{
		return true ;
	}

	public InStream receive( final InStream _stream )
	{
		return _stream ;
	}

	@Override
	public void close() throws Exception {}
}
