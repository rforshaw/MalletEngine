package com.linxonline.mallet.renderer ;

public final class ArrayUniform implements IUniform
{
	private final IUniform[] uniforms ;

	public ArrayUniform( final int _size )
	{
		uniforms = new IUniform[_size] ;
	}

	public boolean set( final int _i, final IUniform _uniform )
	{
		uniforms[_i] = _uniform ;
		return true ;
	}

	public IUniform get( final int _i )
	{
		return uniforms[_i] ;
	}

	public int length()
	{
		return uniforms.length ;
	}

	public boolean forEach( final String _prefix, final IEach _func )
	{
		final StringBuilder builder = new StringBuilder() ;

		final int size = uniforms.length ;
		for( int i = 0; i < size; ++i )
		{
			builder.setLength( 0 ) ;
			builder.append( _prefix ) ;
			builder.append( '[' ) ;
			builder.append( i ) ;
			builder.append( ']' ) ;

			final IUniform uniform = uniforms[i] ;

			final boolean success = switch( uniform )
			{
				case StructUniform su ->
				{
					builder.append( '.' ) ;
					yield su.forEach( builder.toString(), _func ) ;
				}
				case ArrayUniform au ->
				{
					yield au.forEach( builder.toString(), _func ) ;
				}
				default ->
				{
					final String absoluteName = builder.toString() ;
					_func.each( absoluteName, uniform ) ;
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
}
