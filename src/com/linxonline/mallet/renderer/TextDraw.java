package com.linxonline.mallet.renderer ;

public interface TextDraw extends Draw
{
	public StringBuilder setText( final StringBuilder _text ) ;
	public StringBuilder getText() ;

	public int setStart( final int _start ) ;
	public int getStart() ;

	public int setEnd( final int _end ) ;
	public int getEnd() ;
}
