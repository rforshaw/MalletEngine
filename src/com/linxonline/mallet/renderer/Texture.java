package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Ratio ;
import com.linxonline.mallet.maths.IntVector2 ;

/**
	Provides access to the Meta information of a texture
	without the requirement to manage a texture buffer.
	Provides meta data to the developer without the 
	need to access Renderer/Platform specific texture 
	implementations, or wait for the Renderer to load 
	the requested texture.
*/
public final class Texture implements IUniform
{
	private final Meta meta ;

	private Filter minification ;
	private Filter magnification ;
	private Wrap uWrap ;
	private Wrap vWrap ;
	
	/**
		Construct a Texture that uses a resource 
		from the file-system as a texture object.
	*/
	public Texture( final String _texturePath )
	{
		this( _texturePath, Filter.MIP_LINEAR, Wrap.REPEAT ) ;
	}

	public Texture( final String _texturePath, final Filter _filter, final Wrap _wrap )
	{
		this( TextureAssist.createMeta( _texturePath ), _filter, _wrap ) ;
	}

	public Texture( final String _texturePath,
					final Filter _minification,
					final Filter _magnification,
					final Wrap _u,
					final Wrap _v )
	{
		this( TextureAssist.createMeta( _texturePath ), _minification, _magnification, _u, _v ) ;
	}

	/**
		Construct a Texture that uses the World's
		framebuffer as a texture object.
	*/
	public Texture( final World _world )
	{
		// If no attachment index is specified always use the 
		// first attachment meta.
		// World backbuffer is set to NEAREST REPEAT.
		this( _world, 0 ) ;
	}

	/**
		Construct a Texture that uses the World's
		framebuffer as a texture object.
	*/
	public Texture( final World _world, final int _attachmentIndex )
	{
		// World backbuffer is set to NEAREST REPEAT.
		this( _world, Filter.NEAREST, _attachmentIndex ) ;
	}

	public Texture( final World _world, final Filter _filter )
	{
		this( _world, _filter, 0 ) ;
	}

	public Texture( final World _world, final Filter _filter, final int _attachmentIndex )
	{
		this( _world.getMeta( _attachmentIndex ), _filter, Wrap.REPEAT ) ;
	}

	private Texture( final Meta _meta,
					 final Filter _filter,
					 final Wrap _wrap )
	{
		this( _meta, _filter, _filter, _wrap, _wrap ) ;
	}

	private Texture( final Meta _meta,
					 final Filter _minification, 
					 final Filter _magnification,
					 final Wrap _u,
					 final Wrap _v )
	{
		meta = _meta ;
		minification = ( _minification == null ) ? Filter.MIP_LINEAR : _minification ;
		magnification = ( _magnification == null ) ? Filter.MIP_LINEAR : _magnification ;

		uWrap = ( _u == null ) ? Wrap.REPEAT : _u ;
		vWrap = ( _v == null ) ? Wrap.REPEAT : _v ;
	}

	public void setFilter( final Filter _min, final Filter _mag )
	{
		minification = ( _min == null ) ? Filter.MIP_LINEAR : _min ;
		magnification = ( _mag == null ) ? Filter.MIP_LINEAR : _mag ;
	}

	public void setWrap( final Wrap _u, final Wrap _v )
	{
		uWrap = ( _u == null ) ? Wrap.REPEAT : _u ;
		vWrap = ( _v == null ) ? Wrap.REPEAT : _v ;
	}

	/**
		Mallet Textures are considered equal 
		if they share the same file path.
	*/
	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj instanceof Texture rhs )
		{ 
			if( uWrap != rhs.uWrap || vWrap != rhs.vWrap )
			{
				return false ;
			}
			else if( minification != rhs.minification || magnification != rhs.magnification )
			{
				return false ;
			}
			return meta.equals( rhs.meta ) ;
		}

		return false ;
	}

	@Override
	public int hashCode()
	{
		return meta.hashCode() ;
	}

	public String getPath()
	{
		return meta.path ;
	}

	public int getHeight()
	{
		return meta.getHeight() ;
	}

	/**
		Using the UVMap co-ordinates that assume a mapping 
		between 0.0 and 1.0 determine what the sub-height would be.

		If _minUV was 0.0 and _maxUV was 1.0 the value returned 
		would effectively be getHeight().
		
		If _minUV was 0.5 and _maxUV was 1.0 the value returned 
		would effectively be half of getHeight() (height / 2).
	*/
	public int getSubHeight( final float _minUV, final float _maxUV )
	{
		return Math.abs( ( int )( ( _maxUV - _minUV ) * getHeight() ) ) ;
	}

	public int getWidth()
	{
		return meta.getWidth() ;
	}

	/**
		Using the UVMap co-ordinates that assume a mapping 
		between 0.0 and 1.0 determine what the sub-width would be.

		If _minUV was 0.0 and _maxUV was 1.0 the value returned 
		would effectively be getWidth().
		
		If _minUV was 0.5 and _maxUV was 1.0 the value returned 
		would effectively be half of getWidth() (width / 2).
	*/
	public int getSubWidth( final float _minUV, final float _maxUV )
	{
		return Math.abs( ( int )( ( _maxUV - _minUV ) * getWidth() ) ) ;
	}

	public Ratio getRatio()
	{
		return meta.ratio ;
	}

	public Meta getMeta()
	{
		return meta ;
	}

	public Wrap getUWrap()
	{
		return uWrap ;
	}

	public Wrap getVWrap()
	{
		return vWrap ;
	}

	public Filter getMinificationFilter()
	{
		return minification ;
	}

	public Filter getMaxificationFilter()
	{
		return magnification ;
	}

	@Override
	public String toString()
	{
		return meta.toString() ;
	}

	public static enum Wrap
	{
		REPEAT,
		CLAMP_EDGE
	}

	public static enum Filter
	{
		MIP_LINEAR,
		MIP_NEAREST,
		LINEAR,
		NEAREST
	}

	public static class Meta
	{
		private final String path ;
		public final IntVector2 dimensions = new IntVector2() ;
		private final Ratio ratio ;
		private final int attachmentIndex ;

		public Meta( final String _path,
					 final int _width,
					 final int _height )
		{
			this( _path, -1, _width, _height ) ;
		}

		public Meta( final String _path,
					 final int _attachmentIndex,
					 final int _width,
					 final int _height )
		{
			path = _path ;
			set( _width, _height ) ;
			ratio = Ratio.calculateRatio( _width, _height ) ;
			attachmentIndex = _attachmentIndex ;
		}

		public void set( final int _width, final int _height )
		{
			dimensions.setXY( _width, _height ) ;
		}

		@Override
		public boolean equals( final Object _obj )
		{
			if( _obj instanceof Meta meta )
			{
				return path.equals( meta.path ) ;
			}

			return false ;
		}

		public int getWidth()
		{
			return dimensions.x ;
		}

		public int getHeight()
		{
			return dimensions.y ;
		}

		public String getPath()
		{
			return path ;
		}

		public Ratio getRatio()
		{
			return ratio ;
		}

		public int getAttachmentIndex()
		{
			return attachmentIndex ;
		}

		@Override
		public int hashCode()
		{
			return path.hashCode() ;
		}

		@Override
		public String toString()
		{
			final StringBuilder buffer = new StringBuilder() ;
			buffer.append( "Path: " + path + '\n' ) ;
			buffer.append( "Height: " + getHeight() + '\n' ) ;
			buffer.append( "Width: " + getWidth() + '\n') ;
			buffer.append( "Ratio: " + ratio ) ;
			return buffer.toString() ;
		}
	}
}
