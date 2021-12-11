package com.linxonline.mallet.io.serialisation ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	Serialise.Out and Serialise.In work in tandem.

	If Serialise.Out.writeBytes is called and X bytes is 
	passed in, then the subsequent Serialise.Inp will return 
	the same X bytes.

	Serialise.Out.writeBytes( new byte[10] ) ;
	Serialise.Out.writeBytes( new byte[5] ) ;
	Serialise.Out.writeBytes( new byte[8] ) ;

	Serialise.In.readBytes() ; <--- Will return 10
	Serialise.In.readBytes() ; <--- Will return 5
	Serialise.In.readBytes() ; <--- Will return 8

	All implementations should guarantee this. Note must be called 
	in identical order.
**/
public interface Serialise
{
	public class ByteIn implements In
	{
		private final byte[] in ;
		private final int limit ;
		private int offset ;

		public ByteIn( final byte[] _in )
		{
			this( _in, 0, _in.length ) ;
		}

		public ByteIn( final byte[] _in, final int _offset, final int _limit )
		{
			in = _in ;
			offset = _offset ;
			limit = _limit ;
		}

		public int readInt()
		{
			final int val = ConvertBytes.toInt( in, offset ) ;
			offset += 4 ;
			return val ;
		}

		public byte readByte()
		{
			return in[offset++] ;
		}

		public char readChar()
		{
			final char val = ConvertBytes.toChar( in, offset ) ;
			offset += 2 ;
			return val ;
		}

		public long readLong()
		{
			final long val = ConvertBytes.toLong( in, offset ) ;
			offset += 8 ;
			return val ;
		}

		public float readFloat()
		{
			final float val = ConvertBytes.toFloat( in, offset ) ;
			offset += 4 ;
			return val ;
		}

		public double readDouble()
		{
			final double val = ConvertBytes.toDouble( in, offset ) ;
			offset += 8 ;
			return val ;
		}

		public String readString()
		{
			int length = 0 ;

			boolean endChar = false ;
			while( endChar == false )
			{
				final int pos = offset + ++length ;
				if( in[pos] == '\0' )
				{
					endChar = true ;
				}
				else if( in[pos] == '\n' )
				{
					endChar = true ;
				}
				else if( pos + 1 >= limit )
				{
					endChar = true ;
				}
			}

			final String temp = new String( in, offset, length ) ;
			offset += length + 1 ;	// Add one to pass the end char
			return temp ;
		}

		public boolean readBoolean()
		{
			final boolean val = ConvertBytes.toBoolean( in, offset ) ;
			offset += 1 ;
			return val ;
		}

		public int[] readInts()
		{
			throw new UnsupportedOperationException() ;
		}

		public byte[] readBytes()
		{
			throw new UnsupportedOperationException() ;
		}

		public char[] readChars()
		{
			throw new UnsupportedOperationException() ;
		}

		public long[] readLongs()
		{
			throw new UnsupportedOperationException() ;
		}

		public float[] readFloats()
		{
			throw new UnsupportedOperationException() ;
		}

		public double[] readDoubles()
		{
			throw new UnsupportedOperationException() ;
		}

		public String[] readStrings()
		{
			throw new UnsupportedOperationException() ;
		}

		public boolean[] readBooleans()
		{
			throw new UnsupportedOperationException() ;
		}
	}

	public class ByteOut implements Out
	{
		private final byte[] out ;
		private int offset = 0 ;

		public ByteOut( final byte[] _out )
		{
			out = _out ;
		}

		public void writeInt( final int _int )
		{
			ConvertBytes.toBytes( _int, offset, out ) ;
			offset += 4 ;
		}

		public void writeByte( final byte _byte )
		{
			out[offset++] = _byte ;
		}

		public void writeChar( final char _char )
		{
			ConvertBytes.toBytes( _char, offset, out ) ;
			offset += 2 ;
		}

		public void writeLong( final long _long )
		{
			ConvertBytes.toBytes( _long, offset, out ) ;
			offset += 8 ;
		}

		public void writeFloat( final float _float )
		{
			ConvertBytes.toBytes( _float, offset, out ) ;
			offset += 4 ;
		}

		public void writeDouble( final double _double )
		{
			ConvertBytes.toBytes( _double, offset, out ) ;
			offset += 8 ;
		}

		public void writeString( final String _string )
		{
			final byte[] data = _string.getBytes() ;
			System.arraycopy( data, 0, out, offset, data.length ) ;
			offset += data.length ;
		}

		public void writeBoolean( final boolean _bool )
		{
			ConvertBytes.toBytes( _bool, offset, out ) ;
			offset += 1 ;
		}

		public void writeInts( final int[] _int )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeBytes( final byte[] _byte )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeChars( final char[] _char )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeLongs( final long[] _long )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeFloats( final float[] _float )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeDoubles( final double[] _double )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeStrings( final String[] _string )
		{
			throw new UnsupportedOperationException() ;
		}

		public void writeBooleans( final boolean[] _bool )
		{
			throw new UnsupportedOperationException() ;
		}
	}

	public interface In
	{
		public int readInt() ;
		public byte readByte() ;
		public char readChar() ;
		public long readLong() ;
		public float readFloat() ;
		public double readDouble() ;
		public String readString() ;
		public boolean readBoolean() ;

		public int[] readInts() ;
		public byte[] readBytes() ;
		public char[] readChars() ;
		public long[] readLongs() ;
		public float[] readFloats() ;
		public double[] readDoubles() ;
		public String[] readStrings() ;
		public boolean[] readBooleans() ;
	}

	public interface Out
	{
		public void writeInt( final int _int ) ;
		public void writeByte( final byte _byte ) ;
		public void writeChar( final char _char ) ;
		public void writeLong( final long _long ) ;
		public void writeFloat( final float _float ) ;
		public void writeDouble( final double _double ) ;
		public void writeString( final String _string ) ;
		public void writeBoolean( final boolean _bool ) ;

		public void writeInts( final int[] _int ) ;
		public void writeBytes( final byte[] _byte ) ;
		public void writeChars( final char[] _char ) ;
		public void writeLongs( final long[] _long ) ;
		public void writeFloats( final float[] _float ) ;
		public void writeDoubles( final double[] _double ) ;
		public void writeStrings( final String[] _string ) ;
		public void writeBooleans( final boolean[] _bool ) ;
	}
}
