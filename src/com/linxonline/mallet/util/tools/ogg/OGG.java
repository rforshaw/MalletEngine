package com.linxonline.mallet.util.tools.ogg ;

import java.util.ArrayList ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;

import com.linxonline.mallet.resources.ResourceManager ;
import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.util.tools.ConvertBytes ;

public class OGG
{
	public final ArrayList<Page> pages = new ArrayList<Page>() ;

	public OGG( final String _file )
	{
		final FileSystem fileSystem = ResourceManager.getResourceManager().getFileSystem() ;
		final byte[] stream = fileSystem.getResourceRaw( "base/audio/0.ogg" ) ;
		createPages( stream ) ;
	}

	private void createPages( final byte[] _stream )
	{
		int pos = 0 ;
		boolean go = false ;

		do
		{
			final String header = new String( ConvertBytes.toBytes( _stream, pos, 4 ) ) ;
			final int version = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] ;
			final int hType = ConvertBytes.toBytes( _stream, pos += 1, 1 )[0] ;
			final long gPosition = ConvertBytes.toLong( _stream, pos += 1, 8 ) ;
			final int bSN = ConvertBytes.toInt( _stream, pos += 8, 4 ) ;
			final int pSeq = ConvertBytes.toInt( _stream, pos += 4, 4 ) ;
			final int checksum = ConvertBytes.toInt( _stream, pos += 4, 4 ) ;
			final int pSeg = ConvertBytes.toBytes( _stream, pos += 4, 1 )[0] ;
			final byte[] segTable = ConvertBytes.toBytes( _stream, pos += 1, pSeg ) ;

			final ByteArrayOutputStream output = new ByteArrayOutputStream() ;
			try
			{
				output.write( ConvertBytes.toBytes( _stream, pos += pSeg, segTable[0] ) ) ;
				for( int i = 1; i < segTable.length; i++ )
				{
					output.write( ConvertBytes.toBytes( _stream, pos += segTable[i - 1], segTable[i] ) ) ;
				}
				pos += segTable[segTable.length - 1] ;
			}
			catch( IOException ex )
			{
				ex.printStackTrace() ;
			}

			final byte[] data = output.toByteArray() ;
			pages.add( new Page( header, version, hType, gPosition, bSN, pSeq, checksum, pSeg, data ) ) ;

			System.out.println( header ) ;
			System.out.println( "Version: " + version ) ;
			System.out.println( "Header Type: " + hType ) ;
			System.out.println( "Granual Position: " + gPosition ) ;
			System.out.println( "Bitstream Serial Number: " + bSN ) ;
			System.out.println( "Page Seq: " + pSeq ) ;
			System.out.println( "Checksum: " + checksum ) ;
			System.out.println( "Page Seg: " + pSeg ) ;
			System.out.println( "Data : " + new String( data ) ) ;
			
			final String nextHeader = new String( ConvertBytes.toBytes( _stream, pos, 4 ) ) ;
			System.out.println( "Next Header: " + nextHeader ) ;
			if( nextHeader.contains( "OggS" ) == true )
			{
				go = true ;
			}
			else
			{
				go = false ;
			}
		}
		while( go == true ) ;
	}

	public class Page
	{
		final String header ;
		final int version ;
		final int hType ;
		final long gPosition ;
		final int bSN ;
		final int pSeq ;
		final int checksum ;
		final int pSeg ;
		final byte[] data ;

		Page( final String _header, 
			  final int _version,
			  final int _hType, 
			  final long _gPosition, 
			  final int _bSN, 
			  final int _pSeq, 
			  final int _checksum, 
			  final int _pSeg,
			  final byte[] _data )
		{
			header = _header ;
			version = _version ;
			hType = _hType ;
			gPosition = _gPosition ;
			bSN = _bSN ;
			pSeq = _pSeq ;
			checksum = _checksum ;
			pSeg = _pSeg ;
			data = _data ;
		}
	}
}