package com.linxonline.mallet.renderer.GL ;

import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.Resource ;

public class GLFontManager extends AbstractManager
{
	private final GLFontGenerator gen ;

	public GLFontManager( final GLTextureManager _manager )
	{
		gen = new GLFontGenerator( _manager ) ;
	}

	public Resource get( final String _name, final int _size )
	{
		if( exists( _name ) == true )
		{
			return resources.get( _name ) ;
		}

		final Resource resource = createResource( _name, _size ) ;
		if( resource != null )
		{
			add( _name, resource ) ;
			resource.register() ;
		}

		return resource ;
	}

	@Override
	protected GLFontMap createResource( final String _name )
	{
		System.out.println( "Not implemented, use get( String _name, int _size ) instead." ) ;
		return null ;
	}

	protected GLFontMap createResource( final String _name, final int _size )
	{
		GLFontMap fontMap = loadFontMap( _name, _size ) ;
		if( fontMap != null )
		{
			resources.put( _name, fontMap ) ;
			return fontMap ;
		}

		return null ;
	}

	protected GLFontMap loadFontMap( final String _name, final int _size )
	{
		return gen.generateFontMap( _name, _size, " []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+.", 5 ) ;
	}
}