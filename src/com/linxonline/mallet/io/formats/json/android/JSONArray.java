package com.linxonline.mallet.io.formats.json ;

public class JSONArray
{
	protected final org.json.JSONArray array ;

	protected JSONArray()
	{
		array = new org.json.JSONArray() ;
	}

	protected JSONArray( final String _source ) throws org.json.JSONException
	{
		array = new org.json.JSONArray( _source ) ;
	}

	protected JSONArray( final org.json.JSONArray _array )
	{
		array = _array ;
	}

	public static JSONArray construct()
	{
		return new JSONArray() ;
	}

	public static JSONArray construct( final String _source )
	{
		try
		{
			return new JSONArray( _source ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public int length()
	{
		return array.length() ;
	}

	public JSONArray put( final boolean _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JSONArray put( final int _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JSONArray put( final double _value )
	{
		try
		{
			array.put( _value ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
		}

		return this ;
	}

	public JSONArray put( final long _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JSONArray put( final String _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JSONArray put( final JSONObject _value )
	{
		array.put( ( ( JSONObject )_value ).object ) ;
		return this ;
	}

	public JSONArray put( final JSONArray _value )
	{
		array.put( ( ( JSONArray )_value ).array ) ;
		return this ;
	}

	public boolean getBoolean( final int _index )
	{
		return array.optBoolean( _index ) ;
	}

	public boolean optBoolean( final int _index, final boolean _default )
	{
		return array.optBoolean( _index, _default ) ;
	}

	public int getInt( final int _index )
	{
		return array.optInt( _index ) ;
	}

	public int optInt( final int _index, final int _default )
	{
		return array.optInt( _index, _default ) ;
	}

	public double getDouble( final int _index )
	{
		return array.optDouble( _index ) ;
	}

	public double optDouble( final int _index, final double _default )
	{
		return array.optDouble( _index, _default ) ;
	}

	public long getLong( final int _index )
	{
		return array.optLong( _index ) ;
	}

	public long optLong( final int _index, final long _default )
	{
		return array.optLong( _index, _default ) ;
	}

	public String getString( final int _index )
	{
		return array.optString( _index ) ;
	}

	public String optString( final int _index, final String _default )
	{
		return array.optString( _index, _default ) ;
	}

	public JSONObject getJSONObject( final int _index )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return new JSONObject( obj ) ;
	}

	public JSONObject optJSONObject( final int _index, final JSONObject _default )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return _default ;
		}

		return new JSONObject( obj ) ;
	}

	public JSONArray getJSONArray( final int _index )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return new JSONArray( arr ) ;
	}

	public JSONArray optJSONArray( final int _index, final JSONArray _default )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return _default ;
		}

		return new JSONArray( arr ) ;
	}

	public String toString()
	{
		return array.toString() ;
	}

	public String toString( final int _indent )
	{
		try
		{
			return array.toString( _indent ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return "[]" ;
		}
	}
}
