package com.linxonline.mallet.renderer ;

import java.util.Set ;
import java.util.Map ;

import java.lang.ref.WeakReference ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.MalletMap ;

/**
	Program Data is render implentation agnostic. 
	It allows a developer to map Java variables to shader inputs.
	However, a developer should never directly interact with Program Data 
	and should do everything through ProgramAssist.

	Not all Java variables can be directly mapped to a shader.
	For example desktop OpenGL will allow you to map MalletTexture, 
	Matrix4, Vector2 and Vector3 and the primitive types.
*/
public class ProgramMap<U> implements Program<ProgramMap>
{
	private final String id ;
	private final Map<String, Object> uniforms = MalletMap.<String, Object>newMap() ;

	private WeakReference<U> program = null ;			// Handler to renderer specific program.

	public ProgramMap( final String _id )
	{
		id = _id ;
	}

	public void setProgram( final U _program )
	{
		program = new WeakReference<U>( _program ) ;
	}

	public U getProgram()
	{
		return ( program != null ) ? program.get() : null ;
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

	public Map<String, Object> getMaps()
	{
		return uniforms ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof ProgramMap )
		{
			final ProgramMap program = ( ProgramMap )_obj ;
			if( id.equals( program.id ) == false )
			{
				return false ;
			}

			final Map<String, Object> u = program.getMaps() ;
			if( uniforms.size() != u.size() )
			{
				// If the hashmaps are not the same size then 
				// no point in continuing.
				return false ;
			}

			final Set<Map.Entry<String, Object>> entries = uniforms.entrySet() ;
			for( final Map.Entry<String, Object> entry : entries )
			{
				final Object obj1 = entry.getValue() ;
				final Object obj2 = u.get( entry.getKey() ) ;

				if( obj1.equals( obj2 ) == false )
				{
					return false ;
				}
			}
		}

		return true ;
	}

	@Override
	public int hashCode()
	{
		int hashcode = id.hashCode() ;
		hashcode *= uniforms.hashCode() ;
		return hashcode ;
	}
}
