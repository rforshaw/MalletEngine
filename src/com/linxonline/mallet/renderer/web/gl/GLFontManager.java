package com.linxonline.mallet.renderer.web.gl ;

import java.util.Map ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.util.MalletMap ;

public class GLFontManager extends AbstractManager<String, GLFont>
{
	private final static String CHARACTERS = "\0 []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+." ;

	private final GLFontGenerator gen ;
	private final GLTextureManager manager ;
	private final Map<String, MalletFont.Metrics> metrics = MalletMap.<String, MalletFont.Metrics>newMap() ;

	public GLFontManager( final GLTextureManager _manager )
	{
		gen = new GLFontGenerator() ;
		manager = _manager ;
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
		clean() ;

		final String id = _font.getID() ;
		if( exists( id ) == true )
		{
			return resources.get( id ) ;
		}

		final GLFontGenerator.Bundle bundle = createResource( _font ) ;
		if( bundle != null )
		{
			final GLImage image = manager.bind( bundle.canvas, GLTextureManager.InternalFormat.UNCOMPRESSED ) ;
			final GLFont font = new GLFont( bundle.shapes, image ) ;
			put( id, font ) ;
			return font ;
		}

		return null ;
	}

	public String[] loadFont( final String _file )
	{
		return new String[0] ;
	}
	
	public MalletFont.Metrics generateMetrics( final MalletFont _font )
	{
		final String id = _font.getID() ;
		if( metrics.containsKey( id ) == true )
		{
			return metrics.get( id ) ;
		}

		final MalletFont.Metrics met =  gen.generateMetrics( _font.getFontName(),
															 _font.getStyle(),
															 _font.getPointSize(),
															 CHARACTERS ) ;
		metrics.put( id, met ) ;
		return met ;
	}

	public Glyph generateGlyph( final MalletFont _font, final int _code )
	{
		remove( _font.getID() ) ;
		return gen.generateGlyph( _font.getFontName(),
								  _font.getStyle(),
								  _font.getPointSize(),
								  _code ) ;
	}

	protected GLFontGenerator.Bundle createResource( final MalletFont _font )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFont( _font ) ;
	}
}
