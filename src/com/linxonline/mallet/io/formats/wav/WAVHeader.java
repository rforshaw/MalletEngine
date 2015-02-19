package com.linxonline.mallet.io.formats.wav ;

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
		int pos = getDataStartPoint( _soundBuffer ) - 4 ;
		ConvertBytes.flipEndian( _soundBuffer, pos, 4 ) ;
		final int size = ConvertBytes.toInt( _soundBuffer, pos, 4 ) ;
		pos += 4 ;

		return size ;
	}

	public static short getBitsPerSample( final byte[] _soundBuffer )
	{
		ConvertBytes.flipEndian( _soundBuffer, 34, 2 ) ;
		return ConvertBytes.toShort( _soundBuffer, 34, 2 ) ;
	}

	public static int getSampleRate( final byte[] _soundBuffer )
	{
		ConvertBytes.flipEndian( _soundBuffer, 24, 4 ) ;
		return ConvertBytes.toInt( _soundBuffer, 24, 4 ) ;
	}

	public static short getChannels( final byte[] _soundBuffer )
	{
		ConvertBytes.flipEndian( _soundBuffer, 22, 2 ) ;
		return ConvertBytes.toShort( _soundBuffer, 22, 2 ) ;
	}
}