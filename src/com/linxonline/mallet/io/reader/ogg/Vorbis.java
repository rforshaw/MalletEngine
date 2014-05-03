package com.linxonline.mallet.io.reader.ogg ;

import java.util.ArrayList ;
import java.lang.Exception ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	Vorbis integer variables are stored in little endian.
**/
public class Vorbis
{
	private final static int ID_HEADER_TYPE = 1 ;
	private final static int COMMENT_HEADER_TYPE = 3 ;
	private final static int SETUP_HEADER_TYPE = 5 ;

	private final static int NO_LOOKUP_TABLE = 0 ;
	private final static int IMPLICIT_LOOKUP_TABLE = 1 ;
	private final static int EXPLICIT_LOOKUP_TABLE = 2 ;
	
	private ArrayList<VorbisHeader> headers = new ArrayList<VorbisHeader>() ;

	private int version = -1 ;
	private byte audioChannels = 0 ;
	private int sampleRate = 0 ;
	private int bitrateMax = 0 ;
	private int bitrateNom = 0 ;
	private int bitrateMin = 0 ;
	private int blocksize0 = 0 ;
	private int blocksize1 = 0 ;

	private String vendor = null ;
	private final ArrayList<String> statements = new ArrayList<String>() ;
	
	public Vorbis() {}

	public void decode( final OGG _ogg ) throws Exception
	{
		System.out.println( "Reading ogg stream" ) ;
		for( final Page page : _ogg.pages )
		{
			if( headers.size() < 3 )
			{
				decodeHeaders( page )  ;
			}
		}
	}

	private void decodeHeaders( final Page _page ) throws Exception
	{
		final byte[] stream = _page.data ;
		for( int i = 0; i < stream.length; i++ )
		{
			if( stream[i] == 'v')
			{
				if( stream[i + 1] == 'o' &&
					stream[i + 2] == 'r' &&
					stream[i + 3] == 'b' &&
					stream[i + 4] == 'i' &&
					stream[i + 5] == 's' )
				{
					// Package Type is one byte before vorbis.
					final int packageType = ConvertBytes.toBytes( stream, i - 1, 1 )[0] ;
					final VorbisHeader header = new VorbisHeader( packageType, i += 6, _page ) ;
					headers.add( header ) ;

					// Skip bytes part of the current header
					// Required so we don't accidentally find 'vorbis' within the comment header
					i = decodeHeaderType( header ) ;
				}
			}
		}
	}

	private int decodeHeaderType( final VorbisHeader _header ) throws Exception
	{
		switch( _header.headerType )
		{
			case ID_HEADER_TYPE :
			{
				return decodeIDHeader( _header ) ;
			}
			case COMMENT_HEADER_TYPE :
			{
				return decodeCommentHeader( _header ) ;
			}
			case SETUP_HEADER_TYPE :
			{
				return decodeSetupHeader( _header ) ;
			}
		}

		// The first page or so, should contain all Vorbis Header details.
		// This should never be thrown, unless the ogg is corrupt.
		throw new Exception( "Failed to decode header." )  ;
	}

	private int decodeIDHeader( final VorbisHeader _header ) throws Exception
	{
		int pos = _header.start ;
		final byte[] stream = _header.page.data ;

		ConvertBytes.flipEndian( stream, pos, 4 ) ;
		version = ConvertBytes.toInt( stream, pos, 4 ) ;

		audioChannels = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] ;	// unsigned byte

