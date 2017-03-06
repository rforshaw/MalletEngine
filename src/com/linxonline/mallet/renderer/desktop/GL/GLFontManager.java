package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.util.settings.Settings ;

public class GLFontManager extends AbstractManager<GLFont>
{
	private final static String CHARACTERS = "\0 []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+." ;

	private final GLFontGenerator gen ;

	public GLFontManager( final GLTextureManager _manager )
	{
		gen = new GLFontGenerator( _manager ) ;
	}

	/**
		Use to load a ttf font into the system.
	*/
	@Override
	public GLFont get( final String _file )
	{
		System.out.println( "GLFontManager: get( _file ). Not implemented yet." ) ;
		assert( true ) ;
		return null ;
	}

	public GLFont get( final MalletFont _font )
	{
		final String id = _font.getID() ;
		if( exists( id ) == true )
		{
			return resources.get( id ) ;
		}

		final GLFont resource = createResource( _font ) ;
		if( resource != null )
		{
			put( id, resource ) ;
		}

		return resource ;
	}

	public MalletFont.Metrics generateMetrics( final String _font, final int _style, final int _size )
	{
		return gen.generateMetrics( _font, _style, _size, CHARACTERS ) ;
	}

	protected GLFont createResource( final MalletFont _font )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFont( _font ) ;
	}
}
