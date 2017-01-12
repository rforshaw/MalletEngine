package com.linxonline.mallet.io.formats.json ;

import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringInStream ;
import com.linxonline.mallet.io.filesystem.StringInCallback ;

/**
	A wrapper class around a platform specific implementation 
	of the JSON format.
	Allows different platforms to use a JSON library built for 
	that platform, but provide a consistent interface on all platforms.
	Note: The wrapper does not support multiple json libraries 
	simultaneously.
*/
public abstract class JSONObject
{
	protected static JSONObject constructor = null ;

	public JSONObject() {}

	/**
		Create a blank JSON Object
	*/
	public static JSONObject construct()
	{
		return constructor.create() ;
	}

	/**
		Create a JSON Object/s from file stream.
	*/
	public static JSONObject construct( final FileStream _file )
	{
		final StringInStream stream = _file.getStringInStream() ;
		if( stream == null )
		{
			return constructor.create( "" ) ;
		}

		final StringBuilder builder = new StringBuilder() ;
		String line = null ;
		while( ( line = stream.readLine() ) != null )
		{
			builder.append( line ) ;
		}

		return constructor.create( builder.toString() ) ;
	}

	public static boolean construct( final FileStream _file, final ConstructCallback _callback )
	{
		return _file.getStringInCallback( new StringInCallback()
		{
			private final StringBuilder builder = new StringBuilder() ;

			public int resourceAsString( final String[] _resource, final int _length )
			{
				for( int i = 0; i < _length; i++ )
				{
					builder.append( _resource[i] ) ;
				}
			
				return 1 ;
			}

			public void start() {}

			public void end()
			{
				_callback.callback( constructor.create( builder.toString() ) ) ;
			}
		}, 1 ) ;
	}

	/**
		Create a JSON Object/s from source.
	*/
	public static JSONObject construct( final String _source )
	{
		return constructor.create( _source ) ;
	}

	protected abstract JSONObject create() ;
	protected abstract JSONObject create( final String _source ) ;

	public abstract String[] keys() ;
	public abstract boolean has( final String _key ) ;

	public abstract JSONObject put( final String _key, final boolean _value ) ;
	public abstract JSONObject put( final String _key, final int _value ) ;
	public abstract JSONObject put( final String _key, final double _value ) ;
	public abstract JSONObject put( final String _key, final long _value ) ;
	public abstract JSONObject put( final String _key, final String _value ) ;
	public abstract JSONObject put( final String _key, final JSONObject _value ) ;
	public abstract JSONObject put( final String _key, final JSONArray _value ) ;

	public abstract boolean getBoolean( final String _key ) ;
	public abstract boolean optBoolean( final String _key, final boolean _default ) ;

	public abstract int getInt( final String _key ) ;
	public abstract int optInt( final String _key, final int _default ) ;

	public abstract double getDouble( final String _key ) ;
	public abstract double optDouble( final String _key, final double _default ) ;

	public abstract long getLong( final String _key ) ;
	public abstract long optLong( final String _key, final long _default ) ;

	public abstract String getString( final String _key ) ;
	public abstract String optString( final String _key, final String _default ) ;

	public abstract JSONObject getJSONObject( final String _key ) ;
	public abstract JSONObject optJSONObject( final String _key, final JSONObject _default ) ;

	public abstract JSONArray getJSONArray( final String _key ) ;
	public abstract JSONArray optJSONArray( final String _key, final JSONArray _default ) ;

	public abstract String toString() ;
	public abstract String toString( final int _indent ) ;

	public interface ConstructCallback
	{
		public void callback( final JSONObject _object ) ;
	}
}