		ConvertBytes.flipEndian( stream, pos += 1, 4 ) ;
		sampleRate = ConvertBytes.toInt( stream, pos, 4 ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateMax = ConvertBytes.toInt( stream, pos, 4 ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateNom = ConvertBytes.toInt( stream, pos, 4 ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateMin = ConvertBytes.toInt( stream, pos, 4 ) ;

		final byte blocksize = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] ; // unsigned byte
		blocksize0 = ( blocksize & 0x0F ) ;
		blocksize1 = ( ( blocksize >> 4 ) & 0x0F );

		blocksize0 = ( int )Math.pow( 2, blocksize0 ) ;
		blocksize1 = ( int )Math.pow( 2, blocksize1 ) ; ;

		final byte framing = ConvertBytes.toBytes( stream, pos += 1, 1 )[0] ;
		if( ConvertBytes.isBitSet( framing, 0 ) == false )
		{
			throw new Exception( "Identification End Frame not suitable." ) ;
		}

		return pos ;
	}

	private int decodeCommentHeader( final VorbisHeader _header ) throws Exception
	{
		int pos = _header.start ;
		final byte[] stream = _header.page.data ;

		ConvertBytes.flipEndian( stream, pos, 4 ) ;
		final int vendorLength = ConvertBytes.toInt( stream, pos, 4 ) ;
		vendor = new String( ConvertBytes.toBytes( stream, pos += 4, vendorLength ) ) ;

		ConvertBytes.flipEndian( stream, pos += vendorLength, 4 ) ;
		final int iterate = ConvertBytes.toInt( stream, pos, 4 ) ;

		int length = 4 ; // Set to 4 to offset the iterate variable
		for( int i = 0; i < iterate; i++ )
		{
			ConvertBytes.flipEndian( stream, pos += length, 4 ) ;
			length = ConvertBytes.toInt( stream, pos, 4 ) ;
			statements.add( new String( ConvertBytes.toBytes( stream, pos += 4, length ) ) ) ;
		}

		// The last string doesn't increment the pos by its length
		pos += length ;

		final byte framing = ConvertBytes.toBytes( stream, pos, 1 )[0] ;
		if( ConvertBytes.isBitSet( framing, 0 ) == false )
		{
			throw new Exception( "Comment End Frame not suitable." ) ;
		}

		return pos ;
	}

	private int decodeSetupHeader( final VorbisHeader _header ) throws Exception
	{
		int pos = _header.start ;
		final byte[] stream = _header.page.data ;
		
		final int codebookCount = ( ConvertBytes.toBytes( stream, pos, 1 )[0] & 0xff ) + 1 ;	// unsigned byte
		System.out.println( "Codebooks Count: " + codebookCount ) ;
		decodeCodebooks( pos += 1, codebookCount, stream ) ;

		return pos ;
	}

	private int decodeCodebooks( int _pos, final int _count, final byte[] _stream ) throws Exception
	{
		// Pattern Sync
		final byte[] tempSync = ConvertBytes.toBytes( _stream, _pos, 3 ) ;
		final byte[] syncBytes = new byte[4] ;
		syncBytes[0] = tempSync[0] ;
		syncBytes[1] = tempSync[1] ;
		syncBytes[2] = tempSync[2] ;
		syncBytes[3] = 0 ;

		ConvertBytes.flipEndian( syncBytes, 0, 4 ) ;
		final long syncPattern = ConvertBytes.toInt( syncBytes, 0, 4 ) ;
		System.out.println( "Sync Pattern: " + syncPattern + " Pos: " + _pos ) ;

		// Dimensions
		final byte[] dimBytes = ConvertBytes.toBytes( _stream, _pos += 3, 2 ) ;
		ConvertBytes.flipEndian( dimBytes, 0, 2 ) ;
		final int dimensions = ( int )ConvertBytes.toShort( dimBytes, 0, 2 ) ;
		System.out.println( "Dimensions: " + dimensions ) ;

		// Entries
		final byte[] tempEntry = ConvertBytes.toBytes( _stream, _pos += 2, 3 ) ;
		final byte[] entryBytes = new byte[4] ;
		entryBytes[0] = tempEntry[0] ;
		entryBytes[1] = tempEntry[1] ;
		entryBytes[2] = tempEntry[2] ;
		entryBytes[3] = 0 ;

		ConvertBytes.flipEndian( entryBytes, 0, 4 ) ;
		final int entries = ConvertBytes.toInt( entryBytes, 0, 4 ) ;
		System.out.println( "Entries: " + entries ) ;

		// Flags
		final byte flags = ConvertBytes.toBytes( _stream, _pos += 3, 1 )[0] ;
		final boolean orderedFlag = ConvertBytes.isBitSet( flags, 7 ) ;
		final boolean sparseFlag = ConvertBytes.isBitSet( flags, 6 ) ;

		_pos += 1 ;					// Increment by 2 positions to ensure bits access correct content
		int bitOffset = 0 ;			// Reading a bit stream now

		final int[] codewordLengths = new int[entries] ;
		for( int i = 0; i < entries; ++i )
		{
			if( sparseFlag == true )
			{
				final byte flag = ConvertBytes.toBits( _stream, _pos, bitOffset++, 1 )[0] ;
				if( ConvertBytes.isBitSet( flag, 0 ) == true )
				{
					final byte length = ConvertBytes.toBits( _stream, _pos, bitOffset, 5 )[0] ;
					codewordLengths[i] = length + 1 ;
					bitOffset += 5 ;
				}
				else
				{
					codewordLengths[i] = -1 ;		// -1 represents a codeword that isn't used
				}
			}
			else
			{
				final byte length = ConvertBytes.toBits( _stream, _pos, bitOffset, 5 )[0] ;
				codewordLengths[i] = length + 1;
				bitOffset += 5 ;
				System.out.println( "Entry: " + i + " Codeword Length: " + codewordLengths[i] ) ;
			}
		}

		if( orderedFlag == true )
		{
			System.out.println( "Ordered Flag - enabled" ) ;
		}

		final byte lookupType = ConvertBytes.toBits( _stream, _pos, bitOffset, 4 )[0] ;
		bitOffset += 4 ;

		System.out.println( "Lookup Table Type: " + lookupType ) ;
		switch( lookupType )
		{
			case IMPLICIT_LOOKUP_TABLE : System.out.println( "Not Implemented" ) ; break ;
			case EXPLICIT_LOOKUP_TABLE : System.out.println( "Not Implemented" ) ; break ;
		}
		
		return _pos ;
	}

	public long getVersion()
	{
		return version & 0xFFFFFFFFL ;
	}

	public int getChannels()
	{
		return audioChannels & 0xFF ;
	}

	public long getSampleRate()
	{
		return sampleRate & 0xFFFFFFFFL ;
	}

	public long getBitrateMax()
	{
		return bitrateMax & 0xFFFFFFFFL ;
	}

	public long getBitrateNom()
	{
		return bitrateNom & 0xFFFFFFFFL ;
	}

	public long getBitrateMin()
	{
		return bitrateMin & 0xFFFFFFFFL ;
	}

	public int getBlockSize0()
	{
		return blocksize0 ;
	}

	public int getBlockSize1()
	{
		return blocksize1 ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "Version: "     + getVersion()    + "\n" ) ;
		buffer.append( "Channels: "    + getChannels()   + "\n" ) ;
		buffer.append( "Sample Rate: " + getSampleRate() + "\n" ) ;
		buffer.append( "Bitrate Max: " + getBitrateMax() + "\n" ) ;
		buffer.append( "Bitrate Nom: " + getBitrateNom() + "\n" ) ;
		buffer.append( "Bitrate Min: " + getBitrateMin() + "\n" ) ;
		buffer.append( "Blocksize 0: " + getBlockSize0() + "\n" ) ;
		buffer.append( "Blocksize 1: " + getBlockSize1() + "\n" ) ;

		buffer.append( "Vendor: " + vendor + "\n" ) ;
		for( String statement : statements )
		{
			buffer.append( "Statement: " + statement + "\n" ) ;
		}

		return buffer.toString() ;
	}

	/**
		Contains the information required to quickly decode the headers.
	**/
	public class VorbisHeader
	{
		public final int headerType ;	// ID_HEADER_TYPE, COMMENT_HEADER_TYPE, & SETUP_HEADER_TYPE are valid values
		public final int start ;			// The start of header information skips default header data
		public final Page page ;			// Page the data is located on

		public VorbisHeader( final int _headerType, final int _start, final Page _page )
		{
			headerType = _headerType ;
			start = _start ;
			page = _page ;
		}
	}
}