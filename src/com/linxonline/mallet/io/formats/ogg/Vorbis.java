package com.linxonline.mallet.io.formats.ogg ;

import java.util.List ;
import java.lang.Exception ;

import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.MalletList ;

/**
	Vorbis integer variables are stored in little endian.
**/
public final class Vorbis
{
	private final static int ID_HEADER_TYPE = 1 ;
	private final static int COMMENT_HEADER_TYPE = 3 ;
	private final static int SETUP_HEADER_TYPE = 5 ;

	private final static int NO_LOOKUP_TABLE = 0 ;
	private final static int IMPLICIT_LOOKUP_TABLE = 1 ;
	private final static int EXPLICIT_LOOKUP_TABLE = 2 ;
	
	private final static int UNUSED = -1 ;
	
	private List<VorbisHeader> headers = MalletList.<VorbisHeader>newList() ;

	private int version = -1 ;
	private byte audioChannels = 0 ;
	private int sampleRate = 0 ;
	private int bitrateMax = 0 ;
	private int bitrateNom = 0 ;
	private int bitrateMin = 0 ;
	private int blocksize0 = 0 ;
	private int blocksize1 = 0 ;

	private String vendor = null ;
	private final List<String> statements = MalletList.<String>newList() ;

	private final List<CodebookConfiguration> codebooks = MalletList.<CodebookConfiguration>newList() ;
	private final List<FloorConfiguration> floors = MalletList.<FloorConfiguration>newList() ;
	private final List<ResidueConfiguration> residues = MalletList.<ResidueConfiguration>newList() ;
	private final List<Mapping0Configuration> mappings = MalletList.<Mapping0Configuration>newList() ;
	private final List<ModeConfiguration> modes = MalletList.<ModeConfiguration>newList() ;

	public Vorbis() {}

