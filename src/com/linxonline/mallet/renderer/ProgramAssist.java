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

	/**
		Load a program/shader that can be used by the 
		active rendering system.

		@param _id unique identifier.
		@param _path to program definition.
	*/
	public static void load( final String _id, final String _path )
	{
		assist.load( _id, _path ) ;
	}

	/**
		Use the unique identifier when loading the 
		program to create a handle to the Program.

		Use ProgramAssist.remove and ProgramAssist.map 
		to map/remove uniforms to the create program.
	*/
	public static Program add( final Program _program )
	{
		return assist.add( _program ) ;
	}

	public static Program update( final Program _program )
	{
		return assist.update( _program ) ;
	}

	public interface Assist
	{
		public void load( final String _id, final String _path ) ;

		public Program add( final Program _program ) ;

		public Program update( final Program _program ) ;
	}
}
