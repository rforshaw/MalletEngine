package com.linxonline.mallet.system ;

import java.util.ArrayDeque ;

import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate.Callback ;

public class DefaultShutdown implements SystemInterface.ShutdownDelegate
{
	private final ArrayDeque<ShutdownDelegate.Callback> callbacks = new ArrayDeque<ShutdownDelegate.Callback>() ;

	public DefaultShutdown() {}

	public void addShutdownCallback( final ShutdownDelegate.Callback _callback )
	{
		if( callbacks.contains( _callback ) == false )
		{
			callbacks.add( _callback ) ;
		}
	}

	public void removeShutdownCallback( final ShutdownDelegate.Callback _callback )
	{
		if( callbacks.contains( _callback ) == true )
		{
			callbacks.remove( _callback ) ;
		}
	}

	public void shutdown()
	{
		while( callbacks.isEmpty() == false )
		{
			callbacks.pop().shutdown() ;
		}
	}
}
