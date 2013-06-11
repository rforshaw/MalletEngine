package com.linxonline.mallet.util.logger ;

/**
	Logger operates exactly like System.out.println, however, 
	it will filter out print calls based on the verbosity levels.

	This allows the developer to keep all of their print commands, 
	and not have to comment them out, for particular reasons.

	If Logger.verbosity = 10, any call with _verbosity <= 10 
	will not be printed.
**/
public abstract class Logger
{
	private static int verbosity = 0 ; // Used to determine what should be printed.

	public Logger() {}

	public static void setVerbosity( final int _verbosity )
	{
		verbosity = _verbosity ;
	}

	public static void println( final boolean _bool, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _bool ) ;
		}
	}

	public static void println( final char _char, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _char ) ;
		}
	}

	public static void println( final char[] _char, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _char ) ;
		}
	}

	public static void println( final double _no, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final float _no, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final int _no, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final long _no, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final Object _obj, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _obj ) ;
		}
	}
	
	public static void println( final String _text, final int _verbosity )
	{
		if( _verbosity > verbosity )
		{
			System.out.println( _text ) ;
		}
	}
}
