package com.linxonline.mallet.renderer ;

import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.util.MalletMap ;

public class Program
{
	private final static Utility utility = new Utility() ;

	private int index ;
	private String id ;

	private final Map<String, Object> uniforms = MalletMap.<String, Object>newMap() ;
	private final Map<String, Storage> storages = MalletMap.<String, Storage>newMap() ;

	public Program() {}

	public Program( final String _id )
	{
		index = utility.getGlobalIndex() ;
		id = _id ;
	}

	public Program( final Program _program )
	{
		index = _program.index ;
		id = _program.id ;

		uniforms.putAll( _program.uniforms ) ;
		storages.putAll( _program.storages ) ;
	}

	public void copy( final Program _program )
	{
		index = _program.index ;
		id = _program.id ;

		uniforms.clear() ;
		storages.clear() ;

		uniforms.putAll( _program.uniforms ) ;
		storages.putAll( _program.storages ) ;
	}

	public boolean removeUniform( final String _handler )
	{
		if( uniforms.remove( _handler ) != null )
		{
			return true ;
		}

		// Failed to remove an object associated with 
		// the passed in id - most likely never set.
		return false ;
	}

	public boolean mapUniform( final String _handler, final Object _obj )
	{
		if( _handler == null || _obj == null )
		{
			// The id or value cannot be null
			return false ;
		}

		if( _obj == uniforms.get( _handler ) )
		{
			// Attempting reassign to the same object. 
			return false ;
		}

		uniforms.put( _handler, _obj ) ;
		return true ;
	}

	/**
		Return the mapped object associated with _id.
	*/
	public Object getUniform( final String _id )
	{
		return uniforms.get( _id ) ;
	}

	/**
		Return the mapped object associated with _id.
		Will automatically cast object to _clazz.
	*/
	public <T> T getUniform( final String _id, final Class<T> _clazz )
	{
		return _clazz.cast( getUniform( _id ) ) ;
	}

	public Map<String, Object> getUniformMap()
	{
		return uniforms ;
	}

	public boolean removeStorage( final String _handler )
	{
		if( storages.remove( _handler ) != null )
		{
			return true ;
		}

		// Failed to remove an object associated with 
		// the passed in id - most likely never set.
		return false ;
	}

	public boolean mapStorage( final String _handler, final Storage _obj )
	{
		if( _handler == null || _obj == null )
		{
			// The id or value cannot be null
			return false ;
		}

		if( _obj == storages.get( _handler ) )
		{
			// Attempting reassign to the same object.
			return false ;
		}

		storages.put( _handler, _obj ) ;
		return true ;
	}

	public Storage getStorage( final String _id )
	{
		return storages.get( _id ) ;
	}

	public String getID()
	{
		return id ;
	}

	public int index()
	{
		return index ;
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

		if( _obj instanceof Program )
		{
			// We don't compare the index as that is unique 
			// to an instance of program.

			final Program program = ( Program )_obj ;
			if( id.equals( program.id ) == false )
			{
				return false ;
			}

			final Map<String, Object> u = program.uniforms ;
			if( uniforms.size() != u.size() )
			{
				// If the hashmaps are not the same size then 
				// no point in continuing.
				return false ;
			}

			final Map<String, Storage> b = program.storages ;
			if( storages.size() != b.size() )
			{
				return false ;
			}

			{
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
			
			{
				final Set<Map.Entry<String, Storage>> entries = storages.entrySet() ;
				if( entries.isEmpty() == false )
				{
					for( final Map.Entry<String, Storage> entry : entries )
					{
						final Storage obj1 = entry.getValue() ;
						final Storage obj2 = b.get( entry.getKey() ) ;

						if( obj1.equals( obj2 ) == false )
						{
							return false ;
						}
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
