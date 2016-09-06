package com.linxonline.mallet.renderer ;

import java.util.HashMap ;

import com.linxonline.mallet.maths.* ;

public class ProgramData<U> implements Program<ProgramData>
{
	private final String id ;
	private final HashMap<String, Object> uniforms = new HashMap<String, Object>() ;
	private U program ;

	public ProgramData( final String _id )
	{
		id = _id ;
	}

	public void setProgram( final U _program )
	{
		program = _program ;
	}

	public U getProgram()
	{
		return program ;
	}

	public String getID()
	{
		return id ;
	}

	public void remove( final String _handler )
	{
		uniforms.remove( _handler ) ;
	}

	public void set( final String _id, final Object _value )
	{
		uniforms.put( _id, _value ) ;
	}

	public Object get( final String _id )
	{
		return uniforms.get( _id ) ;
	}
}
