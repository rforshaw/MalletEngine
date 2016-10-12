package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public final class ProgramAssist
{
	private static Assist assist ;

	private ProgramAssist() {}

	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	public static Program createProgram( final String _id )
	{
		return assist.createProgram( _id ) ;
	}

	public static Program remove( final Program _program, final String _handler )
	{
		return assist.remove( _program, _handler ) ;
	}

	public static Program map( final Program _program, final String _handler, final Object _obj )
	{
		return assist.map( _program, _handler, _obj ) ;
	}

	public interface Assist
	{
		public Program createProgram( final String _id ) ;

		public Program remove( final Program _program, final String _handler ) ;
		public Program map( final Program _program, final String _handler, final Object _obj ) ;
	}
}
