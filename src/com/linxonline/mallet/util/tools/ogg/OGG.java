package com.linxonline.mallet.util.tools.ogg ;

import java.util.ArrayList ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;

import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.util.tools.ConvertBytes ;

public class OGG
{
	public final ArrayList<Page> pages = new ArrayList<Page>() ;

	public static OGG readOGG( final String _file )
	{
		final byte[] stream = ByteReader.readBytes( _file ) ;
		if( stream != null )
		{
			return new OGG( stream ) ;
		}

		return null ;
	}

	public OGG( final byte[] _stream )
	{
		createPages( _stream ) ;
	}

	private void createPages( final byte[] _stream )
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		boolean end = false ;
		int pos = 0 ;

		while( end == false )
		{
			final String header = new String( ConvertBytes.toBytes( _stream, pos, 4 ) ) ;

			final int version = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] & 0xff ;	// unsigned byte
			final int hType = ConvertBytes.toBytes( _stream, pos += 1, 1 )[0] & 0xff ;		// unsigned byte
			final long gPosition = ConvertBytes.toLong( _stream, pos += 1, 8 ) ;
			final int bSN = ConvertBytes.toInt( _stream, pos += 8, 4 ) ;
			final int pSeq = ConvertBytes.toInt( _stream, pos += 4, 4 ) ;
			final int checksum = ConvertBytes.toInt( _stream, pos += 4, 4 ) ;
			final int pSeg = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] & 0xff ;		// unsigned byte
			final byte[] segTable = ConvertBytes.toBytes( _stream, pos += 1, pSeg ) ;

			try
			{
				pos += pSeg ;
				for( int i = 0; i < pSeg; i++ )
				{
					final int length = ( segTable[i] & 0xff ) ; 
					out.write( ConvertBytes.toBytes( _stream, pos, length ) ) ;
					pos += length ;
				}
			}
			catch( IOException ex )
			{
				ex.printStackTrace() ;
			}

			final byte[] data = out.toByteArray() ;
			out.reset() ;

			pages.add( new Page( header, version, hType, gPosition, bSN, pSeq, checksum, pSeg, data ) ) ;
			if( hType == 4 ) // 4 represents end of file, 0 means progressing & 2 represents start.
			{
				end = true ;
			}
		}
	}
}