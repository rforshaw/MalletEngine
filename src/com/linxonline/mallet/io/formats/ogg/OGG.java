package com.linxonline.mallet.io.formats.ogg ;

import java.util.List ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;

import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Utility ;

public class OGG
{
	public final List<Page> pages = Utility.<Page>newArrayList() ;

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

		final int streamLength = _stream.length ;
		//System.out.println( "Stream Size: " + _stream.length ) ;

		while( end == false  && ( pos + 4 ) < streamLength )
		{
			//System.out.println( "Pos: " + pos ) ;
			final String header = new String( ConvertBytes.toBytes( _stream, pos, 4 ) ) ;
			final byte version = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] ;		// unsigned byte
			final byte hType = ConvertBytes.toBytes( _stream, pos += 1, 1 )[0] ;		// unsigned byte

			ConvertBytes.flipEndian( _stream, pos += 1, 8 ) ;							// OGG is stored in little-endian, Java is big-endian.
			final long gPosition = ConvertBytes.toLong( _stream, pos, 8 ) ;

			ConvertBytes.flipEndian( _stream, pos += 8, 4 ) ;
			final int bSN = ConvertBytes.toInt( _stream, pos, 4 ) ;

			ConvertBytes.flipEndian( _stream, pos += 4, 4 ) ;
			final int pSeq = ConvertBytes.toInt( _stream, pos, 4 ) ;

			ConvertBytes.flipEndian( _stream, pos += 4, 4 ) ;
			final int checksum = ConvertBytes.toInt( _stream, pos, 4 ) ;

			final byte pSeg = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] ;		// unsigned byte
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

			final Page page = new Page( header, version, hType, gPosition, bSN, pSeq, checksum, pSeg, data ) ;
			pages.add( page ) ;

			//System.out.println( page ) ;
			if( page.getHeaderType() == Page.HeaderType.END ) // 4 represents end of file, 0 means progressing & 2 represents start.
			{
				end = true ;
			}
		}
	}
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		for( final Page page : pages )
		{
			buffer.append( page.toString() + "\n" ) ;
		}

		buffer.append( "Pages: " + pages.size() + "\n" ) ;

		return buffer.toString() ;
	}
}
