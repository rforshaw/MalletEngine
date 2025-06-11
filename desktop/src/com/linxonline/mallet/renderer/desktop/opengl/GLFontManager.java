package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.Map ;

import java.io.InputStream ;
import java.io.IOException ;

import com.jogamp.opengl.GLProfile ;
import com.jogamp.graph.font.FontFactory ;
import com.jogamp.graph.font.FontSet ;

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
	private final GLProfile glProfile ;
	private final GLTextureManager manager ;
	
	private final Map<String, com.jogamp.graph.font.Font> fonts = MalletMap.<String, com.jogamp.graph.font.Font>newMap() ;
	private final Map<String, Font.Metrics> metrics = MalletMap.<String, Font.Metrics>newMap() ;

	public GLFontManager( final GLProfile _profile, final GLTextureManager _manager )
	{
		gen = new GLFontGenerator() ;
		glProfile = _profile ;
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

		final String name = _font.getFontName() ;
		if( exists( name ) == true )
		{
			return resources.get( name ) ;
		}

		final Bundle bundle = createResource( _font ) ;
		if( bundle != null )
		{
			final GLFont font = new GLFont( bundle.shapes ) ;
			put( name, font ) ;
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
			final com.jogamp.graph.font.Font font = FontFactory.get( stream, false ) ;

			final String[] names = new String[2] ;
			names[0] = font.getName( com.jogamp.graph.font.Font.NAME_MANUFACTURER ) ;
			names[1] = font.getName( com.jogamp.graph.font.Font.NAME_FAMILY ) ;

			fonts.put( names[0], font ) ;

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

		final String name = _font.getFontName() ;
		com.jogamp.graph.font.Font font = fonts.get( name ) ;
		if( font == null )
		{
			try
			{
				final FontSet set = FontFactory.getDefault() ;
				font = set.getDefault() ;
				fonts.put( name, font ) ;
			}
			catch( final IOException ex )
			{
				Logger.println( "Failed to generate metrics for: " + name, Logger.Verbosity.MAJOR ) ;
				throw new RuntimeException( ex ) ;
			}
		}

		final Font.Metrics met =  gen.generateMetrics( _font, font, CHARACTERS ) ;
		metrics.put( id, met ) ;
		return met ;
	}

	public Glyph generateGlyph( final Font _font, final int _code )
	{
		remove( _font.getID() ) ;
		final String name = _font.getFontName() ;
		final com.jogamp.graph.font.Font font = fonts.get( name ) ;
		return gen.generateGlyph( _font, font, _code ) ;
	}

	protected Bundle createResource( final Font _font )
	{
		Logger.println( "Create Resource: " + _font.getFontName(), Logger.Verbosity.MAJOR ) ;
		final String name = _font.getFontName() ;
		final com.jogamp.graph.font.Font font = fonts.get( name ) ;
		return gen.generateFont( _font, font ) ;
	}
}
