package com.linxonline.mallet.io.formats.ogg ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class Huffman
{
	public static Node build( final int[] _codewords, final int _entries )
	{
		final Node root = new Node( 0 ) ;
		for( int i = 0; i < _entries; ++i )
		{
			root.add( _codewords[i], i ) ;
		}
		return root ;
	}

	public static class Node
	{
		private final int tier ;

		private Node lhs = null ;
		private Node rhs = null ;
		private int value = -1 ;

		private Node( final int _tier )
		{
			tier = _tier ;
		}

		public boolean add( final int _length, final int _value )
		{
			if( tier > _length )
			{
				return false ;
			}

			if( value > -1 )
			{
				return false ;
			}

			if( _length == tier )
			{
				if( lhs != null || rhs != null )
				{
					return false ;
				}

				value = _value ;
				return true ;
			}

			if( lhs == null )
			{
				lhs = new Node( tier + 1 ) ;
			}
			if( lhs.add( _length, _value ) == true )
			{
				return true ;
			}

			if( rhs == null )
			{
				rhs = new Node( tier + 1 ) ;
			}

			if( rhs.add( _length, _value ) == true )
			{
				return true ;
			}

			return false ;
		}

		public int get( final byte[] _codeword, final int _index, final int _length )
		{
			if( _index < _length )
			{
				final boolean side = ConvertBytes.isBitSet( _codeword, _index ) ;
				final int next = _index + 1 ;
				return ( side == false ) ? lhs.get( _codeword, next, _length ) : rhs.get( _codeword, next, _length ) ;
			}

			return value ;
		}

		private String tabs( final int _num )
		{
			final StringBuilder builder = new StringBuilder( _num ) ;
			for( int i = 0; i < _num; ++i )
			{
				builder.append( '\t' ) ;
			}
			return builder.toString() ;
		}

		@Override
		public String toString()
		{
			final String tabs = tabs( tier ) ;

			final StringBuilder builder = new StringBuilder() ;
			builder.append( tabs ) ;
			builder.append( "Tier: " ) ;
			builder.append( tier ) ;
			builder.append( '\n' ) ;
			if( value == -1 )
			{
				builder.append( tabs ) ;
				builder.append( "Left: " ) ;
				builder.append( ( lhs != null ) ? lhs.toString() : ( "null " + tier ) ) ;
				builder.append( '\n' ) ;
				builder.append( tabs ) ;
				builder.append( "Right: " ) ;
				builder.append( ( rhs != null ) ? rhs.toString() : ( "null " + tier ) ) ;
			}
			else
			{
				builder.append( tabs ) ;
				builder.append( "Leaf: " + value ) ;
			}
			builder.append( '\n' ) ;
			return builder.toString() ;
		}
	}
}
