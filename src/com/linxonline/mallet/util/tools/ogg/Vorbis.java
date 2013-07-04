package com.linxonline.mallet.util.tools.ogg ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	Vorbis integer variables are stored in little endian.
**/
public class Vorbis
{
	private final static int ID_HEADER_TYPE = 1 ;
	private final static int COMMENT_HEADER_TYPE = 3 ;
	private final static int SETUP_HEADER_TYPE = 5 ;

	private ArrayList<VorbisHeader> headers = new ArrayList<VorbisHeader>() ;

	public Vorbis() {}

	public void decode( final OGG _ogg )
	{
		for( final Page page : _ogg.pages )
		{
			if( identifyHeaders( page ) >= 3 )
			{
				break ;
			}
		}

		for( final VorbisHeader header : headers )
		{
			switch( header.headerType )
			{
				case ID_HEADER_TYPE :
				{
					System.out.println( "Identification Header Type" ) ;
					decodeIDHeader( header ) ;
					break ;
				}
				case COMMENT_HEADER_TYPE :
				{
					System.out.println( "Comment Header Type" ) ;
					decodeCommentHeader( header ) ;
					break ;
				}
				case SETUP_HEADER_TYPE :
				{
					System.out.println( "Setup Header Type" ) ;
					decodeSetupHeader( header ) ;
					break ;
				}
			}
		}
	}

	private int identifyHeaders( final Page _page )
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
					headers.add( new VorbisHeader( packageType, i += 6, _page ) ) ;
				}
			}
		}

		return headers.size() ;
	}

	private void decodeIDHeader( final VorbisHeader _header )
	{
		int pos = _header.start ;
		final byte[] stream = _header.page.data ;

		ConvertBytes.flipEndian( stream, pos, 4 ) ;
		final int version = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Version: " + version ) ;

		final int channels = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] & 0xff ;	// unsigned byte
		System.out.println( "Audio Channels: " + channels ) ;

		ConvertBytes.flipEndian( stream, pos += 1, 4 ) ;
		final int sampleRate = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Sample Rate: " + sampleRate ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		final int bitrateMax = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Bitrate Max: " + bitrateMax ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		final int bitrateNom = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Bitrate Nominal: " + bitrateNom ) ;

		ConvertBytes.flipEndian( stream, pos += 4, 4 ) ;
		final int bitrateMin = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Bitrate Min: " + bitrateMin ) ;

		final byte blocksize = ConvertBytes.toBytes( stream, pos += 4, 1 )[0] ; // unsigned byte
		System.out.println( "Blocksize: " + blocksize ) ;

		final int blocksize0 = ( ( blocksize & 0x0F ) & 0xff )* ( ( blocksize & 0x0F ) & 0xff ) ;
		System.out.println( "B0: " + blocksize0 ) ;
		final int blocksize1 = ( ( blocksize >> 4 ) & 0xff ) * ( ( blocksize >> 4 ) & 0xff ) ;
		System.out.println( "B1: " + blocksize1 ) ;

		//final boolean framing = ConvertBytes.toBoolean( stream, pos += 1, 1 ) ;
		//System.out.println( "Framing: " + framing ) ;
	}

	private void decodeCommentHeader( final VorbisHeader _header )
	{
		int pos = _header.start ;
		final byte[] stream = _header.page.data ;

		ConvertBytes.flipEndian( stream, pos, 4 ) ;
		final int vendorLength = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Vendor Length: " + vendorLength ) ;

		final String vendor = new String( ConvertBytes.toBytes( stream, pos += 4, vendorLength ) ) ;
		System.out.println( "Vendor: " + vendor ) ;

		ConvertBytes.flipEndian( stream, pos += vendorLength, 4 ) ;
		final int iterate = ConvertBytes.toInt( stream, pos, 4 ) ;
		System.out.println( "Iterate: " + iterate ) ;
		
		int length = 4 ; // Set to 4 to offset the iterate variable
		for( int i = 0; i < iterate; i++ )
		{
			ConvertBytes.flipEndian( stream, pos += length, 4 ) ;
			length = ConvertBytes.toInt( stream, pos, 4 ) ;
			System.out.println( "Length: " + length ) ;
			
			final String statement = new String( ConvertBytes.toBytes( stream, pos += 4, length ) ) ;
			System.out.println( "Statement: " + statement ) ;
		}

	}

	private void decodeSetupHeader( final VorbisHeader _header ) {}

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