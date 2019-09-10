package com.linxonline.mallet.io.writer.config ;

import java.util.Collection ;
import java.util.List ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.io.writer.WriteFile ;

public class ConfigWriter
{
	public ConfigWriter() {}

	public static boolean write( final String _file, final Settings _config )
	{
		return write( GlobalFileSystem.getFile( _file ), _config ) ;
	}

	public static boolean write( final FileStream _stream, final Settings _config )
	{
		final Collection<AVariable> variables = _config.toArray() ;
		final List<String> list = MalletList.<String>newList( variables.size() ) ;
		final StringBuilder buffer = new StringBuilder() ;

		for( final AVariable variable : variables )
		{
			buffer.append( variable.name ) ;
			buffer.append( ' ' ) ;
			buffer.append( variable.toString() ) ;
			buffer.append( '\n' ) ;

			list.add( buffer.toString() ) ;
			buffer.setLength( 0 ) ;
		}

		return WriteFile.write( _stream, list ) ;
	}
}
