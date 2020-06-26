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
public class MalletTexture
{
	private final Filter minFilter ;
	private final Filter maxFilter ;
	private final Wrap uWrap ;
	private final Wrap vWrap ;
	private final Meta meta ;

	/**
		Construct a MalletTexture that uses a resource 
		from the file-system as a texture object.
	*/
	public MalletTexture( final String _texturePath )
	{
		this( _texturePath, Filter.MIP_LINEAR, Wrap.REPEAT ) ;
	}

	public MalletTexture( final String _texturePath, final Filter _filter, final Wrap _wrap )
	{
		this( TextureAssist.createMeta( _texturePath ), _filter, _wrap ) ;
	}

	public MalletTexture( final String _texturePath,
						  final Filter _min,
						  final Filter _max,
						  final Wrap _u,
						  final Wrap _v )
	{
		this( TextureAssist.createMeta( _texturePath ), _min, _max, _u, _v ) ;
	}

	/**
		Construct a MalletTexture that uses the World's
		framebuffer as a texture object.
	*/
	public MalletTexture( final World _world )
	{
		// World backbuffer is set to NEAREST REPEAT.
		this( TextureAssist.createMeta( _world ), Filter.NEAREST, Wrap.REPEAT ) ;
	}

	private MalletTexture( final Meta _meta,
						   final Filter _filter,
						   final Wrap _wrap )
	{
		this( _meta, _filter, _filter, _wrap, _wrap ) ;
	}
	
	private MalletTexture( final Meta _meta,
						   final Filter _min, 
						   final Filter _max,
						   final Wrap _u,
						   final Wrap _v )
	{
		meta = _meta ;
		minFilter = ( _min == null ) ? Filter.MIP_LINEAR : _min ;
		maxFilter = ( _max == null ) ? Filter.MIP_LINEAR : _max ;

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
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof MalletTexture )
		{
			final MalletTexture rhs = ( MalletTexture )_obj ; 
			if( uWrap != rhs.uWrap || vWrap != rhs.vWrap )
			{
				return false ;
			}
			else if( minFilter != rhs.minFilter || maxFilter != rhs.maxFilter )
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

	public Wrap getUWrap()
	{
		return uWrap ;
	}

	public Wrap getVWrap()
	{
		return vWrap ;
	}

	public Filter getMinimumFilter()
	{
		return minFilter ;
	}

	public Filter getMaximumFilter()
	{
		return minFilter ;
	}

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
		public final String path ;
		public final IntVector2 dimensions = new IntVector2() ;
		public final Ratio ratio ;

		public Meta( final String _path,
					 final int _height,
					 final int _width )
		{
			path = _path ;
			set( _width, _height ) ;
			ratio = Ratio.calculateRatio( _width, _height ) ;
		}

		public void set( final int _width, final int _height )
		{
			dimensions.setXY( _width, _height ) ;
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

			if( _obj instanceof Meta )
			{
				final Meta meta = ( Meta )_obj ;
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
