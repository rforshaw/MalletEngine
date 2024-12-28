package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.Map ;

import java.io.InputStream ;

import java.awt.GraphicsEnvironment ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.desktop.* ;

import com.linxonline.mallet.renderer.desktop.opengl.GLFontGenerator.Bundle ;
import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.Logger ;

public final class GLFontManager extends AbstractManager<String, GLFont>
{
	private final static String CHARACTERS = "\0 []{}:;'@~#<>,/?|`-=¬abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"£$%^&*()_+." ;

	private final GLFontGenerator gen ;
	private final GLTextureManager manager ;
	private final Map<String, Font.Metrics> metrics = MalletMap.<String, Font.Metrics>newMap() ;

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

	public GLFont get( final Font _font )
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
			final GLImage image = manager.createGLImage( bundle.image, GLTextureManager.InternalFormat.UNCOMPRESSED ) ;
			final GLFont font = new GLFont( bundle.shapes, image ) ;
			put( id, font ) ;
			return font ;
		}

		return null ;
	}

	public String[] loadFont( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		if( file.exists() == false )
		{
			Logger.println( "Failed to load font: " + _file, Logger.Verbosity.NORMAL ) ;
			return new String[0] ;
		}

		try( final DesktopByteIn in = ( DesktopByteIn )file.getByteInStream() )
		{
			final InputStream stream = in.getInputStream() ;
			final java.awt.Font font = java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, stream ) ;

			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
			if( ge.registerFont( font ) == false )
			{
				return new String[0] ;
			}

			final String[] names = new String[2] ;
			names[0] = font.getFontName() ;
			names[1] = font.getFamily() ;
			return names ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return new String[0] ;
		}
	}

	public Font.Metrics generateMetrics( final Font _font )
	{
		final String id = _font.getID() ;
		if( metrics.containsKey( id ) == true )
		{
			return metrics.get( id ) ;
		}

		final Font.Metrics met =  gen.generateMetrics( _font.getFontName(),
															 _font.getStyle(),
															 _font.getPointSize(),
															 CHARACTERS ) ;
		metrics.put( id, met ) ;
		return met ;
	}

	public Glyph generateGlyph( final Font _font, final int _code )
	{
		remove( _font.getID() ) ;
		return gen.generateGlyph( _font.getFontName(),
								  _font.getStyle(),
								  _font.getPointSize(),
								  _code ) ;
	}

	protected Bundle createResource( final Font _font )
	{
		// Generate the Glyphs for the passed in characters
		return gen.generateFont( _font ) ;
	}
}
