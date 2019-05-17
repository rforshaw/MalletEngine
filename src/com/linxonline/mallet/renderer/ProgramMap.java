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
public class ProgramMap<U> implements Program<ProgramMap<U>>
{
	private final String id ;
	private final Map<String, Object> uniforms = MalletMap.<String, Object>newMap() ;

	private WeakReference<U> program = null ;			// Handler to renderer specific program.
	private boolean dirty = true ;

	public ProgramMap( final String _id )
	{
		id = _id ;
	}

	public ProgramMap( final ProgramMap<U> _map )
	{
		id = _map.id ;
		uniforms.putAll( _map.getMaps() ) ;

		setProgram( _map.getProgram() ) ;
		setDirty( _map.dirty ) ;
	}

	public void setProgram( final U _program )
	{
		if( _program != getProgram() )
		{
			program = new WeakReference<U>( _program ) ;
			dirty() ;
		}
	}

	public U getProgram()
	{
		return ( program != null ) ? program.get() : null ;
	}

	public String getID()
	{
		return id ;
	}

	public boolean remove( final String _id )
	{
		if( uniforms.remove( _id ) != null )
		{
			dirty() ;
			return true ;
		}

		// Failed to remove an object associated with 
		// the passed in id - most likely never set.
		return false ;
	}

	public boolean set( final String _id, final Object _value )
	{
		if( _id == null || _value == null )
		{
			// The id or value cannot be null
			return false ;
		}

		if( _value == get( _id ) )
		{
			// Attempting reassign to the same object. 
			return false ;
		}

		uniforms.put( _id, _value ) ;
		dirty() ;
		return true ;
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

	public void dirty()
	{
		setDirty( true ) ;
	}

	public void setDirty( final boolean _dirty )
	{
		dirty = _dirty ;
	}

	public boolean isDirty()
	{
		return dirty ;
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
			final ProgramMap<U> program = ( ProgramMap<U> )_obj ;
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
			if( entries.isEmpty() == false )
			{
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
