package com.linxonline.mallet.renderer ;

import java.text.MessageFormat ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;

public final class AssetLookup<L, R>
{
	private final String name ;
	private Tuple<L, R>[] assets ;

	public AssetLookup( final String _name )
	{
		this( _name, 10 ) ;
	}

	public AssetLookup( final String _name, final int _capacity )
	{
		name = _name ;
		assets = ( Tuple<L, R>[] )new Tuple<?, ?>[_capacity] ;
	}

	public Tuple<L, R> get( final int _index )
	{
		if( _index >= assets.length )
		{
			notYetMapped( _index ) ;
			return null ;
		}

		return assets[_index] ;
	}

	public L getLHS( final int _index )
	{
		if( _index >= assets.length )
		{
			notYetMapped( _index ) ;
			return null ;
		}

		final Tuple<L, R> asset = assets[_index] ;
		if( asset == null )
		{
			notYetMapped( _index ) ;
			return null ;
		}

		return asset.getLeft() ;
	}

	public R getRHS( final int _index )
	{
		if( _index >= assets.length )
		{
			notYetMapped( _index ) ;
			return null ;
		}

		final Tuple<L, R> asset = assets[_index] ;
		if( asset == null )
		{
			notYetMapped( _index ) ;
			return null ;
		}

		return asset.getRight() ;
	}

	public Tuple<L, R> map( final int _index, final L _lhs, final R _rhs )
	{
		if( assets.length <= _index )
		{
			final int diff = _index - assets.length ;
			expand( diff + 10 ) ;
		}

		final Tuple<L, R> asset = new Tuple<L, R>( _lhs, _rhs ) ;
		assets[_index] = asset ;

		return asset ;
	}

	public Tuple<L, R> unmap( final int _index )
	{
		if( assets.length <= _index )
		{
			final int diff = _index - assets.length ;
			expand( diff + 10 ) ;
		}

		final Tuple<L, R> asset = assets[_index] ;
		assets[_index] = null ;
		return asset ;
	}

	/**
		Expand the assets array.
	*/
	public void expand( final int _extra )
	{
		final int length = assets.length + _extra ;
		final Tuple<L, R>[] to = ( Tuple<L, R>[] )new Tuple<?, ?>[length] ;
		System.arraycopy( assets, 0, to, 0, assets.length ) ;
		assets = to ;
	}

	private void notYetMapped( final int _index )
	{
		Logger.println( MessageFormat.format( "{0}, attempting to access {1}, asset not yet mapped.", name, _index ), Logger.Verbosity.MAJOR ) ;
	}
}
