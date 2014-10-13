package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.Resource ;

public class GLFontManager extends AbstractManager<GLFontMap>
{
	private final GLFontGenerator gen ;

	public GLFontManager( final GLTextureManager _manager )
	{
		gen = new GLFontGenerator( _manager ) ;
	}

	public GLFontMap get( final String _name, final int _size )
	{
		final String id = _name + _size ;
		if( exists( id ) == true )
		{
			return resources.get( id ) ;
		}

		final GLFontMap resource = createResource( _name, _size ) ;
		if( resource != null )
		{
			add( id, resource ) ;
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
		final GLFontMap fontMap = loadFontMap( _name, _size ) ;
		if( fontMap != null )
		{
			return fontMap ;
		}

		return null ;
	}

	protected GLFontMap loadFontMap( final String _name, final int _size )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFontMap( _name, _size, "\0 []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+.", 5 ) ;
	}
}