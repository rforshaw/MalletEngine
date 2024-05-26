package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public final class UniformList
{
	private final List<String> uniforms = MalletList.<String>newList() ;

	public boolean remove( final String _handler )
	{
		final int size = uniforms.size() ;
		for( int i = 0; i < size; ++i )
		{
			final String name = uniforms.get( i ) ;
			if( _handler.equals( name ) )
			{
				uniforms.remove( i ) ;
				return true ;
			}
		}

		// Failed to remove an object associated with 
		// the passed in id - most likely never set.
		return false ;
	}

	public boolean add( final String _handler )
	{
		if( _handler == null )
		{
			// The id or value cannot be null
			return false ;
		}

		uniforms.add( _handler ) ;
		return true ;
	}

	public String get( final int _index )
	{
		return uniforms.get( _index ) ;
	}

	public int size()
	{
		return uniforms.size() ;
	}

	public boolean isEmpty()
	{
		return uniforms.isEmpty() ;
	}
}
