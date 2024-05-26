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
		if( IUniform.Type.validate( _uniform ) == false )
		{
			// Only certain classes that implement IUniform
			// are considered valid.
			return false ;
		}

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

	public void forEach( final String _prefix, final IEach _func )
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

			switch( uniform.getType() )
			{
				default     :
				{
					final String absoluteName = builder.toString() ;
					_func.each( absoluteName, uniform ) ;
					break ;
				}
				case STRUCT :
				{
					builder.append( '.' ) ;
					( ( StructUniform )uniform ).forEach( builder.toString(), _func ) ;
					break ;
				}
				case ARRAY  :
				{
					( ( ArrayUniform )uniform ).forEach( builder.toString(), _func ) ;
					break ;
				}
			}
		}
	}

	@Override
	public final IUniform.Type getType()
	{
		return IUniform.Type.ARRAY ;
	}
}
