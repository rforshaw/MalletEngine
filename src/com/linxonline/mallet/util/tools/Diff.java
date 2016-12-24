package com.linxonline.mallet.util.tools ;

import java.util.List ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.io.serialisation.ByteOutput ;
import com.linxonline.mallet.io.serialisation.ByteInput ;
import com.linxonline.mallet.util.Utility ;

/**
	This Diff implementation aims to find comparisons 
	between _base and _compare.
	It works well on dynamic data sctructures like 
	text.
*/
public final class Diff
{
	private Diff() {}

	public static byte[] encode( final byte[] _base, final byte[] _compare )
	{
		final List<Point> matches = Utility.<Point>newArrayList() ;

		int pos = 0 ;
		// Find parts between _base and _compare 
		// that are identical.
		while( pos < _compare.length )
		{
			final Point point = findBestMatch( _compare, pos, _base, 0 ) ;
			if( point != null )
			{
				if( isUsed( point, matches ) == false )
				{
					pos = point.pos + point.length ;
					matches.add( point ) ;
					continue ;
				}
			}

			++pos ;
		}

		final List<Point> additions = Utility.<Point>newArrayList() ;

		// Assume that all other positions not flagged 
		// within an existing point are new and must be 
		// added to the diff.
		pos = 0 ;
		for( final Point point : matches )
		{
			if( pos < point.pos )
			{
				additions.add( new Point( -1, pos, ( point.pos - pos ) ) ) ;
			}

			pos = point.pos + point.length ;
		}

		matches.addAll( additions ) ;

		final ByteOutput out = new ByteOutput() ;
		for( final Point point : matches )
		{
			if( point.basePos >= 0 )
			{
				out.writeInt( point.basePos ) ;
				out.writeInt( point.pos ) ;
				out.writeInt( point.length ) ;
			}
			else
			{
				out.writeInt( point.basePos ) ;
				out.writeInt( point.pos ) ;
				out.writeBytes( ConvertBytes.toBytes( _compare, point.pos, point.length ) ) ;		// Copy the new bytes to the diff
			}
		}
		
		return out.getBytes() ;
	}

	public static byte[] decode( final byte[] _base, final byte[] _diff )
	{
		final ByteInput diff = ByteInput.readStream( _diff ) ;
		final List<Point> points = Utility.<Point>newArrayList() ;

		int arraySize = 0 ;
		while( diff.isEnd() == false )
		{
			final int basePos = diff.readInt() ;
			final int pos = diff.readInt() ;
			
			if( basePos >= 0 )
			{
				final int length = diff.readInt() ;
				if( arraySize < ( pos + length ) )
				{
					arraySize = pos + length ;
				}

				points.add( new Point( basePos, pos, length ) ) ;
			}
			else
			{
				final byte[] stream = diff.readBytes() ;
				if( arraySize < ( pos + stream.length ) )
				{
					arraySize = pos + stream.length ;
				}

				points.add( new Point( basePos, pos, stream ) ) ;
			}
		}

		final byte[] stream = new byte[arraySize] ;
		for( final Point point : points )
		{
			if( point.basePos >= 0 )
			{
				ConvertBytes.insert( _base, point.basePos, point.length, stream, point.pos ) ;
			}
			else
			{
				ConvertBytes.insert( point.addition, 0, point.length, stream, point.pos ) ;
			}
		}

		return stream ;
	}

	
	private static Point findBestMatch( final byte[] _stream, final int _pos, final byte[] _match, int _offset )
	{
		Point best = null ;

		while( _offset < _match.length )
		{
			Point temp = findMatch( _stream, _pos, _match, _offset++ ) ;
			if( temp != null )
			{
				if( best == null )
				{
					best = temp ;
				}
				else
				{
					if( best.length < temp.length )
					{
						best = temp ;
					}
				}
			}
		}

		return best ;
	}

	private static Point findMatch( final byte[] _stream, final int _pos, final byte[] _compare, final int _offset )
	{
		if( _compare[_offset] == _stream[_pos] )
		{
			final int longest = findLongest( _stream, _pos + 1, _stream.length, _compare, _offset + 1 ) ;
			if( longest > 1 )
			{
				//System.out.println( "Longest: " + ( longest + 1 ) ) ;
				return new Point( _offset, _pos, longest + 1 ) ;
			}
		}

		return null ;
	}

	private static int findLongest( final byte[] _stream, final int _pos, final int _len, final byte[] _compare, int _increment )
	{
		int i = _pos ;

		for( i = _pos; i < _len; ++i )
		{
			if( _increment >= _compare.length )
			{
				break ;
			}
			if( _stream[i] != _compare[_increment++] )
			{
				break ;
			}
		}

		return i - _pos ;
	}

	/**
		Determine whether or not _point.pos is 
		used in another point.
	*/
	private static boolean isUsed( final Point _point, final List<Point> _points )
	{
		for( final Point point : _points )
		{
			if( point.isWithin( _point ) == true )
			{
				return true ;
			}
		}

		return false ;
	}
	
	private static class Point
	{
		public final int basePos ;
		public final int pos ;
		public final int length ;

		public final byte[] addition ;
		
		public Point( final int _basePos, final int _pos, final int _length )
		{
			basePos = _basePos ;
			pos = _pos ;
			length = _length ;
			addition = null ;
		}

		public Point( final int _basePos, final int _pos, final byte[] _add )
		{
			basePos = _basePos ;
			pos = _pos ;
			length = _add.length ;
			addition = _add ;
		}

		public boolean isWithin( final Point _point )
		{
			final int overlap = overlap( _point.pos, _point.length ) ;
			return ( overlap > 0 ) ? true : false ;
		}

		public int overlap( final int _pos, final int _length )
		{
			final int min1 = pos ;
			final int max1 = pos + length ;

			final int min2 = _pos ;
			final int max2 = _pos + _length ;

			return Math.max( 0, Math.min( max1, max2 ) - Math.max( min1, min2 ) ) ;
		}
		
		public String toString()
		{
			return "Base Pos: " + basePos + " Pos: " + pos + " Length: " + length ;
		}
	}
}