	public void decode( final OGG _ogg ) throws Exception
	{
		final List<Page> pages = _ogg.pages ;
		for( int i = 0; i < 3; ++i )
		{
			// First 3 pages should be headers.
			final Page page = pages.get( i ) ;
			decodeHeaders( page )  ;
		}

		// All pages afterwards should be audio-packets.
		final int size = pages.size() ;
		for( int i = 3; i < size; ++i )
		{
			System.out.println( "Audio Page" ) ;
			final Page page = pages.get( i ) ;
			final AudioPacket packet = decodeAudioPacket( page ) ;
			if( packet.isAudioPacket() == false )
			{
				continue ;
			}

			packet.decode() ;
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
		version = ConvertBytes.toInt( stream, pos ) ;

		audioChannels = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] ;	// unsigned byte

		ConvertBytes.flipEndian( stream, pos += 1, 4 ) ;
		sampleRate = ConvertBytes.toInt( stream, pos ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateMax = ConvertBytes.toInt( stream, pos ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateNom = ConvertBytes.toInt( stream, pos ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		bitrateMin = ConvertBytes.toInt( stream, pos ) ;

		final byte blocksize = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] ; // unsigned byte
		blocksize0 = ( blocksize & 0x0F ) ;
		blocksize1 = ( ( blocksize >> 4 ) & 0x0F );

		blocksize0 = 1 << blocksize0 ;
		blocksize1 = 1 << blocksize1 ;

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
		final int vendorLength = ConvertBytes.toInt( stream, pos ) ;
		vendor = new String( ConvertBytes.toBytes( stream, pos += 4, vendorLength ) ) ;

		ConvertBytes.flipEndian( stream, pos += vendorLength, 4 ) ;
		final int iterate = ConvertBytes.toInt( stream, pos ) ;

		int length = 4 ; // Set to 4 to offset the iterate variable
		for( int i = 0; i < iterate; i++ )
		{
			ConvertBytes.flipEndian( stream, pos += length, 4 ) ;
			length = ConvertBytes.toInt( stream, pos ) ;
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
		pos = decodeResidues( pos, stream ) ;
		pos = decodeMappings( pos, stream ) ;
		pos = decodeModes( pos, stream ) ;

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
		final long syncPattern = ConvertBytes.toInt( syncBytes, 0 ) ;
		if( syncPattern != 5653314 )
		{
			throw new Exception( "OGG not in sync, failed to read correct Sync Pattern." ) ;
		}

		// Dimensions
		final byte[] dimBytes = ConvertBytes.toBits( _stream, 0, _pos += 24, 16 ) ;
		ConvertBytes.flipEndian( dimBytes, 0, 2 ) ;
		final int dimensions = ConvertBytes.toShort( dimBytes, 0 ) & 0xFFFF ;

		// Entries
		final byte[] tempEntry = ConvertBytes.toBits( _stream, 0, _pos += 16, 24 ) ;
		final byte[] entryBytes = new byte[4] ;
		entryBytes[0] = tempEntry[0] ;
		entryBytes[1] = tempEntry[1] ;
		entryBytes[2] = tempEntry[2] ;
		entryBytes[3] = 0 ;

		ConvertBytes.flipEndian( entryBytes, 0, 4 ) ;
		final long entries = ConvertBytes.toInt( entryBytes, 0 ) & 0xFFFFFFFFL ;

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
		book.root = Huffman.build( book.codewordLengths, ( int )book.entries ) ;

		codebooks.add( book ) ;
		return _pos ;
	}

	private int decodeTimeDomain( int _pos, final byte[] _stream ) throws Exception
	{
		final int timeCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		_pos += 6 ;

		for( int i = 0; i < timeCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			ConvertBytes.flipEndian( read ) ;
			final int time = ConvertBytes.toShort( read, 0 ) & 0xFFFF  ;
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
		_pos += 6 ;

		for( int i = 0; i < floorCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			ConvertBytes.flipEndian( read ) ;
			final int floor = ConvertBytes.toShort( read, 0 ) & 0xFFFF  ;

			switch( floor )
			{
				case 0  : _pos = decodeFloorType0( _pos, _stream ) ; break ;
				case 1  : _pos = decodeFloorType1( _pos, _stream ) ; break ;
				default : throw new Exception( "Unknown floor type: " + floor ) ;
			}
		}

		return _pos ;
	}

	private int decodeFloorType0( int _pos, final byte[] _stream ) throws Exception
	{
		final Floor0Configuration floor = new Floor0Configuration() ;
		_pos = floor.decodeHeader( _pos, _stream ) ;
		floors.add( floor ) ;
		return _pos ;
	}

	private int decodeFloorType1( int _pos, final byte[] _stream ) throws Exception
	{
		final Floor1Configuration floor = new Floor1Configuration() ;
		_pos = floor.decodeHeader( _pos, _stream ) ;
		floors.add( floor ) ;
		return _pos ;
	}

	private int decodeResidues( int _pos, final byte[] _stream ) throws Exception
	{
		final int residueCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		_pos += 6 ;

		final int[] residues = new int[residueCount] ;
		for( int i = 0; i < residueCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			ConvertBytes.flipEndian( read ) ;
			final int residue = ConvertBytes.toShort( read, 0 ) & 0xFFFF  ;

			switch( residue )
			{
				case 0  : _pos = decodeResidueType0( _pos, _stream ) ; break ;
				case 1  : _pos = decodeResidueType1( _pos, _stream ) ; break ;
				case 2  : _pos = decodeResidueType2( _pos, _stream ) ; break ;
				default : throw new Exception( "Unknown residue type: " + residue ) ;
			}
		}

		return _pos ;
	}

	private int decodeResidueType0( int _pos, final byte[] _stream ) throws Exception
	{
		final Residue0Configuration residue = new Residue0Configuration() ;
		_pos = residue.decodeHeader( _pos, _stream ) ;
		residues.add( residue ) ;
		return _pos ;
	}

	private int decodeResidueType1( int _pos, final byte[] _stream ) throws Exception
	{
		final Residue1Configuration residue = new Residue1Configuration() ;
		_pos = residue.decodeHeader( _pos, _stream ) ;
		residues.add( residue ) ;
		return _pos ;
	}

	private int decodeResidueType2( int _pos, final byte[] _stream ) throws Exception
	{
		final Residue2Configuration residue = new Residue2Configuration() ;
		_pos = residue.decodeHeader( _pos, _stream ) ;
		residues.add( residue ) ;
		return _pos ;
	}

	private int decodeMappings( int _pos, final byte[] _stream ) throws Exception
	{
		final int mappingCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		_pos += 6 ;

		for( int i = 0; i < mappingCount; i++ )
		{
			final byte[] read = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			_pos += 16 ;

			ConvertBytes.flipEndian( read ) ;
			final int mapping = ConvertBytes.toShort( read, 0 ) & 0xFFFF  ;

			switch( mapping )
			{
				case 0  : _pos = decodeMappingType0( _pos, _stream ) ; break ;
				default : throw new Exception( "Unknown mapping type: " + mapping ) ;
			}
		}

		return _pos ;
	}

	private int decodeMappingType0( int _pos, final byte[] _stream ) throws Exception
	{
		final Mapping0Configuration mapping = new Mapping0Configuration() ;
		_pos = mapping.decodeHeader( _pos, _stream ) ;
		mappings.add( mapping ) ;

		System.out.println( mapping.toString() ) ;
		return _pos ;
	}

	private int decodeModes( int _pos, final byte[] _stream ) throws Exception
	{
		final int modeCount = ( ConvertBytes.toBits( _stream, 0, _pos, 6 )[0] & 0xFF ) + 1 ;
		_pos += 6 ;

		System.out.println( "Mode Count: " + modeCount ) ;

		for( int i = 0; i < modeCount; i++ )
		{
			final ModeConfiguration mode = new ModeConfiguration() ;
			_pos = mode.decodeHeader( _pos, _stream ) ;
			modes.add( mode ) ;
		}

		if( ConvertBytes.isBitSet( _stream, _pos++ ) == false )
		{
			throw new Exception( "Mode framing bit not set." ) ;
		}

		return _pos ;
	}

	private AudioPacket decodeAudioPacket( final Page _page ) throws Exception
	{
		int pos = 0 ;
		final byte[] stream = _page.data ;

		final AudioPacket packet = new AudioPacket( pos, stream ) ;
		return packet ;
	}

	private float unpackFloat( final byte[] _pack )
	{
		ConvertBytes.flipEndian( _pack ) ;
		final int x = ConvertBytes.toInt( _pack, 0 ) ;

		int mantissa = x & 0x1FFFFF ;
		final int sign = x & 0x80000000 ;
		final int exponent = ( x & 0x7fe00000 ) >> 21 ;

		mantissa = ( sign != 0 ) ? 1 : mantissa ;
		return ( float )( mantissa * ( 1 << ( exponent - 788 ) ) ) ;
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
	
	private static int iLog( int _x )
	{
		int value = 0 ;
		while( _x > 0 )
		{
			++value ;
			_x = _x >> 1 ;
		}

		return value ;
	}

	private static String testILog()
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

		buffer.append( "Codebooks: " + codebooks.size() + "\n" ) ;
		buffer.append( "Floors: " + floors.size() + "\n" ) ;
		
		/*for( final CodebookConfiguration book : codebooks )
		{
			buffer.append( book.toString() ) ;
		}*/

		return buffer.toString() ;
	}

	public final class Floor1Configuration implements FloorConfiguration
	{
		private final int[] RANGES = { 256, 128, 86, 64 } ;

		private int partitions = 0 ;
		private int[] partitionClassList = null ;
		private int[] classDimensions = null ;
		private int[] subClasses = null ;
		private int[] masterbooks = null ;
		private int[][] subClassBooks = null ;
		private int[] xList = null ;

		private int multiplier = 0 ;

		@Override
		public int decodeHeader( int _pos, final byte[] _stream )
		{
			partitions = ( ConvertBytes.toBits( _stream, 0, _pos, 5 )[0] & 0xFF ) ;
			_pos += 5 ;

			partitionClassList = new int[partitions] ;
			int maximumClass = -1 ;

			for( int i = 0; i < partitionClassList.length; i++ )
			{
				partitionClassList[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 4 )[0] & 0xFF ) ;
				_pos += 4 ;

				if( partitionClassList[i] > maximumClass )
				{
					maximumClass = partitionClassList[i] ;
				}
			}

			maximumClass += 1 ;

			classDimensions = new int[maximumClass] ;
			subClasses = new int[maximumClass] ;
			masterbooks = new int[maximumClass] ;
			subClassBooks = new int[maximumClass][] ;

			for( int i = 0; i < maximumClass; i++ )
			{
				classDimensions[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 3 )[0] & 0xFF ) + 1 ;
				subClasses[i] = ( ConvertBytes.toBits( _stream, 0, _pos += 3, 2 )[0] & 0xFF ) ;
				_pos += 2 ;

				if( subClasses[i] != 0 )
				{
					masterbooks[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
					_pos += 8 ;

				}

				subClassBooks[i] = new int[1 << subClasses[i]] ;
				for( int j = 0; j < subClassBooks[i].length; j++ )
				{
					subClassBooks[i][j] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) - 1 ;
					_pos += 8 ;
				}
			}

			multiplier = ( ConvertBytes.toBits( _stream, 0, _pos, 2 )[0] & 0xFF ) + 1 ;
			final int rangeBits = ( ConvertBytes.toBits( _stream, 0, _pos += 2, 4 )[0] & 0xFF ) ;
			_pos += 4 ;

			int xListLength = 2 ;
			for( int i = 0; i < partitionClassList.length; i++ )
			{
				final int classNumber = partitionClassList[i] ;
				xListLength += classDimensions[classNumber] ;
			}

			if( xListLength > 65 )
			{
				throw new RuntimeException( "xList exceeds limit of 65." ) ;
			}

			xList = new int[xListLength] ;
			xList[0] = 0 ;
			xList[1] = 1 << rangeBits ;

			int values = 2 ;
			for( int i = 0; i < partitionClassList.length; i++ )
			{
				final int classNumber = partitionClassList[i] ;
				for( int j = 0; j < classDimensions[classNumber]; j++ )
				{
					xList[values++] = ( ConvertBytes.toBits( _stream, 0, _pos, rangeBits )[0] & 0xFF ) ;
					_pos += rangeBits ;
				}
			}

			if( isUnique( xList ) == false )
			{
				throw new RuntimeException( "xList does not contain only unique values." ) ;
			}

			return _pos ;
		}

		@Override
		public int type()
		{
			return 1 ;
		}

		public int decodePacket( int _pos, final byte[] _stream, final DecodedFloor1 _floor )
		{
			_floor.unused = false ;
			_floor.y = null ;

			if( ConvertBytes.isBitSet( _stream, _pos++ ) == false )
			{
				System.out.println( "Floor not used" ) ;
				_floor.unused = true ;
				return _pos ;
			}

			final int range = RANGES[multiplier - 1] ;
			final int bitsToRead = iLog( range - 1 ) ;

			_floor.y = new int[xList.length] ;
			_floor.y[0] = ( ConvertBytes.toBits( _stream, 0, _pos, bitsToRead )[0] & 0xFF ) ;
			_floor.y[1] = ( ConvertBytes.toBits( _stream, 0, _pos += bitsToRead, bitsToRead )[0] & 0xFF ) ;
			_pos += bitsToRead ;

			int offset = 2 ;
			for( int i = 0; i < partitionClassList.length; i++ )
			{
				final int classNumber = partitionClassList[i] ;
				final int cdim = classDimensions[classNumber] ;
				final int cbits = subClasses[classNumber] ;
				final int csub = ( 1 << cbits ) - 1 ;

				int cval = 0 ;
				if( cbits > 0 )
				{
					final CodebookConfiguration book = codebooks.get( masterbooks[classNumber] ) ;
					int codewordLength = 0 ;
					int number = -1 ;
					while( number == -1 )
					{
						codewordLength += 1 ;
						final byte[] codeword = ConvertBytes.toBits( _stream, 0, _pos, codewordLength ) ;
						number = book.getEntry( codeword, codewordLength ) ;
					}

					cval = number ;
					_pos += codewordLength ;
				}

				for( int j = 0; j < cdim; j++ )
				{
					final int bookIndex = subClassBooks[classNumber][cval & csub] ;
					cval = cval >>>= cbits ;

					if( bookIndex >= 0 )
					{
						final CodebookConfiguration book = codebooks.get( bookIndex ) ;
						int codewordLength = 0 ;
						int number = -1 ;
						while( number == -1 )
						{
							codewordLength += 1 ;
							final byte[] codeword = ConvertBytes.toBits( _stream, 0, _pos, codewordLength ) ;
							number = book.getEntry( codeword, codewordLength ) ;
						}

						_floor.y[j + offset] = number ;
						_pos += codewordLength ;
					}
					else
					{
						_floor.y[j + offset] = 0 ;
					}
				}

				offset += cdim ;
			}

			// TODO: 7.2.4. curve computation 
			return _pos ;
		}
	
		private boolean isUnique( final int[] _xList )
		{
			for( int i = 0; i < _xList.length; i++ )
			{
				for( int j = 0; j < _xList.length; j++ )
				{
					if( _xList[i] == _xList[j] && i != j )
					{
						return false ;
					}
				}
			}

			return true ;
		}
	}

	public final static class Floor0Configuration implements FloorConfiguration
	{
		private int order = 0 ;
		private int rate = 0 ;
		private int barkMapSize = 0 ;
		private int amplitudeBits = 0 ;
		private int amplitudeOffset = 0 ;
		private int[] bookList = null ;

		@Override
		public int decodeHeader( int _pos, final byte[] _stream )
		{
			int order = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
			final byte[] readRate = ConvertBytes.toBits( _stream, 0, _pos += 8, 16 ) ;
			ConvertBytes.flipEndian( readRate ) ;
			int rate = ConvertBytes.toShort( readRate, 0 ) & 0xFFFF  ;

			final byte[] readbarkMapSize = ConvertBytes.toBits( _stream, 0, _pos += 16, 16 ) ;
			ConvertBytes.flipEndian( readbarkMapSize ) ;
			int barkMapSize = ConvertBytes.toShort( readbarkMapSize, 0 ) & 0xFFFF  ;

			amplitudeBits = ( ConvertBytes.toBits( _stream, 0, _pos += 16, 6 )[0] & 0xFF ) ;
			amplitudeOffset = ( ConvertBytes.toBits( _stream, 0, _pos += 6, 8 )[0] & 0xFF ) ;

			final int numBooks = ( ConvertBytes.toBits( _stream, 0, _pos += 8, 4 )[0] & 0xFF ) + 1 ;
			_pos += 4 ;

			bookList = new int[numBooks] ;

			for( int i = 0; i < numBooks; i++ )
			{
				bookList[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
				_pos += 8 ;
			}

			return _pos ;
		}

		@Override
		public int type()
		{
			return 0 ;
		}

		public int decodePacket( int _pos, final byte[] _stream )
		{
			final int amplitude = ( ConvertBytes.toBits( _stream, 0, _pos, amplitudeBits )[0] & 0xFF ) ;
			_pos += amplitudeBits ;

			if( amplitude > 0 )
			{
			
			}

			return _pos ;
		}
	}

	public interface FloorConfiguration
	{
		public int decodeHeader( int _pos, final byte[] _stream ) throws Exception ;
		public int type() ;
	}

	public final class Residue0Configuration extends ResidueConfiguration
	{
		@Override
		public int type()
		{
			return 0 ;
		}
	}

	public final class Residue1Configuration extends ResidueConfiguration
	{
		@Override
		public int type()
		{
			return 1 ;
		}
	}

	public final class Residue2Configuration extends ResidueConfiguration
	{
		@Override
		public int type()
		{
			return 2 ;
		}
	}

	public abstract class ResidueConfiguration
	{
		public long begin ;
		public long end ;
		public long partitionSize ;
		public int classifications ;
		public int classbook ;
		public int[][] books ;

		public int decodeHeader( int _pos, final byte[] _stream ) throws Exception
		{
			final byte[] tempBegin = ConvertBytes.toBits( _stream, 0, _pos, 24 ) ;
			final byte[] beginBytes = new byte[4] ;
			beginBytes[0] = tempBegin[0] ;
			beginBytes[1] = tempBegin[1] ;
			beginBytes[2] = tempBegin[2] ;
			beginBytes[3] = 0 ;

			ConvertBytes.flipEndian( beginBytes, 0, 4 ) ;
			begin = ConvertBytes.toInt( beginBytes, 0 ) & 0xFFFFFFFFL ;

			final byte[] tempEnd = ConvertBytes.toBits( _stream, 0, _pos += 24, 24 ) ;
			final byte[] endBytes = new byte[4] ;
			endBytes[0] = tempEnd[0] ;
			endBytes[1] = tempEnd[1] ;
			endBytes[2] = tempEnd[2] ;
			endBytes[3] = 0 ;

			ConvertBytes.flipEndian( endBytes, 0, 4 ) ;
			end = ConvertBytes.toInt( endBytes, 0 ) & 0xFFFFFFFFL ;

			final byte[] tempPartition = ConvertBytes.toBits( _stream, 0, _pos += 24, 24 ) ;
			final byte[] partitionBytes = new byte[4] ;
			partitionBytes[0] = tempPartition[0] ;
			partitionBytes[1] = tempPartition[1] ;
			partitionBytes[2] = tempPartition[2] ;
			partitionBytes[3] = 0 ;

			ConvertBytes.flipEndian( partitionBytes, 0, 4 ) ;
			partitionSize = ( ConvertBytes.toInt( partitionBytes, 0 ) & 0xFFFFFFFFL ) + 1L ;

			classifications = ( ConvertBytes.toBits( _stream, 0, _pos += 24, 6 )[0] & 0xFF ) + 1 ;
			classbook = ( ConvertBytes.toBits( _stream, 0, _pos += 6, 8 )[0] & 0xFF ) ;
			_pos += 8 ;

			final byte[] cascade = new byte[classifications] ;
			for( int i = 0; i < classifications; ++i )
			{
				int highBits = 0 ;
				final int lowBits = ( ConvertBytes.toBits( _stream, 0, _pos, 3 )[0] & 0xFF ) ;
				_pos += 3 ;

				if( ConvertBytes.isBitSet( _stream, _pos++ ) == true )
				{
					highBits = ( ConvertBytes.toBits( _stream, 0, _pos, 5 )[0] & 0xFF ) ;
					_pos += 5 ;
				}

				cascade[i] = ( byte )( highBits * 8 + lowBits ) ;
			}

			books = new int[classifications][8] ;
			for( int i = 0; i < classifications; ++i )
			{
				// 8 in Vorbis I, as constrained by the elements of the cascade bitmap being eight bits
				for( int j = 0; j < 8; ++j )
				{
					if( ConvertBytes.isBitSet( cascade[i], j ) == true )
					{
						books[i][j] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
						_pos += 8 ;
					}
					else
					{
						books[i][j] = UNUSED ;
					}
				}
			}

			return _pos ;
		}

		public abstract int type() ;

		public int decodePacket( int _pos, final byte[] _stream, final int _blocksize, final int _ch, final boolean[] _doNotDecode, final float[][] _values ) throws Exception
		{
			final int type = type() ;
			int actualSize = _blocksize / 2 ;
			if( type == 2 )
			{
				actualSize = actualSize * _ch ;
			}

			final int limitBegin = ( begin < actualSize ) ? ( int )begin : actualSize ;
			final int limitEnd = ( end < actualSize ) ? ( int )end : actualSize ;
			final int toRead = limitEnd - limitBegin ;

			final CodebookConfiguration book = codebooks.get( classbook ) ;
			final int codeword = book.dimensions ;
			final int partitionsToRead = toRead / ( int )partitionSize ;

			final int[][] dClassifications = new int[_ch][codeword * partitionsToRead] ;
			for( int i = 0; i < _values.length; ++i )
			{
				_values[i] = new float[_blocksize] ;
			}

			if( toRead <= 0 )
			{
				return _pos ;
			}

			for( int pass = 0; pass <= 7; ++pass )
			{
				int partitionCount = 0 ;
				while( partitionCount < partitionsToRead )
				{
					if( pass == 0 )
					{
						for( int j = 0; j < _ch; ++j )
						{
							if( _doNotDecode[j] )
							{
								continue ;
							}

							int codewordLength = 0 ;
							int temp = -1 ;
							while( temp == -1 )
							{
								codewordLength += 1 ;
								final byte[] t = ConvertBytes.toBits( _stream, 0, _pos, codewordLength ) ;
								temp = book.getEntry( t, codewordLength ) ;
							}

							_pos += codewordLength ;

							for( int i = codeword - 1; i >= 0; --i )
							{
								dClassifications[j][i + partitionCount] = temp % classifications ;
								temp = temp / classifications ;
							}
						}
					}

					for( int i = 0; i < codeword; ++i )
					{
						if( partitionCount >= partitionsToRead )
						{
							break ;
						}

						final int offset = ( int )begin + partitionCount * ( int )partitionSize ;

						switch( type )
						{
							case 0 :
							case 1 :
							{
								for( int j = 0; j < _ch; ++j )
								{
									if( _doNotDecode[j] )
									{
										continue ;
									}

									final int vqClass = dClassifications[j][partitionCount] ;
									final int vqBook = books[vqClass][pass] ;
									if( vqBook == -1 )
									{
										continue ;
									}

									final CodebookConfiguration cBook = codebooks.get( vqBook ) ;

									switch( type )
									{
										case 0 :
										{
											_pos = decodeResidue0( _pos, _stream, cBook, offset, _values[j] ) ;
											break ;
										}
										case 1 :
										{
											_pos = decodeResidue1( _pos, _stream, cBook, offset, _values[j] ) ;
											break ;
										}
									}
								}
								break ;
							}
							case 2 :
							{
								final float[] values = new float[_ch * _blocksize] ;
								_pos = decodeResidue2( _pos, _stream, book, offset, values ) ;
								break ;
							}
						}

						++partitionCount ;
					}
				}
			}

			System.out.println( "Actual Size: " + actualSize + " Limit Begin: " + limitBegin + " Limit End: " + limitEnd ) ;
			return _pos ;
		}

		private int decodeResidue0( int _pos, final byte[] _stream, final CodebookConfiguration _book, final int _offset, final float[] _values )
		{
			//System.out.println( "Decode Residue 0" ) ;
			//System.out.println( "Values Length: " + _values.length ) ;
			final float[] temp = new float[_book.dimensions] ;

			final int step = ( int )partitionSize / _book.dimensions ;
			for( int i = 0; i < step; ++i )
			{
				int codewordLength = 0 ;
				boolean success = false ;
				while( success == false )
				{
					codewordLength += 1 ;
					final byte[] t = ConvertBytes.toBits( _stream, 0, _pos, codewordLength ) ;
					success = _book.getVQLookupTable( t, codewordLength, temp ) ;
				}

				_pos += codewordLength ;

				for( int j = 0; j < _book.dimensions; ++j )
				{
					_values[_offset + i + j * step] += temp[j] ;
					//System.out.println( "Index: " + ( _offset + i + j * step ) + " RV: " + _values[_offset + i + j * step] ) ;
				}
			}

			return _pos ;
		}

		private int decodeResidue1( int _pos, final byte[] _stream, final CodebookConfiguration _book, final int _offset, final float[] _values )
		{
			//System.out.println( "Decode Residue 1" ) ;
			final float[] temp = new float[_book.dimensions] ;

			int i = 0 ;

			while( i < partitionSize )
			{
				int codewordLength = 0 ;
				boolean success = false ;
				while( success == false )
				{
					codewordLength += 1 ;
					final byte[] t = ConvertBytes.toBits( _stream, 0, _pos, codewordLength ) ;
					success = _book.getVQLookupTable( t, codewordLength, temp ) ;
				}

				_pos += codewordLength ;

				for( int j = 0; j < _book.dimensions; ++j )
				{
					_values[_offset + i] += temp[j] ;
					//System.out.println( "Index: " + ( _offset + i ) + " RV: " + _values[_offset + i] ) ;
					++i ;
				}
			}

			return _pos ;
		}

		private int decodeResidue2( int _pos, final byte[] _stream, final CodebookConfiguration _book, final int _offset, final float[] _values )
		{
			//System.out.println( "Decode Residue 2" ) ;
			_pos = decodeResidue1( _pos, _stream, _book, _offset, _values ) ;
			return _pos ;
		}

		@Override
		public String toString()
		{
			final StringBuffer buffer = new StringBuffer() ;
			buffer.append( "Begin: " + begin + "\n" ) ;
			buffer.append( "End: " + end + "\n" ) ;
			buffer.append( "Partition Size: " + partitionSize + "\n" ) ;
			buffer.append( "Classifications: " + classifications + "\n" ) ;
			buffer.append( "Classbook: " + classbook + "\n" ) ;

			for( int i = 0; i < books.length; ++i )
			{
				buffer.append( "Book: " + i ) ;
				final int size = books[i].length ;
				for( int j = 0; j < size; ++j )
				{
					buffer.append( ", " ) ;
					buffer.append( books[i][j] ) ;
				}

				buffer.append( "\n" ) ;
			}

			return buffer.toString() ;
		}
	}

	public final class Mapping0Configuration implements MappingConfiguration
	{
		public int subMaps ;
		public int couplingSteps ;

		public int[] magnitudes ;
		public int[] angles ;

		public int[] muxs ;

		public int[] subMapFloors ;
		public int[] subMapResidues ;

		@Override
		public int decodeHeader( int _pos, final byte[] _stream ) throws Exception
		{
			if( ConvertBytes.isBitSet( _stream, _pos++ ) == true )
			{
				subMaps = ( ConvertBytes.toBits( _stream, 0, _pos, 4 )[0] & 0xFF ) + 1 ;
				_pos += 4 ;
			}
			else
			{
				subMaps = 1 ;
			}

			if( ConvertBytes.isBitSet( _stream, _pos++ ) == true )
			{
				couplingSteps = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) + 1 ;
				_pos += 8 ;
				magnitudes = new int[couplingSteps] ;
				angles = new int[couplingSteps] ;

				for( int i = 0; i < couplingSteps; ++i )
				{
					final int toRead = iLog( ( int )( audioChannels - 1 ) ) ;

					magnitudes[i] = ( ConvertBytes.toBits( _stream, 0, _pos, toRead )[0] ) & 0xFF ;
					_pos += toRead ;

					angles[i] = ( ConvertBytes.toBits( _stream, 0, _pos, toRead )[0] ) & 0xFF ;
					_pos += toRead ;

					System.out.println( "Magnitude: " + magnitudes[i] ) ;
					System.out.println( "Angle: " + angles[i] ) ;
				}
			}
			else
			{
				couplingSteps = 0 ;
			}

			final int reserved = ( ConvertBytes.toBits( _stream, 0, _pos, 2 )[0] & 0xFF ) ;
			_pos += 2 ;
			if( reserved != 0 )
			{
				throw new Exception( "Mapping reserved value is expected to be 0 but got: " + reserved ) ;
			}

			muxs = new int[audioChannels] ;
			if( subMaps > 1 )
			{
				for( int i = 0; i < audioChannels; ++i )
				{
					muxs[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 4 )[0] & 0xFF ) ;
					_pos += 4 ;
					if( muxs[i] > subMaps )
					{
						throw new Exception( "Mapping mux: " + muxs[i] + " is greater than subMap: " + subMaps ) ;
					}
				}
			}

			final int floorSize = floors.size() ;
			final int residueSize = residues.size() ;

			subMapFloors = new int[subMaps] ;
			subMapResidues = new int[subMaps] ;
			for( int i = 0; i < subMaps; ++i )
			{
				_pos += 8 ;		// discard 8 bits (the unused time configuration placeholder)
				subMapFloors[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
				_pos += 8 ;
				if( subMapFloors[i] > floorSize )
				{
					throw new Exception( "Mapping sub map floor exceeds available floors: " + subMapFloors[i] ) ;
				}
				
				subMapResidues[i] = ( ConvertBytes.toBits( _stream, 0, _pos, 8 )[0] & 0xFF ) ;
				_pos += 8 ;
				if( subMapResidues[i] > residueSize )
				{
					throw new Exception( "Mapping sub map residue exceeds available residues: " + subMapResidues[i] ) ;
				}
			}

			return _pos ;
		}
	}

	public interface MappingConfiguration
	{
		public int decodeHeader( int _pos, final byte[] _stream ) throws Exception ;
	}

	public final class ModeConfiguration
	{
		public boolean blockFlag ;
		public int windowType ;
		public int transformType ;
		public int mapping ;
	
		public int decodeHeader( int _pos, final byte[] _stream ) throws Exception
		{
			blockFlag = ConvertBytes.isBitSet( _stream, _pos++ ) ;
			System.out.println( "Block Flag: " + blockFlag ) ;

			final byte[] readWindowType = ConvertBytes.toBits( _stream, 0, _pos, 16 ) ;
			ConvertBytes.printBytes( readWindowType, 0, readWindowType.length * 8 ) ;
			windowType = ConvertBytes.toShort( readWindowType, 0 ) & 0xFFFF  ;

			if( windowType != 0 )
			{
				throw new Exception( "Mode window type is meant to be zero: " + windowType ) ;
			}

			final byte[] readTransformType = ConvertBytes.toBits( _stream, 0, _pos += 16, 16 ) ;
			ConvertBytes.printBytes( readTransformType, 0, readTransformType.length * 8 ) ;
			transformType = ConvertBytes.toShort( readTransformType, 0 ) & 0xFFFF  ;

			if( transformType != 0 )
			{
				throw new Exception( "Mode window type is meant to be zero: " + windowType ) ;
			}

			mapping = ( ConvertBytes.toBits( _stream, 0, _pos += 16, 8 )[0] & 0xFF ) ;
			_pos += 8 ;

			if( mapping > mappings.size() )
			{
				throw new Exception( "Mode mapping: " + mapping + " exceeds available maps: " + mappings.size() ) ;
			}

			System.out.println( "Mapping: " + mapping ) ;
			return _pos ;
		}

		//public int decodePacket( int _pos, final byte[] _stream ) throws Exception ;
	}

	public final static class CodebookConfiguration
	{
		private int entry ;					// Denotes the entry of the codebook, for example 
											// the 5th codebook to be read in
		public int dimensions ;
		long entries ;
		int[] codewordLengths = null ;		// Codeword Lengths length is the size of entries

		int lookupType ;
		float minValue ;
		float deltaValue ;
		boolean sequenceP ;
		int[] multiplicands = null ;		// Lookup Values is multiplicands length
		Huffman.Node root = null ;

		public int getEntry( final byte[] _codeword, final int _length )
		{
			return root.get( _codeword, 0, _length ) ;
		}

		public boolean getVQLookupTable( final byte[] _codeword, final int _length, final float[] _fill )
		{
			return getVQLookupTable( _codeword, _length, 0, _fill ) ;
		}

		public boolean getVQLookupTable( final byte[] _codeword, final int _length, final int _offset, final float[] _fill )
		{
			switch( lookupType )
			{
				case IMPLICIT_LOOKUP_TABLE : return getVQLookupTable1( _codeword, _length, _offset, _fill ) ;
				case EXPLICIT_LOOKUP_TABLE : return getVQLookupTable2( _codeword, _length, _offset, _fill ) ;
				default                    : return true ;
			}
		}

		private boolean getVQLookupTable1( final byte[] _codeword, final int _length, final int _offset, final float[] _vq )
		{
			final int lookupOffset = getEntry( _codeword, _length ) ;
			if( lookupOffset == -1 )
			{
				return false ;
			}

			float last = 0.0f ;
			int indexDivisor = 1 ;

			for( int i = 0; i < dimensions; ++i )
			{
				final int index = _offset + i ;
				final int multiplicandOffset = ( lookupOffset / indexDivisor ) % multiplicands.length ;
				_vq[index] = multiplicands[multiplicandOffset] * deltaValue + minValue + last ;

				last = ( sequenceP == true ) ? _vq[index] : last ;
				indexDivisor = indexDivisor * multiplicands.length ;
			}

			return true ;
		}

		private boolean getVQLookupTable2( final byte[] _codeword, final int _length, final int _offset, final float[] _vq )
		{
			final int lookupOffset = getEntry( _codeword, _length ) ;
			if( lookupOffset == -1 )
			{
				return false ;
			}

			float last = 0.0f ;
			int multiplicandOffset = lookupOffset * dimensions ;

			for( int i = 0; i < dimensions; ++i )
			{
				final int index = _offset + i ;
				_vq[index] = multiplicands[multiplicandOffset] * deltaValue + minValue + last ;

				last = ( sequenceP == true ) ? _vq[index] : last ;
				++multiplicandOffset ;
			}

			return true ;
		}

		public String toString()
		{
			final StringBuffer buffer = new StringBuffer() ;
			buffer.append( "Entry: " + entry + "\n" ) ;
			buffer.append( "Dimensions: " + dimensions + "\n" ) ;
			buffer.append( "Entries: " + entries + "\n" ) ;
			if( lookupType != NO_LOOKUP_TABLE )
			{
				buffer.append( "Min: " + minValue + "\n" ) ;
				buffer.append( "Delta: " + deltaValue + "\n" ) ;
				buffer.append( "sequenceP: " + sequenceP + "\n" ) ;
				buffer.append( "Multiplicands: " + multiplicands.length + "\n" ) ;
			}

			for( int i = 0; i < entries; ++i )
			{
				buffer.append( "Entry: " + i + " Codeword Length: " + codewordLengths[i] + "\n" ) ;
			}

			if( multiplicands != null )
			{
				for( int i = 0; i < multiplicands.length; ++i )
				{
					buffer.append( "Index: " + i + " Multiplicands: " + multiplicands[i] + "\n" ) ;
				}
			}

			return buffer.toString() ;
		}
	}

	public final class AudioPacket
	{
		private final static double HALF_PI = Math.PI / 2 ;

		private boolean notAudio = true ;
		private final int startPosition ;
		private final byte[] stream ;

		private final ModeConfiguration mode ;
		private final Mapping0Configuration mapping ;

		private final int n ;

		private final boolean prevWindowFlag ;
		private final boolean nextWindowFlag ;

		private final int windowCenter ;

		private final int leftWindowStart ;
		private final int leftWindowEnd ;
		private final int leftN ;

		private final int rightWindowStart ;
		private final int rightWindowEnd ;
		private final int rightN ;

		private final double[] window ;

		private final DecodedFloor[] decodedFloors = new DecodedFloor[audioChannels] ;
		private final boolean[] noResidue = new boolean[audioChannels] ;
		private final boolean[] doNotDecode = new boolean[audioChannels] ;
		private final float[][][] residueValues ;

		public AudioPacket( int _pos, final byte[] _stream )
		{
			notAudio = ConvertBytes.isBitSet( _stream, _pos++ ) ;
			if( notAudio == true )
			{
				// A well constructed vorbis file is unlikely to have malformed
				// audio packets, but in case it does we'll not continue any further.
				startPosition = -1 ;
				stream = null ;

				mode = null ;
				mapping = null ;
				residueValues = null ;

				n = -1 ;

				prevWindowFlag = false ;
				nextWindowFlag = false ;
				windowCenter = -1 ;

				leftWindowStart = -1 ;
				leftWindowEnd = -1 ;
				leftN = -1 ;
				
				rightWindowStart = -1 ;
				rightWindowEnd = -1 ;
				rightN = -1 ;

				window = null ;

				return ;
			}

			final int toRead = iLog( modes.size() - 1 ) ;
			final int modeNumber = ( ConvertBytes.toBits( _stream, 0, _pos, toRead )[0] ) & 0xFF ;
			_pos += toRead ;

			mode = modes.get( modeNumber ) ;
			mapping = mappings.get( mode.mapping ) ;

			residueValues = new float[mapping.subMaps][][] ;

			n = ( mode.blockFlag == false ) ? blocksize0 : blocksize1 ;

			// long window
			prevWindowFlag = ( mode.blockFlag == true ) ? ConvertBytes.isBitSet( _stream, _pos++ ) : false ;
			nextWindowFlag = ( mode.blockFlag == true ) ? ConvertBytes.isBitSet( _stream, _pos++ ) : false ;

			windowCenter = ( int )( n / 2.0f ) ;

			leftWindowStart = ( mode.blockFlag == true && prevWindowFlag == false ) ? ( int )( n / 4.0f - blocksize0 / 4.0f ) : 0 ;
			leftWindowEnd   = ( mode.blockFlag == true && prevWindowFlag == false ) ? ( int )( n / 4.0f + blocksize0 / 4.0f ) : windowCenter ;
			leftN           = ( mode.blockFlag == true && prevWindowFlag == false ) ? ( blocksize0 / 2 ) : ( n / 2 ) ;

			rightWindowStart = ( mode.blockFlag == true && nextWindowFlag == false ) ? ( int )( n * 0.75f - blocksize0 / 4 ) : windowCenter ;
			rightWindowEnd   = ( mode.blockFlag == true && nextWindowFlag == false ) ? ( int )( n * 0.75f + blocksize0 / 4 ) : n ;
			rightN           = ( mode.blockFlag == true && nextWindowFlag == false ) ? ( blocksize0 / 2 ) : ( n / 2 ) ;

			window = computeWindow() ;

			startPosition = _pos ;
			stream = _stream ;
		}

		/**
			It's possible for an Audio Packet to be constructed
			but malformed, in which case it should not be used.
			Check to ensure the AudioPacket is safe to decode.
		*/
		public boolean isAudioPacket()
		{
			return !notAudio ;
		}

		public void decode() throws Exception
		{
			int pos = startPosition ;
			System.out.println( "SPos: " + pos + " Length: " + ( stream.length * 8 ) ) ;

			pos = decodeFloorCurve( pos, stream ) ;
			pos = decodeResidue( pos, stream ) ;

			System.out.println( "EPos: " + pos + " Length: " + ( stream.length * 8 ) ) ;
		}

		private double[] computeWindow()
		{
			final double[] w = new double[n] ;

			for( int i = leftWindowStart; i < leftWindowEnd; ++i )
			{
				double x = ( ( i - leftWindowStart ) + 0.5 ) / leftN * HALF_PI ;
				x = Math.sin( x ) ;
				x *= x ;
				x *= HALF_PI ;
				x = Math.sin( x ) ;
				w[i] = x ;
			}

			for( int i = leftWindowEnd; i < rightWindowStart; ++i )
			{
				w[i] = 1.0 ;
			}

			for( int i = rightWindowStart; i < rightWindowEnd; ++i )
			{
				double x = ( ( i - rightWindowStart ) + 0.5 ) / rightN * HALF_PI + HALF_PI ;
				x = Math.sin( x ) ;
				x *= x ;
				x *= HALF_PI ;
				x = Math.sin( x ) ;
				w[i] = x ;
			}

			return w ;
		}

		private int decodeFloorCurve( int _pos, final byte[] _stream )
		{
			for( int i = 0; i < ( int )audioChannels; ++i )
			{
				final int subMapNumber = mapping.muxs[i] ;
				final int floorNumber = mapping.subMapFloors[subMapNumber] ;

				final FloorConfiguration floorConfig = floors.get( floorNumber ) ;
				switch( floorConfig.type() )
				{
					case 0 :
					{
						System.out.println( "Skipping FloorConfig 0..." ) ;
						break ;
					}
					case 1 :
					{
						final DecodedFloor1 floor = new DecodedFloor1() ;
						decodedFloors[i] = floor ;

						final Floor1Configuration config = ( Floor1Configuration )floorConfig ;
						_pos = config.decodePacket( _pos, _stream, floor ) ;

						noResidue[i] = ( floor.unused ) ? true : false ;
						break ;
					}
				}
			}

			// nonzero vector propagate
			for( int i = 0; i < mapping.couplingSteps; ++i )
			{
				final int magnitude = mapping.magnitudes[i] ;
				final int angle = mapping.angles[i] ;

				if( angle == 0 || magnitude == 0 )
				{
					mapping.magnitudes[i] = 0 ;
					mapping.angles[i] = 0 ;
				}
			}

			System.out.println( "Floor pos: " + _pos ) ;
			return _pos ;
		}

		private int decodeResidue( int _pos, final byte[] _stream ) throws Exception
		{
			for( int i = 0; i < mapping.subMaps; ++i )
			{
				int ch = 0 ;
				for( int j = 0; j < audioChannels; ++j )
				{
					if( mapping.muxs[j] == i )
					{
						doNotDecode[ch] = ( noResidue[j] ) ? true : false ;
						++ch ;
					}
				}

				// We construct and 2D array of the channels we are going to decode.
				// This array will be compact and does not retain the channels 'speaker' position.
				residueValues[i] = new float[ch][] ;

				final int residueNumber = mapping.subMapResidues[i] ;
				final ResidueConfiguration residue = residues.get( residueNumber ) ;

				_pos = residue.decodePacket( _pos, _stream, n, ch, doNotDecode, residueValues[i] ) ;

				ch = 0 ;
				final float[][] channels = new float[audioChannels][] ;
				for( int j = 0; j < audioChannels; ++j )
				{
					if( mapping.muxs[j] == i )
					{
						// Stick the channel back into its intended 'speaker' position.
						channels[j] = residueValues[i][ch] ;
						++ch ;
					}
				}

				residueValues[i] = channels ;
			}

			return _pos ;
		}
	}

	public final class DecodedFloor1 implements DecodedFloor
	{
		private boolean unused = false ;
		private int[] y = null ;

		@Override
		public int type()
		{
			return 1 ;
		}

		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder() ;
			builder.append( "Unused: " ) ;
			builder.append( unused ) ;
			builder.append( '\n' ) ;

			builder.append( "Y: " ) ;
			for( int i = 0; i < y.length; ++i )
			{
				builder.append( y[i] ) ;
				builder.append( ", " ) ;
			}

			return builder.toString() ;
		}
	}

	public interface DecodedFloor
	{
		public int type() ;
	}

	/**
		Contains the information required to quickly decode the headers.
	**/
	public final static class VorbisHeader
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
