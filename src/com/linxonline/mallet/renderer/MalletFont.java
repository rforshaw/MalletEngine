package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.QuickSort ;
import com.linxonline.mallet.maths.MathUtil ;

import com.linxonline.mallet.renderer.font.FontAssist ;
import com.linxonline.mallet.renderer.font.Glyph ;

public class MalletFont
{
	public static final int PLAIN = 5 ;

	private final String name ;				// The Family Font Name
	private final int style ;				// Italic, Bold, Plain, currently only Plain is supported
	private final int size ;				// Text point size
	private final String id ;
	private final Metrics metrics ;
	
	public MalletFont( final String _name, final int _style, final int _size )
	{
		name = _name ;
		style = _style ;
		size = _size ;

		id = name + size + style ;

		metrics = FontAssist.createMetrics( this ) ;
		metrics.setParent( this ) ;
	}

	public MalletFont( final String _name, final int _size )
	{
		this( _name, PLAIN, _size ) ;
	}

	public MalletFont( final String _name )
	{
		this( _name, 12 ) ;
	}

	public static MalletFont createByPixel( final String _name, final int _style, final int _pixels )
	{
		final int pt = ( int )MathUtil.linear( 6, 12, 8, 16, _pixels ) ;
		return createByPoint( _name, _style, pt ) ;
	}

	public static MalletFont createByPoint( final String _name, final int _style, final int _pt )
	{
		return new MalletFont( _name, _style, _pt ) ;
	}
	
	public String getFontName()
	{
		return name ;
	}

	public int getStyle()
	{
		return style ;
	}

	public int getPointSize()
	{
		return size ;
	}

	public int stringIndexWidth( final StringBuilder _text, final float _width )
	{
		float width = 0.0f ;

		final int length = _text.length() ;
		for( int i = 0; i < length; ++i )
		{
			width += metrics.getGlyphWithChar( _text.charAt( i ) ).getWidth() ;
			if( width > _width )
			{
				return ( i > 0 ) ? i - 1 : 0 ;
			}
		}
		
		return length ;
	}

	public int stringIndexWidth( final String _text, final float _width )
	{
		float width = 0.0f ;

		final int length = _text.length() ;
		for( int i = 0; i < length; ++i )
		{
			width += metrics.getGlyphWithChar( _text.charAt( i ) ).getWidth() ;
			if( width > _width )
			{
				return ( i > 0 ) ? i - 1 : 0 ;
			}
		}
		
		return length ;
	}

	public float stringWidth( final StringBuilder _text )
	{
		return stringWidth( _text, 0, _text.length() ) ;
	}

	public float stringWidth( final StringBuilder _text, final int _start, final int _end )
	{
		float width = 0.0f ;

		final int length = _text.length() ;
		for( int i = _start; i < _end; ++i )
		{
			width += metrics.getGlyphWithChar( _text.charAt( i ) ).getWidth() ;
		}
		
		return width ;
	}

	public float stringWidth( final String _text )
	{
		return stringWidth( _text, 0, _text.length() ) ;
	}

	public float stringWidth( final String _text, final int _start, final int _end )
	{
		float width = 0.0f ;

		for( int i = _start; i < _end; ++i )
		{
			width += metrics.getGlyphWithChar( _text.charAt( i ) ).getWidth() ;
		}

		return width ;
	}

	public Metrics getMetrics()
	{
		return metrics ;
	}

	public String getID()
	{
		return id ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof MalletFont )
		{
			final MalletFont font = ( MalletFont )_obj ;
			return id.equals( font.id ) ;
		}

		return false ;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode() ;
	}

	public static class Metrics
	{
		private MalletFont parent ;

		private Glyph[] glyphs ;
		private final float height ;
		private final float ascent ;
		private final float descent ;
		private final float leading ;

		public Metrics( final Glyph[] _glyphs,
						final float _height,
						final float _ascent,
						final float _descent,
						final float _leading )
		{
			final Glyph[] temp = QuickSort.quicksort( _glyphs ) ;
			final int size = temp.length ;
			final int largestCode = temp[size - 1].sortValue() ;

			// Create a map based on the glyphs char number.
			glyphs = new Glyph[largestCode + 1] ;
			for( int i = 0; i < size; ++i)
			{
				final int code = temp[i].sortValue() ;
				glyphs[code] = temp[i] ;
			}

			height = _height ;
			ascent = _ascent ;
			descent = _descent ;
			leading = _leading ;
		}

		private void setParent( final MalletFont _parent )
		{
			parent = _parent ;
		}

		public float getHeight()
		{
			return height ;
		}

		public float getAscent()
		{
			return ascent ;
		}

		public float getDescent()
		{
			return descent ;
		}

		public float getLeading()
		{
			return leading ;
		}

		public Glyph[] getGlyphs()
		{
			return glyphs ;
		}

		public Glyph getGlyphWithChar( final char _char )
		{
			return getGlyphWithCode( ( int )_char ) ;
		}

		public Glyph getGlyphWithCode( final int _code )
		{
			if( _code < glyphs.length )
			{
				final Glyph glyph = glyphs[_code] ;
				if( glyph != null )
				{
					return glyph ;
				}
			}

			acquireNewGlyph( _code ) ;
			return getGlyphWithCode( _code ) ;
		}

		private void acquireNewGlyph( final int _code )
		{
			//System.out.println( "Acquiring Code: " + _code + " Char: " + ( char )_code + " For: " + parent.getID() ) ;
			if( _code >= glyphs.length )
			{
				// We need to extend the glyph array
				// to support the requested glyph.
				final Glyph[] temp = glyphs ;
				glyphs = new Glyph[_code + 1] ;

				//System.out.println( "Copying Temp Glyphs: " + temp.length + " to: " + glyphs.length ) ;
				final int size = temp.length ;
				for( int i = 0; i < size; i++ )
				{
					glyphs[i] = temp[i] ;
				}
			}

			glyphs[_code] = FontAssist.createGlyph( parent, _code ) ;
		}
	}
}
