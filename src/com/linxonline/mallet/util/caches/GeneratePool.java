package com.linxonline.mallet.util.caches ;

import java.util.Map ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JObject ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;

/**
	Used to create a resource based on a JObject.
	This class can be used directly but you're expected
	to extend the class and define a specific use-case.
	See MaterialPool for an example.
*/
public class GeneratePool<T> implements IGeneratePool<T>
{
	private final Map<String, IGenerator<T>> creators = MalletMap.<String, IGenerator<T>>newMap() ;
	private final Map<String, T> items = MalletMap.<String, T>newMap() ;	// Previously created items.
	private final String[] extensions ;		// The file path extensions that are considered valid.

	public GeneratePool( final Map<String, IGenerator<T>> _generators, final String ... _extensions )
	{
		extensions = _extensions ;
		creators.putAll( _generators ) ;
	}

	/**
		Create a resource based on the filepath passed in.
		return an existing resource if the _path has been
		previously processed.
		NOTE: The file defined by the path must contain 
		valid json.
	*/
	public T create( final String _path )
	{
		T item = items.get( _path ) ;
		if( item != null )
		{
			// The path has previously been created.
			return item ;
		}

		if( GlobalFileSystem.isExtension( _path, extensions ) == false )
		{
			Logger.println( "Item: " + _path + " invalid extension.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _path ) ;
		if( stream.exists() == false )
		{
			Logger.println( "Item: " + _path + " doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final JObject jItem = JObject.construct( stream ) ;
		
		final String type = jItem.optString( "type", null ) ;
		final IGenerator<T> generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( "Item: " + type + " unable to find matching generator.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		item = generator.generate( _path, jItem ) ;
		if( item != null )
		{
			items.put( _path, item ) ;
		}

		return item ;
	}
}
