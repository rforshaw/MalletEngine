package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.tools.ChecksumGenerator ;
import com.linxonline.mallet.util.tools.ConvertBytes ;

import com.linxonline.mallet.maths.Ratio ;

public final class TextureArray implements IUniform
{
	private final byte[] checksum ;

	private final String id ;
	private final Texture.Meta[] layers ;

	private Texture.Filter minification = Texture.Filter.MIP_LINEAR ;
	private Texture.Filter magnification = Texture.Filter.MIP_LINEAR ;
	private Texture.Wrap uWrap = Texture.Wrap.REPEAT ;
	private Texture.Wrap vWrap = Texture.Wrap.REPEAT ;

	private TextureArray( final byte[] _checksum, final Texture.Meta[] _layers )
	{
		checksum = _checksum ;
		id = new String( checksum ) ;

		layers = _layers ;
	}

	/**
		Create a texture array using the passed in
		texture paths. This will allow you access to
		multiple textures at once within your shader.
		It's similar to a texture atlas, but, doe not
		suffer the same limitations in terms of edge clamping.
	*/
	public static TextureArray create( final String[] _paths )
	{
		final ChecksumGenerator gen = new ChecksumGenerator() ;
		final byte[] checksum = gen.generate( _paths ) ;

		final Texture.Meta[] layers = new Texture.Meta[_paths.length] ;
		layers[0] = TextureAssist.createMeta( _paths[0] ) ;

		for( int i = 1; i < _paths.length; ++i )
		{
			layers[i] = TextureAssist.createMeta( _paths[i] ) ;
		}

		return new TextureArray( checksum, layers ) ;
	}

	public void setFilter( final Texture.Filter _min, final Texture.Filter _mag )
	{
		minification = ( _min == null ) ? Texture.Filter.MIP_LINEAR : _min ;
		magnification = ( _mag == null ) ? Texture.Filter.MIP_LINEAR : _mag ;
	}

	public void setWrap( final Texture.Wrap _u, final Texture.Wrap _v )
	{
		uWrap = ( _u == null ) ? Texture.Wrap.REPEAT : _u ;
		vWrap = ( _v == null ) ? Texture.Wrap.REPEAT : _v ;
	}

	public String getID()
	{
		return id ;
	}

	public int getHeight()
	{
		return layers[0].getHeight() ;
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
		return layers[0].getWidth() ;
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
		return layers[0].getRatio() ;
	}

	public Texture.Wrap getUWrap()
	{
		return uWrap ;
	}

	public Texture.Wrap getVWrap()
	{
		return vWrap ;
	}

	public Texture.Filter getMinificationFilter()
	{
		return minification ;
	}

	public Texture.Filter getMagnificationFilter()
	{
		return magnification ;
	}

	@Override
	public IUniform.Type getType()
	{
		return IUniform.Type.SAMPLER2D_ARRAY ;
	}

	public int getDepth()
	{
		return layers.length ;
	}

	public Texture.Meta getMeta( final int _index )
	{
		return layers[_index] ;
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

		if( !( _obj instanceof TextureArray ) )
		{
			return false ;
		}

		final TextureArray t = ( TextureArray )_obj ;
		return ConvertBytes.compare( checksum, t.checksum ) ;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode() ;
	}
}
