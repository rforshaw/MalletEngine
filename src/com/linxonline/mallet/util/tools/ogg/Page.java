package com.linxonline.mallet.util.tools.ogg ;

public class Page
{
	public final String header ;
	public final int version ;
	public final int hType ;
	public final long gPosition ;
	public final int bSN ;
	public final int pSeq ;
	public final int checksum ;
	public final int pSeg ;
	public final byte[] data ;

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

	public String toString()
	{
		return new String( data ) ;
	}
}