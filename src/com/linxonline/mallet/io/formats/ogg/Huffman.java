package com.linxonline.mallet.io.formats.ogg ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class Huffman
{
	public static Node build( final int[] _codewords, final int _entries )
	{
		final Node root = new Node( null ) ;
		for( int i = 0; i < _entries; ++i )
		{
			final int codeword = _codewords[i] ;
			if( codeword <= -1 )
			{
				continue ;
			}

			if( root.add( codeword, i ) == false )
			{
				throw new RuntimeException( "Failed to build a valid Huffman." ) ;
			}
		}
		return root ;
	}

	public static class Node
	{
		private final Node parent ;
		private final int depth ;

		private Node lhs = null ;
		private Node rhs = null ;
		private int value = -1 ;

		private Node( final Node _parent )
		{
			this( _parent, -1 ) ;
		}

		private Node( final Node _parent, final int _value )
		{
			parent = _parent ;
			depth = ( parent != null ) ? parent.getDepth() + 1 : 0 ;
			value = _value ;
		}

		public boolean isFull()
		{
			if( lhs == null && rhs == null && value >= 0 )
			{
				return true ;
			}
		
			boolean lhsFull = false ;
			if( lhs != null )
			{
				lhsFull = lhs.isFull() ;
			}

			boolean rhsFull = false ;
			if( rhs != null )
			{
				rhsFull = rhs.isFull() ;
			}

			return ( lhsFull == true && rhsFull == true )  ;
		}

		public boolean add( final int _length, final int _value )
		{
			//System.out.println( "Length: " + _length + " Value: " + _value ) ;
			if( isFull() == true )
			{
				return false ;
			}

			if( _length == 1 )
			{
				if( lhs == null )
				{
					lhs = new Node( this, _value ) ;
					//System.out.println( "Stored LHS: " + lhs.getDepth() + " Value: " + _value ) ;
					return true ;
				}
				else if( rhs == null )
				{
					rhs = new Node( this, _value ) ;
					//System.out.println( "Stored RHS: " + rhs.getDepth() + " Value: " + _value ) ;
					return true ;
				}
				return false ;
			}

			lhs = ( lhs == null ) ? new Node( this ) : lhs ;
			if( lhs.add( _length - 1, _value ) == true )
			{
				return true ;
			}

			rhs = ( rhs == null ) ? new Node( this ) : rhs ;
			if( rhs.add( _length - 1, _value ) == true )
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

		public int getDepth()
		{
			return depth ;
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
			final String tabs = tabs( depth ) ;

			final StringBuilder builder = new StringBuilder() ;
			builder.append( "Node: " ) ;
			builder.append( depth ) ;
			builder.append( '\n' ) ;

			if( lhs == null && rhs == null )
			{
				builder.append( "Value: " ) ;
				builder.append( value ) ;
				builder.append( '\n' ) ;
			}

			builder.append( "Left: " ) ;
			builder.append( ( lhs != null ) ? lhs.toString() : "" ) ;

			builder.append( "Right: " ) ;
			builder.append( ( rhs != null ) ? rhs.toString() : "" ) ;

			return builder.toString() ;
		}
	}
}
