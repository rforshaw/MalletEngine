package com.linxonline.mallet.script.javascript ;

import com.linxonline.mallet.util.Logger ;

public final class JSLogger
{
	public final static String MINOR = "MINOR" ;
	public final static String NORMAL = "NORMAL" ;
	public final static String MAJOR = "MAJOR" ;

	public JSLogger() {}

	public static void setVerbosity( final String _verbosity )
	{
		Logger.setVerbosity( Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final boolean _bool, final String _verbosity )
	{
		Logger.println( _bool, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final char _char, final String _verbosity )
	{
		Logger.println( _char, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final char[] _char, final String _verbosity )
	{
		Logger.println( _char, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final double _no, final String _verbosity )
	{
		Logger.println( _no, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final float _no, final String _verbosity )
	{
		Logger.println( _no, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final int _no, final String _verbosity )
	{
		Logger.println( _no, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final long _no, final String _verbosity )
	{
		Logger.println( _no, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void println( final Object _obj, final String _verbosity )
	{
		Logger.println( _obj, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}
	
	public void println( final String _text, final String _verbosity )
	{
		Logger.println( _text, Logger.Verbosity.valueOf( _verbosity ) ) ;
	}

	public void printStackTrace( final String _verbosity )
	{
		Logger.printStackTrace( Logger.Verbosity.valueOf( _verbosity ) ) ;
	}
}
