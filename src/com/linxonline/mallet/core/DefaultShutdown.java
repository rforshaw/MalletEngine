package com.linxonline.mallet.core ;

import java.util.ArrayDeque ;

import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate.Callback ;

import com.linxonline.mallet.util.Logger ;

public class DefaultShutdown implements ISystem.ShutdownDelegate
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
		Logger.println( "Total Shutdown procedures: " + callbacks.size(), Logger.Verbosity.MINOR ) ;
		while( callbacks.isEmpty() == false )
		{
			Logger.println( "Shutdown procedure: " + callbacks.size(), Logger.Verbosity.MINOR ) ;
			callbacks.pop().shutdown() ;
		}
	}
}
