package com.linxonline.mallet.util.tools ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.io.serialisation.ByteOutput ;
import com.linxonline.mallet.io.serialisation.ByteInput ;

/**
	Simple Diff aims to find differences between 
	_base and _compare.
	It works well on fixed data structures like 
	uncompressed bitmaps.
*/
public class SimpleDiff
{
	public static byte[] encode( final byte[] _base, final byte[] _compare )
	{
		int changes = 0 ;
		int pos = -1 ;
		int changeLength = 0 ;

		final int size = _compare.length ;
		final ByteOutput out = new ByteOutput() ;

		for( int i = 0; i < size; ++i )
		{
			if( ( i >= _base.length ) || _base[i] != _compare[i] )
			{
				// If the byte referenced by i has changed or 
				// if i is larger than the base, then we 
				// must consider it changed.
				if( pos == -1 )
				{
					// Only consider a byte as a change if it is 
					// the first new change byte in the array.
					++changes ;
					pos = i ;
				}

				++changeLength ;
				if( i < ( size - 1 ) )
				{
					// We want to skip the write out if
					// a byte change has taken place or 
					// we are not at the end of the array.
					continue ;
				}
			}

			if( pos != -1 )
			{
				out.writeInt( pos ) ;
				out.writeBytes( ConvertBytes.toBytes( _compare, pos, changeLength ) ) ;

				pos = -1 ;				// Must be reset
				changeLength = 0 ;		// Must be reset
			}
		}

		// int - Num of changes, int - Position of change, int - Num of bytes - X array of bytes,
		// repeating from int - Position of change.
		return ConvertBytes.concat( ConvertBytes.toBytes( changes, ConvertBytes.BIG_ENDIAN ), out.getBytes() ) ;
	}

	public static byte[] decode( final byte[] _base, final byte[] _diff )
	{
		final ByteInput input = ByteInput.readStream( _diff ) ;
		final int changes = input.readInt() ;

		if( changes == 0 )
		{
			return _base ;
		}

		final ByteOutput out = new ByteOutput() ;
		int index = 0 ;

		for( int i = 0; i < changes; i++ )
		{
			final int pos = input.readInt() ;
			final byte[] values = input.readBytes() ;

			if( pos < _base.length )
			{
				// Write out the bytes to the new stream 
				// that have not changed.
				for( int j = index; j < pos; j++ )
				{
					out.writeByte( _base[j] ) ;
				}
			}

			final int length = values.length ;
			for( int j = 0; j < length; ++j )
			{
				out.writeByte( values[j] ) ;
			}

			index = pos + length ;
		}
		
		return out.getBytes() ;
	}
}