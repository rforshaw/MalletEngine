package com.linxonline.mallet.renderer ;

import java.util.HashMap ;

import com.linxonline.mallet.maths.* ;

/**
	Program Data is render implentation agnostic. 
	It allows a developer to map Java variables to shader inputs.
	However, a developer should never directly interact with Program Data 
	and should do everything through ProgramAssist.

	Not all Java variables can be directly mapped to a shader.
	For example desktop OpenGL will allow you to map MalletTexture, 
	Matrix4, Vector2 and Vector3 and the primitive types.
*/
public class ProgramData<U> implements Program<ProgramData>
{
	private final String id ;
	private final HashMap<String, Object> uniforms = new HashMap<String, Object>() ;

	private U program ;			// Handler to renderer specific program.

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

	/**
		Return the mapped object associated with _id.
	*/
	public Object get( final String _id )
	{
		return uniforms.get( _id ) ;
	}

	/**
		Return the mapped object associated with _id.
		Will automatically cast object to _clazz.
	*/
	public <T> T get( final String _id, final Class<T> _clazz )
	{
		return _clazz.cast( get( _id ) ) ;
	}

	public HashMap<String, Object> getMaps()
	{
		return uniforms ;
	}
}
