package com.linxonline.mallet.io.formats.json ;

import java.util.Iterator ;

import com.linxonline.mallet.io.formats.json.IJSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringInStream ;
import com.linxonline.mallet.io.filesystem.StringInCallback ;

public class JSONObject implements IJSONObject
{
	public final org.json.JSONObject object ;

	protected JSONObject()
	{
		object = new org.json.JSONObject() ;
	}

	protected JSONObject( final String _source ) throws org.json.JSONException
	{
		object = new org.json.JSONObject( _source ) ;
	}

	protected JSONObject( final org.json.JSONObject _object )
	{
		object = _object ;
	}

	/**
		Create a blank JSON Object
	*/
	public static JSONObject construct()
	{
		return new JSONObject() ;
	}

	/**
		Create a JSON Object/s from file stream.
	*/
	public static JSONObject construct( final FileStream _file )
	{
		final StringInStream stream = _file.getStringInStream() ;
		if( stream == null )
		{
			return new JSONObject() ;
		}

		final StringBuilder builder = new StringBuilder() ;
		String line = null ;
		while( ( line = stream.readLine() ) != null )
		{
			builder.append( line ) ;
		}

		stream.close() ;
		return JSONObject.construct( builder.toString() ) ;
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
				_callback.callback( JSONObject.construct( builder.toString() ) ) ;
			}
		}, 1 ) ;
	}

	/**
		Create a JSON Object/s from source.
	*/
	public static JSONObject construct( final String _source )
	{
		try
		{
			return new JSONObject( _source ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public String[] keys()
	{
		final int length = object.length() ;
		final String[] keys = new String[length] ;

		final Iterator<String> iter = object.keys() ;
		for( int i = 0; i < length; i++ )
		{
			keys[i] = iter.next() ;
		}

		return keys ;
	}

	@Override
	public boolean has( final String _key )
	{
		return object.has( _key ) ;
	}

	@Override
	public JSONObject put( final String _key, final boolean _value )
	{
		try
		{
			object.put( _key, _value ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final int _value )
	{
		try
		{
			object.put( _key, _value ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final double _value )
	{
		try
		{
			object.put( _key, _value ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final long _value )
	{
		try
		{
			object.put( _key, _value ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final String _value )
	{
		try
		{
			object.put( _key, _value ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final JSONObject _value )
	{
		try
		{
			object.put( _key, ( ( JSONObject )_value ).object ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public JSONObject put( final String _key, final JSONArray _value )
	{
		try
		{
			object.put( _key, ( ( JSONArray )_value ).array ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	@Override
	public boolean getBoolean( final String _key )
	{
		return object.optBoolean( _key ) ;
	}

	@Override
	public boolean optBoolean( final String _key, final boolean _default )
	{
		return object.optBoolean( _key, _default ) ;
	}

	@Override
	public int getInt( final String _key )
	{
		return object.optInt( _key ) ;
	}

	@Override
	public int optInt( final String _key, final int _default )
	{
		return object.optInt( _key, _default ) ;
	}

	@Override
	public double getDouble( final String _key )
	{
		return object.optDouble( _key ) ;
	}

	@Override
	public double optDouble( final String _key, final double _default )
	{
		return object.optDouble( _key, _default ) ;
	}

	@Override
	public long getLong( final String _key )
	{
		return object.optLong( _key ) ;
	}

	@Override
	public long optLong( final String _key, final long _default )
	{
		return object.optLong( _key, _default ) ;
	}

	@Override
	public String getString( final String _key )
	{
		return object.optString( _key ) ;
	}

	@Override
	public String optString( final String _key, final String _default )
	{
		return object.optString( _key, _default ) ;
	}

	@Override
	public JSONObject getJSONObject( final String _key )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return null ;
		}

		return new JSONObject( obj ) ;
	}

	@Override
	public JSONObject optJSONObject( final String _key, final JSONObject _default )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return ( JSONObject )_default ;
		}

		return new JSONObject( obj ) ;
	}

	@Override
	public JSONArray getJSONArray( final String _key )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return null ;
		}

		return new JSONArray( array ) ;
	}

	@Override
	public JSONArray optJSONArray( final String _key, final JSONArray _default )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return _default ;
		}

		return new JSONArray( array ) ;
	}

	@Override
	public String toString()
	{
		return object.toString() ;
	}

	@Override
	public String toString( final int _indent )
	{
		try
		{
			return object.toString( _indent ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return "{}" ;
		}
	}
}
