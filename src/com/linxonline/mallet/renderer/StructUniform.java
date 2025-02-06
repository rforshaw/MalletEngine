package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public final class StructUniform implements IUniform
{
	private final List<Pair> uniforms = MalletList.<Pair>newList() ;

	public boolean remove( final String _handler )
	{
		final int size = uniforms.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Pair pair = uniforms.get( i ) ;
			if( _handler.equals( pair.name ) )
			{
				uniforms.remove( i ) ;
				return true ;
			}
		}

		// Failed to remove an object associated with 
		// the passed in id - most likely never set.
		return false ;
	}

	public boolean map( final String _handler, final IUniform _uniform )
	{
		if( _handler == null || _uniform == null )
		{
			// The id or value cannot be null
			return false ;
		}

		final Pair found = getPair( _handler ) ;
		if( found != null && _uniform == found.uniform )
		{
			// Attempting reassign to the same object. 
			return false ;
		}

		if( found != null )
		{
			found.uniform =_uniform ;
			return true ;
		}

		uniforms.add( new Pair( _handler, _uniform ) ) ;
		return true ;
	}

	/**
		Add the uniform to the end of the array-list.
		This is used by draw-uniforms where order
		becomes important.
	*/
	public boolean add( final String _handler, final IUniform _uniform )
	{
		if( _handler == null || _uniform == null )
		{
			// The id or value cannot be null
			return false ;
		}

		uniforms.add( new Pair( _handler, _uniform ) ) ;
		return true ;
	}

	public IUniform get( final int _index )
	{
		return uniforms.get( _index ).uniform ;
	}

	public IUniform get( final String _id )
	{
		final Pair pair = getPair( _id ) ;
		return ( pair != null ) ? pair.uniform : null ;
	}

	public boolean forEach( final IEach _func )
	{
		return forEach( "", _func ) ;
	}

	/**
		Navigate the entire tree structure, constructing the
		absolute name of each uniforms as we go.
	*/
	public boolean forEach( final String _prefix, final IEach _func )
	{
		final StringBuilder builder = new StringBuilder() ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; ++i )
		{
			builder.setLength( 0 ) ;
			builder.append( _prefix ) ;

			final StructUniform.Pair pair = uniforms.get( i ) ;

			builder.append( pair.name ) ;
			final IUniform uniform = pair.uniform ;

			final boolean success = switch( uniform )
			{
				case StructUniform su ->
				{
					builder.append( '.' ) ;
					yield su.forEach( builder.toString(), _func ) ;
				}
				case ArrayUniform au ->
				{
					au.forEach( builder.toString(), _func ) ;
					yield true ;
				}
				default ->
				{
					final String absoluteName = builder.toString() ;
					if( _func.each( absoluteName, uniform ) == false )
					{
						yield false ;
					}

					yield true ;
				}
			} ;

			if( !success )
			{
				return false ;
			}
		}

		return true ;
	}

	public int size()
	{
		return uniforms.size() ;
	}

	public boolean isEmpty()
	{
		return uniforms.isEmpty() ;
	}

	private Pair getPair( final String _id )
	{
		final int size = uniforms.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Pair pair = uniforms.get( i ) ;
			if( _id.equals( pair.name ) )
			{
				return pair ;
			}
		}

		return null ;
	}

	@Override
	public int hashCode()
	{
		return uniforms.hashCode() ;
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

		if( _obj instanceof StructUniform )
		{
			final StructUniform struct = ( StructUniform )_obj ;
			final int size = uniforms.size() ;
			if( size != struct.uniforms.size() )
			{
				return false ;
			}

			for( int i = 0; i < size; ++i )
			{
				final Pair a = uniforms.get( i ) ;
				final Pair b = struct.uniforms.get( i ) ;

				if( a.equals( b ) == false )
				{
					return false ;
				}
			}

			return true ;
		}

		return false ;
	}

	private static final class Pair
	{
		public final String name ;
		public IUniform uniform ;

		public Pair( final String _name, final IUniform _uniform )
		{
			name = _name ;
			uniform = _uniform ;
		}

		@Override
		public int hashCode()
		{
			return name.hashCode() ;
		}

		@Override
		public boolean equals( final Object _obj )
		{
			if( _obj instanceof Pair )
			{
				final Pair pair = ( Pair )_obj ;
				return uniform.equals( pair.uniform ) ;
			}

			return false ;
		}
	}
}
