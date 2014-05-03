package com.linxonline.mallet.io.reader.ogg ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class Page
{
	public enum HeaderType
	{
		CONTINUATION, BEGIN, END, UNDEFINED
	}

	public final String header ;		// Capture pattern OggS
	public final byte version ;			// Version - 0
	public final byte hType ;			// Header Type
	public final long gPosition ;		// Granule Position
	public final int bSN ;				// Bitstream Serial Number
	public final int pSeq ;				// Page Sequence Number
	public final int checksum ;			// Checksum
	public final byte pSeg ;			// Page Segments
	public final byte[] data ;			// Segment Table

	Page( final String _header, 
		  final byte _version,
		  final byte _hType, 
		  final long _gPosition, 
		  final int _bSN, 
		  final int _pSeq, 
		  final int _checksum, 
		  final byte _pSeg,
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

	public HeaderType getHeaderType()
	{
		switch( hType )
		{
			case 0x01 : return HeaderType.CONTINUATION ;
			case 0x02 : return HeaderType.BEGIN ;
			case 0x04 : return HeaderType.END ;
		}

		if( ConvertBytes.isBitSet( hType, 2 ) == true )
		{
			return HeaderType.END ;
		}
		else if( ConvertBytes.isBitSet( hType, 1 ) == true )
		{
			return HeaderType.BEGIN ;
		}
		else if( ConvertBytes.isBitSet( hType, 0 ) == false )
		{
			return HeaderType.CONTINUATION ;
		}

		return HeaderType.UNDEFINED ;
	}

	public long getBitStreamNumber()
	{
		return ( bSN & 0xFFFFFFFFL ) ;
	}
	
	public long getPageSequence()
	{
		return ( pSeq & 0xFFFFFFFFL ) ;
	}

	public long getChecksum()
	{
		return ( checksum & 0xFFFFFFFFL ) ;
	}

	public int getPageSegment()
	{
		return ( pSeg & 0xFF ) ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "Header: "    + header               + "\n" ) ;
		buffer.append( "Version: "   + version              + "\n" ) ;
		buffer.append( "hType: "     + getHeaderType()      + "\n" ) ;
		buffer.append( "gPosition: " + gPosition            + "\n" ) ;
		buffer.append( "bSN: "       + getBitStreamNumber() + "\n" ) ;
		buffer.append( "pSeq: "      + getPageSequence()    + "\n" ) ;
		buffer.append( "Checksum: "  + getChecksum()        + "\n" ) ;
		buffer.append( "pSeg: "      + getPageSegment()     + "\n" ) ;

		return buffer.toString() ;
	}
}