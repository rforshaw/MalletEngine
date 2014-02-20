package com.linxonline.mallet.io.reader ;

import com.linxonline.mallet.util.settings.* ;

/**
	Parse Interface - Convert _src to _dest.
	Allows a developer to convert the String based format of _src
	to the appropriate key and value property of _dest.
*/
public interface ParseInterface
{
	public void parse( final Settings _src, final Settings _dest ) ;
}