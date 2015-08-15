package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Tuple ;

public class GLFontManager extends AbstractManager<GLFontMap>
{
	private final GLFontGenerator gen ;

	public GLFontManager( final GLTextureManager _manager )
	{
		gen = new GLFontGenerator( _manager ) ;
	}

	@Override
	public GLFontMap get( final String _key, final Settings _settings )
	{
		System.out.println( "GLFontManager: get( _key, _settings ). Not implemented yet." ) ;
		return null ;
	}

	@Override
	public GLFontMap get( final String _key, final String _file )
	{
		System.out.println( "GLFontManager: get( _key, _file ). Not implemented yet." ) ;
		return null ;
	}

	@Override
	public GLFontMap get( final String _file )
	{
		System.out.println( "GLFontManager: get( _file ). Not implemented yet." ) ;
		return null ;
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

	public GLFontMap generateFontGeometry( final MalletFont _font )
	{
		return gen.generateFontGeometry( _font, ( GLFontMap )_font.font.getFont() ) ;
	}

	protected GLFontMap createResource( final String _name, final int _size )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFontMap( _name, _size, "\0 []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+.", 5 ) ;
	}

	/**
		OpenGL Context can be lost on Android devices.
		Remove references to previous context data, so 
		it can be reloaded during rendering.
	*/
	public void recover()
	{
		for( final GLFontMap map : resources.values() )
		{
			map.fontMap.setTexture( null ) ;
		}
	}
}