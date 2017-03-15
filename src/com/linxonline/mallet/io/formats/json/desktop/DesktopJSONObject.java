package com.linxonline.mallet.io.formats.json.desktop ;

import java.util.Iterator ;

import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

public class DesktopJSONObject extends JSONObject
{
	protected final org.json.JSONObject object ;

	public DesktopJSONObject()
	{
		object = new org.json.JSONObject() ;
	}

	public DesktopJSONObject( final String _source ) throws org.json.JSONException
	{
		object = new org.json.JSONObject( _source ) ;
	}

	protected DesktopJSONObject( final org.json.JSONObject _object )
	{
		object = _object ;
	}

	@Override
	protected JSONObject create()
	{
		return new DesktopJSONObject() ;
	}

	@Override
	protected JSONObject create( final String _source )
	{
		try
		{
			return new DesktopJSONObject( _source ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public static void init()
	{
		setConstructor( new DesktopJSONObject() ) ;
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
			object.put( _key, ( ( DesktopJSONObject )_value ).object ) ;
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
			object.put( _key, ( ( DesktopJSONArray )_value ).array ) ;
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

		return new DesktopJSONObject( obj ) ;
	}

	@Override
	public JSONObject optJSONObject( final String _key, final JSONObject _default )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return _default ;
		}

		return new DesktopJSONObject( obj ) ;
	}

	@Override
	public JSONArray getJSONArray( final String _key )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return null ;
		}

		return new DesktopJSONArray( array ) ;
	}

	@Override
	public JSONArray optJSONArray( final String _key, final JSONArray _default )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return _default ;
		}

		return new DesktopJSONArray( array ) ;
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
