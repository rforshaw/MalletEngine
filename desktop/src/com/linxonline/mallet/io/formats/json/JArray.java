package com.linxonline.mallet.io.formats.json ;

public final class JArray
{
	protected final org.json.JSONArray array ;

	private JArray()
	{
		array = new org.json.JSONArray() ;
	}

	private JArray( final String _source ) throws org.json.JSONException
	{
		array = new org.json.JSONArray( _source ) ;
	}

	private JArray( final org.json.JSONArray _array )
	{
		array = _array ;
	}

	protected static JArray wrap( org.json.JSONArray _array )
	{
		return new JArray( _array ) ;
	}

	public static JArray construct()
	{
		return new JArray() ;
	}

	public static JArray construct( final String _source )
	{
		try
		{
			return new JArray( _source ) ;
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

	public JArray put( final boolean _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JArray put( final int _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JArray put( final double _value )
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

	public JArray put( final long _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JArray put( final String _value )
	{
		array.put( _value ) ;
		return this ;
	}

	public JArray put( final JObject _value )
	{
		array.put( ( ( JObject )_value ).object ) ;
		return this ;
	}

	public JArray put( final JArray _value )
	{
		array.put( ( ( JArray )_value ).array ) ;
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

	public JObject getJObject( final int _index )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return JObject.wrap( obj ) ;
	}

	public JObject optJObject( final int _index, final JObject _default )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return _default ;
		}

		return JObject.wrap( obj ) ;
	}

	public JArray getJArray( final int _index )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return JArray.wrap( arr ) ;
	}

	public JArray optJArray( final int _index, final JArray _default )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return _default ;
		}

		return JArray.wrap( arr ) ;
	}

	@Override
	public int hashCode()
	{
		return array.hashCode() ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		return array.equals( _obj ) ;
	}

	@Override
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
