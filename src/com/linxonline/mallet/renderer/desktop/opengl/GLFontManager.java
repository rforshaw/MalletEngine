package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.renderer.desktop.opengl.GLFontGenerator.Bundle ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.Tuple ;

public class GLFontManager extends AbstractManager<GLFont>
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

		final Bundle bundle = createResource( _font ) ;
		if( bundle != null )
		{
			final GLFont font = new GLFont( bundle.shapes, manager.bind( bundle.image, GLTextureManager.InternalFormat.UNCOMPRESSED ) ) ;
			put( id, font ) ;
			return font ;
		}

		return null ;
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
		//System.out.println( "Create: " + ( char )_code ) ;
		return gen.generateGlyph( _font.getFontName(),
								  _font.getStyle(),
								  _font.getPointSize(),
								  _code ) ;
	}

	protected Bundle createResource( final MalletFont _font )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFont( _font ) ;
	}
}