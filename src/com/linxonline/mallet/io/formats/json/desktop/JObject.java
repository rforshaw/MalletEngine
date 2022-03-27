package com.linxonline.mallet.io.formats.json ;

import java.util.Iterator ;

import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringInStream ;
import com.linxonline.mallet.io.filesystem.StringInCallback ;

public class JObject
{
	public final org.json.JSONObject object ;

	protected JObject()
	{
		object = new org.json.JSONObject() ;
	}

	protected JObject( final String _source ) throws org.json.JSONException
	{
		object = new org.json.JSONObject( _source ) ;
	}

	protected JObject( final org.json.JSONObject _object )
	{
		object = _object ;
	}

	/**
		Create a blank JSON Object
	*/
	public static JObject construct()
	{
		return new JObject() ;
	}

	/**
		Create a JSON Object/s from file stream.
	*/
	public static JObject construct( final FileStream _file )
	{
		try( final StringInStream stream = _file.getStringInStream() )
		{
			if( stream == null )
			{
				return new JObject() ;
			}

			final StringBuilder builder = new StringBuilder() ;
			String line = null ;
			while( ( line = stream.readLine() ) != null )
			{
				builder.append( line ) ;
			}

			return JObject.construct( builder.toString() ) ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return new JObject() ;
		}
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
				_callback.callback( JObject.construct( builder.toString() ) ) ;
			}
		}, 1 ) ;
	}

	/**
		Create a JSON Object/s from source.
	*/
	public static JObject construct( final String _source )
	{
		try
		{
			return new JObject( _source ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

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

	public boolean has( final String _key )
	{
		return object.has( _key ) ;
	}

	public JObject put( final String _key, final boolean _value )
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

	public JObject put( final String _key, final int _value )
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

	public JObject put( final String _key, final double _value )
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

	public JObject put( final String _key, final long _value )
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

	public JObject put( final String _key, final String _value )
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

	public JObject put( final String _key, final JObject _value )
	{
		try
		{
			object.put( _key, ( ( JObject )_value ).object ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public JObject put( final String _key, final JArray _value )
	{
		try
		{
			object.put( _key, ( ( JArray )_value ).array ) ;
			return this ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public boolean getBoolean( final String _key )
	{
		return object.optBoolean( _key ) ;
	}

	public boolean optBoolean( final String _key, final boolean _default )
	{
		return object.optBoolean( _key, _default ) ;
	}

	public int getInt( final String _key )
	{
		return object.optInt( _key ) ;
	}

	public int optInt( final String _key, final int _default )
	{
		return object.optInt( _key, _default ) ;
	}

	public double getDouble( final String _key )
	{
		return object.optDouble( _key ) ;
	}

	public double optDouble( final String _key, final double _default )
	{
		return object.optDouble( _key, _default ) ;
	}

	public long getLong( final String _key )
	{
		return object.optLong( _key ) ;
	}

	public long optLong( final String _key, final long _default )
	{
		return object.optLong( _key, _default ) ;
	}

	public String getString( final String _key )
	{
		return object.optString( _key ) ;
	}

	public String optString( final String _key, final String _default )
	{
		return object.optString( _key, _default ) ;
	}

	public JObject getJObject( final String _key )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return null ;
		}

		return new JObject( obj ) ;
	}

	public JObject optJObject( final String _key, final JObject _default )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return ( JObject )_default ;
		}

		return new JObject( obj ) ;
	}

	public JArray getJArray( final String _key )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return null ;
		}

		return new JArray( array ) ;
	}

	public JArray optJArray( final String _key, final JArray _default )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return _default ;
		}

		return new JArray( array ) ;
	}

	public String toString()
	{
		return object.toString() ;
	}

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

	public interface ConstructCallback
	{
		public void callback( JObject _object ) ;
	}
}
