package com.linxonline.mallet.renderer ;

import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.util.MalletMap ;

public final class Program
{
	private final static Attribute[] DEFAULT_ATTRIBUTES = new Attribute[]
	{
		new Attribute( "inVertex", Shape.Attribute.VEC3 ),
		new Attribute( "inColour", Shape.Attribute.FLOAT ),
		new Attribute( "inTexCoord0", Shape.Attribute.VEC2 )
	} ;

	private final static Utility utility = new Utility() ;

	private final int index ;
	private final String id ;

	private final StructUniform uniforms = new StructUniform() ;
	private final UniformList drawUniforms = new UniformList() ;
	private final Map<String, Storage> storages = MalletMap.<String, Storage>newMap() ;

	private IShape.Style style = IShape.Style.FILL ;
	private Attribute[] attributes = DEFAULT_ATTRIBUTES ;

	public Program( final String _id )
	{
		this( _id, DEFAULT_ATTRIBUTES ) ;
	}

	public Program( final String _id, final Attribute[] _attributes )
	{
		this( _id, IShape.Style.FILL, _attributes ) ;
	}

	public Program( final String _id, final IShape.Style _style, final Attribute[] _attributes )
	{
		index = utility.getGlobalIndex() ;

		id = _id ;
		attributes = ( _attributes != null ) ? _attributes : attributes ;
		style = ( _style != null ) ? _style : style ;
	}

	public IShape.Style getStyle()
	{
		return style ;
	}

	public Attribute[] getAttributes()
	{
		return attributes ;
	}

	public boolean removeUniform( final String _handler )
	{
		return uniforms.remove( _handler ) ;
	}

	/**
		Map a uniform that the Program is expected
		to provide.
		Program uniforms can support hierarchical structures
		and arrays of structures.
	*/
	public boolean mapUniform( final String _handler, final IUniform _uniform )
	{
		return uniforms.map( _handler, _uniform ) ;
	}

	/**
		Return the mapped object associated with _id.
	*/
	public IUniform getUniform( final String _id )
	{
		return uniforms.get( _id ) ;
	}

	public boolean forEachUniform( IUniform.IEach _func )
	{
		return uniforms.forEach( _func ) ;
	}

	public UniformList getDrawUniforms()
	{
		return drawUniforms ;
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
		if( _obj instanceof Program program )
		{
			// We don't compare the index as that is unique 
			// to an instance of program.

			if( id.equals( program.id ) == false )
			{
				return false ;
			}

			if( style.equals( program.style ) == false )
			{
				return false ;
			}

			if( uniforms.equals( program.uniforms ) == false )
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

			if( isCompatibleAttribute( attributes, program.attributes ) == false )
			{
				return false ;
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

	/**
		Return true if the Geometry contains the
		same attribute-types as the program requires.
	*/
	public boolean isCompatible( final IShape.Attribute[] _b )
	{
		if( attributes.length != _b.length )
		{
			return false ;
		}

		for( int i = 0; i < _b.length; ++i )
		{
			if( attributes[i].type != _b[i] )
			{
				return false ;
			}
		}

		return true ;
	}

	protected static boolean isCompatibleAttribute( final Attribute[] _a, final Attribute[] _b )
	{
		if( _a.length != _b.length )
		{
			return false ;
		}

		for( int i = 0; i < _a.length; ++i )
		{
			if( _a[i].isCompatible( _b[i] ) == false )
			{
				return false ;
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
