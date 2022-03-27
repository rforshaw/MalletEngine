package com.linxonline.mallet.io.writer ;

import java.util.Collection ;
import com.linxonline.mallet.io.filesystem.* ;

public final class WriteFile
{
	private WriteFile() {}

	/**
		Writes each String of Collection to a new line of txt file.
	*/
	public static final boolean write( final String _file, final Collection<String> _list )
	{
		return write( GlobalFileSystem.getFile( _file ), _list ) ;
	}

	public static final boolean write( final FileStream _file, final Collection<String> _list )
	{
		try( final StringOutStream stream = _file.getStringOutStream() )
		{
			for( final String line : _list )
			{
				stream.writeLine( line ) ;
			}
			return true ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	public static final boolean write( final String _file, final String _data )
	{
		return write( GlobalFileSystem.getFile( _file ), _data ) ;
	}

	public static final boolean write( final FileStream _file, final String _data )
	{
		try( final StringOutStream stream = _file.getStringOutStream() )
		{
			stream.writeLine( _data ) ;
			return true ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}
}
