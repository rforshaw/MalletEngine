package com.linxonline.mallet.io.formats.json.android ;

import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

public class AndroidJSONArray extends JSONArray
{
	protected final org.json.JSONArray array ;

	public AndroidJSONArray()
	{
		array = new org.json.JSONArray() ;
	}

	public AndroidJSONArray( final String _source ) throws org.json.JSONException
	{
		array = new org.json.JSONArray( _source ) ;
	}

	protected AndroidJSONArray( final org.json.JSONArray _array )
	{
		array = _array ;
	}

	@Override
	protected JSONArray create()
	{
		return new AndroidJSONArray() ;
	}

	@Override
	protected JSONArray create( final String _source )
	{
		try
		{
			return new AndroidJSONArray( _source ) ;
		}
		catch( org.json.JSONException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public static void init()
	{
		setConstructor( new AndroidJSONArray() ) ;
	}

	@Override
	public int length()
	{
		return array.length() ;
	}

	@Override
	public JSONArray put( final boolean _value )
	{
		array.put( _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final int _value )
	{
		array.put( _value ) ;
		return this ;
	}

	@Override
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

	@Override
	public JSONArray put( final long _value )
	{
		array.put( _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final String _value )
	{
		array.put( _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final JSONObject _value )
	{
		array.put( ( ( AndroidJSONObject )_value ).object ) ;
		return this ;
	}

	@Override
	public JSONArray put( final JSONArray _value )
	{
		array.put( ( ( AndroidJSONArray )_value ).array ) ;
		return this ;
	}

	@Override
	public boolean getBoolean( final int _index )
	{
		return array.optBoolean( _index ) ;
	}

	@Override
	public boolean optBoolean( final int _index, final boolean _default )
	{
		return array.optBoolean( _index, _default ) ;
	}

	@Override
	public int getInt( final int _index )
	{
		return array.optInt( _index ) ;
	}

	@Override
	public int optInt( final int _index, final int _default )
	{
		return array.optInt( _index, _default ) ;
	}

	@Override
	public double getDouble( final int _index )
	{
		return array.optDouble( _index ) ;
	}

	@Override
	public double optDouble( final int _index, final double _default )
	{
		return array.optDouble( _index, _default ) ;
	}

	@Override
	public long getLong( final int _index )
	{
		return array.optLong( _index ) ;
	}

	@Override
	public long optLong( final int _index, final long _default )
	{
		return array.optLong( _index, _default ) ;
	}

	@Override
	public String getString( final int _index )
	{
		return array.optString( _index ) ;
	}

	@Override
	public String optString( final int _index, final String _default )
	{
		return array.optString( _index, _default ) ;
	}

	@Override
	public JSONObject getJSONObject( final int _index )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return new AndroidJSONObject( obj ) ;
	}

	@Override
	public JSONObject optJSONObject( final int _index, final JSONObject _default )
	{
		final org.json.JSONObject obj = array.optJSONObject( _index ) ;
		if( obj == null )
		{
			return _default ;
		}

		return new AndroidJSONObject( obj ) ;
	}

	@Override
	public JSONArray getJSONArray( final int _index )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return new AndroidJSONArray( arr ) ;
	}

	@Override
	public JSONArray optJSONArray( final int _index, final JSONArray _default )
	{
		final org.json.JSONArray arr = array.optJSONArray( _index ) ;
		if( arr == null )
		{
			return _default ;
		}

		return new AndroidJSONArray( arr ) ;
	}

	@Override
	public String toString()
	{
		return array.toString() ;
	}

	@Override
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
