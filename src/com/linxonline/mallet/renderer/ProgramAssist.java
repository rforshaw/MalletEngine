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
	public void load( final String _id, final String _path )
	{
		assist.load( _id, _path ) ;
	}

	/**
		Use the unique identifier when loading the 
		program to create a handle to the Program.

		Use ProgramAssist.remove and ProgramAssist.map 
		to map/remove uniforms to the create program.
	*/
	public static Program create( final String _id )
	{
		return assist.create( _id ) ;
	}

	/**
		Remove a previous mapped uniform from the 
		passed in program.
	*/
	public static Program remove( final Program _program, final String _handler )
	{
		return assist.remove( _program, _handler ) ;
	}

	/**
		Map a uniform to the passed in program.

		Some rendering implementations will not render 
		the Draw if the Program it is associated to does 
		not contain valid uniform mappings.

		@param _program to map uniform to.
		@param _handler uniform name.
		@param _obj the uniform/value to be mapped.
	*/
	public static Program map( final Program _program, final String _handler, final Object _obj )
	{
		return assist.map( _program, _handler, _obj ) ;
	}

	public interface Assist
	{
		public void load( final String _id, final String _path ) ;

		public Program create( final String _id ) ;

		public Program remove( final Program _program, final String _handler ) ;
		public Program map( final Program _program, final String _handler, final Object _obj ) ;
	}
}
