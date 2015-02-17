package com.linxonline.mallet.io.formats.ogg ;

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
	
	private final static int UNUSED = -1 ;
	
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

	private final ArrayList<CodebookConfiguration> codebooks = new ArrayList<CodebookConfiguration>() ;

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
			else
			{
				//System.out.println( "Audio Page" ) ;
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
			case ID_HEADER_TYPE      : return decodeIDHeader( _header ) ;
			case COMMENT_HEADER_TYPE : return decodeCommentHeader( _header ) ;
			case SETUP_HEADER_TYPE   : return decodeSetupHeader( _header ) ;
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
		final byte[] stream = _header.page.data ;
		int pos = _header.start * 8 ;					// Convert to bits

		final int codebookCount = ( ConvertBytes.toBits( stream, 0, pos, 8 )[0] & 0xFF ) + 1 ;	// unsigned byte
		pos += 8 ;

		for( int i = 0; i < codebookCount; i++ )
		{
			pos = decodeCodebooks( pos, codebookCount, stream ) ;
		}

		pos = decodeTimeDomain( pos, stream ) ;
		pos = decodeFloors( pos, stream ) ;

		// Residues
		// Mappings
		// Modes
		
		return pos / 8 ;
	}

	private int decodeCodebooks( int _pos, final int _count, final byte[] _stream ) throws Exception
	{
		// Pattern Sync
		final byte[] tempSync = ConvertBytes.toBits( _stream, 0, _pos, 24 ) ;
		final byte[] syncBytes = new byte[4] ;
		syncBytes[0] = tempSync[0] ;
		syncBytes[1] = tempSync[1] ;
		syncBytes[2] = tempSync[2] ;
		syncBytes[3] = 0 ;

		ConvertBytes.flipEndian( syncBytes, 0, 4 ) ;
		final long syncPattern = ConvertBytes.toInt( syncBytes, 0, 4 ) ;
		if( syncPattern != 5653314 )
		{
			throw new Exception( "OGG not in sync, failed to read correct Sync Pattern." ) ;
		}

		// Dimensions
		final byte[] dimBytes = ConvertBytes.toBits( _stream, 0, _pos += 24, 16 ) ;
		ConvertBytes.flipEndian( dimBytes, 0, 2 ) ;
		final int dimensions = ConvertBytes.toShort( dimBytes, 0, 2 ) & 0xFFFF ;

		// Entries
		final byte[] tempEntry = ConvertBytes.toBits( _stream, 0, _pos += 16, 24 ) ;
		final byte[] entryBytes = new byte[4] ;
		entryBytes[0] = tempEntry[0] ;
		entryBytes[1] = tempEntry[1] ;
		entryBytes[2] = tempEntry[2] ;
		entryBytes[3] = 0 ;

		ConvertBytes.flipEndian( entryBytes, 0, 4 ) ;
		final long entries = ConvertBytes.toInt( entryBytes, 0, 4 ) & 0xFFFFFFFFL ;

		_pos += 24 ;
		final boolean orderedFlag = ConvertBytes.isBitSet( _stream, _pos++ ) ;
		final int[] codewordLengths = new int[( int )entries] ;

		if( orderedFlag == false )
		{
			final boolean sparseFlag = ConvertBytes.isBitSet( _stream, _pos++ ) ;

			for( int i = 0; i < entries; ++i )
			{
				if( sparseFlag == true )
				{
					final boolean flag = ConvertBytes.isBitSet( _stream, _pos++ ) ;
					if( flag == true )
					{
						codewordLengths[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 5 )[0] & 0xFF ) + 1 ;
						_pos += 5 ;
					}
					else
					{
						codewordLengths[i] = UNUSED ;		// -1 represents a codeword that isn't used
					}
				}
				else
				{
					codewordLengths[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 5 )[0] & 0xFF ) + 1 ;
					_pos += 5 ;
				}
			}
		}
		else
		{
			int currentEntry = 0 ;
			int currentLength = ( ConvertBytes.toBits( _stream, 0, _pos, 5 )[0] & 0xFF ) + 1 ;
			_pos += 5 ;

			while( currentEntry < entries )
			{
				final int toRead = iLog( ( int )( entries - currentEntry ) ) ;
				final int number = ( ConvertBytes.toBits( _stream, 0, _pos, toRead )[0] ) & 0xFF ;
				_pos += toRead ;

				codewordLengths[currentEntry + number - 1] = currentLength ;
				currentEntry = number + currentEntry ;
				++currentLength ;

				if( currentEntry > entries )
				{
					throw new Exception( "Current Entry out of bounds." ) ;
				}
			}
			
		}

		final CodebookConfiguration book = new CodebookConfiguration() ;
		book.entries = entries ;
		book.dimensions = dimensions ;
		book.codewordLengths = codewordLengths ;

		// Read lookup table data - if any exists
		final int lookupType = ConvertBytes.toBits( _stream, 0, _pos, 4 )[0] & 0xFF ;
		book.lookupType = lookupType ;
		_pos += 4 ;

		if( lookupType != NO_LOOKUP_TABLE )
		{
			book.minValue = unpackFloat( ConvertBytes.toBits( _stream, 0, _pos, 32 ) ) ;
			book.deltaValue = unpackFloat( ConvertBytes.toBits( _stream, 0, _pos += 32, 32 ) ) ;
			final int valueBits = ( ConvertBytes.toBits( _stream, 0, _pos += 32, 4 )[0] & 0xFF ) + 1 ;
			book.sequenceP = ConvertBytes.isBitSet( _stream, _pos += 4 ) ;
			++_pos ;

			final int lookupValues ;
			switch( lookupType )
			{
				case IMPLICIT_LOOKUP_TABLE : lookupValues = getLookup1( ( int )entries, dimensions ) ; break ;
				case EXPLICIT_LOOKUP_TABLE : lookupValues = ( int )entries * dimensions ; break ; 
				default                    : throw new Exception( "Failed to identify Lookup Table: " + lookupType ) ;
			}

			final int[] multiplicands = new int[lookupValues] ;
			for( int i = 0; i < lookupValues; i++ )
			{
				multiplicands[i] = ConvertBytes.toBits( _stream, 0, _pos, valueBits )[0] & 0xFF ;
				_pos += valueBits ;
			}

			book.multiplicands = multiplicands ;
		}

		book.entry = codebooks.size() ;
		codebooks.add( book ) ;
		return _pos ;
	}

	private int decodeTimeDomain( int _pos, final byte[] _stream ) throws Exception
	{
		final int timeCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		System.out.println( "Time Count: " + timeCount ) ;
		_pos += 6 ;

		for( int i = 0; i < timeCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			for( int j = 0; j < read.length; j++ )
			{
				ConvertBytes.printByte( read[j] ) ;
			}

			ConvertBytes.flipEndian( read ) ;
			final int time = ConvertBytes.toShort( read, 0, 2 ) & 0xFFFF  ;
			if( time != 0 )
			{
				throw new Exception( "Failed to sync to time zero: " + time ) ;
			}
		}

		return _pos ;
	}

	private int decodeFloors( int _pos, final byte[] _stream ) throws Exception
	{
		final int floorCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		System.out.println( "Floor Count: " + floorCount ) ;
		_pos += 6 ;

		final int[] floors = new int[floorCount] ;
		for( int i = 0; i < floorCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			for( int j = 0; j < read.length; j++ )
			{
				ConvertBytes.printByte( read[j] ) ;
			}

			ConvertBytes.flipEndian( read ) ;
			floors[i] = ConvertBytes.toShort( read, 0, 2 ) & 0xFFFF  ;
			System.out.println( "Floor: " + floors[i] ) ;

			switch( floors[i] )
			{
				case 0  : _pos = decodeFloorType0( _pos, _stream ) ; break ;
				case 1  : _pos = decodeFloorType1( _pos, _stream ) ; break ;
				default : throw new Exception( "Unknown floor type: " + floors[i] ) ;
			}
		}
		
		return _pos ;
	}

	private int decodeFloorType0( int _pos, final byte[] _stream ) throws Exception
	{
		final int order = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
		final byte[] readRate = ConvertBytes.toBits( _stream, 0, _pos += 8, 16 ) ;
		ConvertBytes.flipEndian( readRate ) ;
		final int rate = ConvertBytes.toShort( readRate, 0, 2 ) & 0xFFFF  ;

		final byte[] readbarkMapSize = ConvertBytes.toBits( _stream, 0, _pos += 16, 16 ) ;
		ConvertBytes.flipEndian( readbarkMapSize ) ;
		final int barkMapSize = ConvertBytes.toShort( readbarkMapSize, 0, 2 ) & 0xFFFF  ;

		final int amplitude = ( ConvertBytes.toBits( _stream, 0, _pos += 16, 6 )[0] & 0xFF ) ;
		final int amplitudeOffset = ( ConvertBytes.toBits( _stream, 0, _pos += 6, 8 )[0] & 0xFF ) ;
		final int numBooks = ( ConvertBytes.toBits( _stream, 0, _pos += 8, 4 )[0] & 0xFF ) + 1 ;
		_pos += 4 ;

		for( int j = 0; j < numBooks; j++ )
		{
			final int bookList = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
			_pos += 8 ;
		}

		return _pos ;
	}

	private int decodeFloorType1( int _pos, final byte[] _stream ) throws Exception
	{
	
		return _pos ;
	}

	private float unpackFloat( final byte[] _pack )
	{
		ConvertBytes.flipEndian( _pack ) ;
		final int x = ConvertBytes.toInt( _pack, 0, 4 ) ;

		int mantissa = x & 0x1FFFFF ;
		final int sign = x & 0x80000000 ;
		final int exponent = ( x & 0x7fe00000 ) >> 21 ;

		mantissa = ( sign != 0 ) ? 1 : mantissa ;
		return ( float )( mantissa * ( Math.pow( 2, ( exponent - 788 ) ) ) ) ;
	}

	private int getLookup1( final int _entries, final int _dimensions )
	{
		int i = 1 ;
		while( Math.pow( i, _dimensions ) <= _entries )
		{
			i += 1 ;
		}

		return --i ;
	}
	
	private int iLog( int _x )
	{
		int value = 0 ;
		while( _x > 0 )
		{
			++value ;
			_x = _x >> 1 ;
		}

		return value ;
	}

	private String testILog()
	{
		final StringBuffer buffer = new StringBuffer() ;
		for( int i = -1; i < 8; ++i )
		{
			buffer.append( "Pass: " + i + " Result: " + iLog( i ) + "\n" ) ;
		}

		return buffer.toString() ;
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

		buffer.append( "Codebooks: " + codebooks.size() ) ;
		/*for( final CodebookConfiguration book : codebooks )
		{
			buffer.append( book.toString() ) ;
		}*/

		return buffer.toString() ;
	}

	public static class CodebookConfiguration
	{
		int entry ;							// Denotes the entry of the codebook, for example 
											// the 5th codebook to be read in
		int dimensions ;
		long entries ;
		int[] codewordLengths = null ;		// Codeword Lengths length is the size of entries

		int lookupType ;
		float minValue ;
		float deltaValue ;
		boolean sequenceP ;
		int[] multiplicands = null ;		// Lookup Values is multiplicands length

		public float[] getVQLookupTable()
		{
			switch( lookupType )
			{
				case IMPLICIT_LOOKUP_TABLE : return getVQLookupTable1() ;
				case EXPLICIT_LOOKUP_TABLE : return getVQLookupTable2() ;
				default                    : return null ;
			}
		}

		private float[] getVQLookupTable1()
		{
			float last = 0.0f ;
			int indexDivisor = 1 ;
			final int lookupOffset = entry ;

			final float[] vq = new float[dimensions] ;
			for( int i = 0; i < dimensions; ++i )
			{
				final int multiplicandOffset = ( lookupOffset / indexDivisor ) % multiplicands.length ;
				vq[i] = multiplicands[multiplicandOffset] * deltaValue + minValue + last ;

				last = ( sequenceP == true ) ? vq[i] : last ;
				indexDivisor = indexDivisor * multiplicands.length ;
			}
			
			return vq ;
		}

		private float[] getVQLookupTable2()
		{
			float last = 0.0f ;
			final int lookupOffset = entry ;
			int multiplicandOffset = lookupOffset * dimensions ;

			final float[] vq = new float[dimensions] ;
			for( int i = 0; i < dimensions; ++i )
			{
				vq[i] = multiplicands[multiplicandOffset] * deltaValue + minValue + last ;

				last = ( sequenceP == true ) ? vq[i] : last ;
				++multiplicandOffset ;
			}

			return vq ;
		}

		public String toString()
		{
			final StringBuffer buffer = new StringBuffer() ;
			buffer.append( "Dimensions: " + dimensions + "\n" ) ;
			buffer.append( "Entries: " + entries + "\n" ) ;
			if( lookupType != NO_LOOKUP_TABLE )
			{
				buffer.append( "Min: " + minValue + "\n" ) ;
				buffer.append( "Delta: " + deltaValue + "\n" ) ;
				buffer.append( "sequenceP: " + sequenceP + "\n" ) ;
				buffer.append( "Multiplicands: " + multiplicands.length + "\n" ) ;
			}

			/*for( int i = 0; i < entries; ++i )
			{
				buffer.append( "Entry: " + i + " Codeword Length: " + codewordLengths[i] + "\n" ) ;
			}

			if( multiplicands != null )
			{
				for( int i = 0; i < multiplicands.length; ++i )
				{
					buffer.append( "Index: " + i + " Multiplicands: " + multiplicands[i] + "\n" ) ;
				}
			}*/

			return buffer.toString() ;
		}
	}

	/**
		Contains the information required to quickly decode the headers.
	**/
	public static class VorbisHeader
	{
		public final int headerType ;	// ID_HEADER_TYPE, COMMENT_HEADER_TYPE, & SETUP_HEADER_TYPE are valid values
		public final int start ;		// The start of header information skips default header data
		public final Page page ;		// Page the data is located on

		public VorbisHeader( final int _headerType, final int _start, final Page _page )
		{
			headerType = _headerType ;
			start = _start ;
			page = _page ;
		}
	}
}