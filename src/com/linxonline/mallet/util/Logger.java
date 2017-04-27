package com.linxonline.mallet.util ;

/**
	Logger operates exactly like System.out.println, however, 
	it will filter out print calls based on the verbosity levels.

	This allows the developer to keep all of their print commands, 
	and not have to comment them out, for particular reasons.

	Set Verbosity level to MINOR, will print out MINOR, NORMAL, & MAJOR calls.
	Set Verbosity level to NORMAL, will print out NORMAL, & MAJOR calls.
	Set Verbosity level to MAJOR, will print out MAJOR calls.
**/
public final class Logger
{
	private static Verbosity verbosity = Verbosity.MINOR ;

	private Logger() {}

	public static void setVerbosity( final Verbosity _verbosity )
	{
		verbosity = _verbosity ;
	}

	public static void println( final boolean _bool, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _bool ) ;
		}
	}

	public static void println( final char _char, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _char ) ;
		}
	}

	public static void println( final char[] _char, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _char ) ;
		}
	}

	public static void println( final double _no, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final float _no, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final int _no, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final long _no, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _no ) ;
		}
	}

	public static void println( final Object _obj, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _obj ) ;
		}
	}
	
	public static void println( final String _text, final Verbosity _verbosity )
	{
		if( _verbosity.ordinal() >= verbosity.ordinal() )
		{
			System.out.println( _text ) ;
		}
	}

	public enum Verbosity
	{
		MINOR, NORMAL, MAJOR
	}
}
