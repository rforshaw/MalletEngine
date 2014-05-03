package com.linxonline.mallet.io.reader.wav ;

import com.linxonline.mallet.util.tools.* ;

public class WAVHeader
{
	public int startPoint = 0 ;
	public int length = 0 ;
	public short bitPerSample = 0 ;
	public int samplerate = 0 ;
	public short channels ; 				// The amount of channels available

	private WAVHeader() {}

	public static WAVHeader getHeader( final byte[] _buffer )
	{
		final WAVHeader header = new WAVHeader() ;
		header.startPoint = getDataStartPoint( _buffer ) ;
		header.length = getDataSize( _buffer ) ;
		header.bitPerSample = getBitsPerSample( _buffer ) ;
		header.samplerate = getSampleRate( _buffer ) ;
		header.channels = getChannels( _buffer ) ;
		return header ;
	}

	public static int getDataStartPoint( final byte[] _soundBuffer )
	{
		final int length = _soundBuffer.length ;
		for( int i = 0; i < length; ++i )
		{
			if( _soundBuffer[i] == 'd' )
			{
				if( _soundBuffer[i + 1] == 'a' &&
					_soundBuffer[i + 2] == 't' &&
					_soundBuffer[i + 3] == 'a' )
				{
					// Add 4 to skip data and
					// another 4 to skip ChunkSize
					return i + 8 ;
				}
			}
		}

		return 0 ;
	}

	public static int getDataSize( final byte[] _soundBuffer )
	{
		final int pos = getDataStartPoint( _soundBuffer ) - 4 ;
		final byte[] temp = new byte[4] ;
		temp[0] = _soundBuffer[pos + 3] ;
		temp[1] = _soundBuffer[pos + 2] ;
		temp[2] = _soundBuffer[pos + 1] ;
		temp[3] = _soundBuffer[pos] ;

		return ConvertBytes.toInteger( temp ) ;
	}

	public static short getBitsPerSample( final byte[] _soundBuffer )
	{
		final byte[] temp = new byte[2] ;
		temp[0] = _soundBuffer[35] ;
		temp[1] = _soundBuffer[34] ;

		return ConvertBytes.toShort( temp ) ;
	}

	public static int getSampleRate( final byte[] _soundBuffer )
	{
		final byte[] temp = new byte[4] ;
		temp[0] = _soundBuffer[27] ;
		temp[1] = _soundBuffer[26] ;
		temp[2] = _soundBuffer[25] ;
		temp[3] = _soundBuffer[24] ;

		return ConvertBytes.toInteger( temp ) ;
	}

	public static short getChannels( final byte[] _soundBuffer )
	{
		final byte[] temp = new byte[2] ;
		temp[0] = _soundBuffer[23] ;
		temp[1] = _soundBuffer[22] ;

		return ConvertBytes.toShort( temp ) ;
	}
}