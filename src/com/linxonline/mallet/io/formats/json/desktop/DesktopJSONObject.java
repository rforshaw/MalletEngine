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

	protected JSONObject create()
	{
		return new DesktopJSONObject() ;
	}

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
		constructor = new DesktopJSONObject() ;
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

	public boolean optBoolean( final String _key )
	{
		return object.optBoolean( _key ) ;
	}

	public boolean optBoolean( final String _key, final boolean _default )
	{
		return object.optBoolean( _key, _default ) ;
	}

	public int optInt( final String _key )
	{
		return object.optInt( _key ) ;
	}

	public int optInt( final String _key, final int _default )
	{
		return object.optInt( _key, _default ) ;
	}

	public double optDouble( final String _key )
	{
		return object.optDouble( _key ) ;
	}

	public double optDouble( final String _key, final double _default )
	{
		return object.optDouble( _key, _default ) ;
	}

	public long optLong( final String _key )
	{
		return object.optLong( _key ) ;
	}

	public long optLong( final String _key, final long _default )
	{
		return object.optLong( _key, _default ) ;
	}

	public String optString( final String _key )
	{
		return object.optString( _key ) ;
	}

	public String optString( final String _key, final String _default )
	{
		return object.optString( _key, _default ) ;
	}

	public JSONObject optJSONObject( final String _key )
	{
		final org.json.JSONObject obj = object.optJSONObject( _key ) ;
		if( obj == null )
		{
			return null ;
		}

		return new DesktopJSONObject( obj ) ;
	}

	public JSONArray optJSONArray( final String _key )
	{
		final org.json.JSONArray array = object.optJSONArray( _key ) ;
		if( array == null )
		{
			return null ;
		}

		return new DesktopJSONArray( array ) ;
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
}