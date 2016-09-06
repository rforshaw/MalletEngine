package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public class ProgramAssist
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

	public static Program map( final Program _program, final String _handler, final Matrix3 _matrix )
	{
		return assist.map( _program, _handler, _matrix ) ;
	}

	public static Program map( final Program _program, final String _handler, final Matrix4 _matrix )
	{
		return assist.map( _program, _handler, _matrix ) ;
	}

	public static Program map( final Program _program, final String _handler, final Vector2 _vec2 )
	{
		return assist.map( _program, _handler, _vec2 ) ;
	}

	public static Program map( final Program _program, final String _handler, final Vector3 _vec3 )
	{
		return assist.map( _program, _handler, _vec3 ) ;
	}

	public interface Assist
	{
		public Program createProgram( final String _id ) ;

		public Program remove( final Program _program, final String _handler ) ;

		public Program map( final Program _program, final String _handler, final Matrix3 _matrix ) ;
		public Program map( final Program _program, final String _handler, final Matrix4 _matrix ) ;

		public Program map( final Program _program, final String _handler, final Vector2 _vec2 ) ;
		public Program map( final Program _program, final String _handler, final Vector3 _vec3 ) ;
	}
}
