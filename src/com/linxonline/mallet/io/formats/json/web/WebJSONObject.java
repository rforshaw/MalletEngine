package com.linxonline.mallet.io.formats.json.web ;

import org.teavm.jso.json.JSON ;
import org.teavm.jso.JSObject ;
import org.teavm.jso.JSBody ;

import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

public class WebJSONObject extends JSONObject
{
	protected final JSObject object ;

	public WebJSONObject()
	{
		object = JSON.parse( "{}" ) ;
	}

	public WebJSONObject( final String _source )
	{
		object = JSON.parse( _source ) ;
	}

	protected WebJSONObject( final JSObject _object )
	{
		object = _object ;
	}

	@Override
	protected JSONObject create()
	{
		return new WebJSONObject() ;
	}

	@Override
	protected JSONObject create( final String _source )
	{
		return new WebJSONObject( _source ) ;
	}

	public static void init()
	{
		setConstructor( new WebJSONObject() ) ;
	}

	@Override
	public String[] keys()
	{
		return keys( object ) ;
	}

	@Override
	public boolean has( final String _key )
	{
		return hasKey( object, _key ) ;
	}

	@Override
	public JSONObject put( final String _key, final boolean _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final int _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final double _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final long _value )
	{
		put( object, _key, ( double )_value ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final String _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final JSONObject _value )
	{
		put( object, _key, ( ( WebJSONObject )_value ).object ) ;
		return this ;
	}

	@Override
	public JSONObject put( final String _key, final JSONArray _value )
	{
		put( object, _key, ( ( WebJSONArray )_value ).array ) ;
		return this ;
	}

	@Override
	public boolean getBoolean( final String _key )
	{
		return optBoolean( object, _key ) ;
	}

	@Override
	public boolean optBoolean( final String _key, final boolean _default )
	{
		if( has( _key ) )
		{
			return optBoolean( object, _key ) ;
		}

		return _default ;
	}

	@Override
	public int getInt( final String _key )
	{
		return optInt( object, _key ) ;
	}

	@Override
	public int optInt( final String _key, final int _default )
	{
		if( has( _key ) )
		{
			return optInt( object, _key ) ;
		}

		return _default ;
	}

	@Override
	public double getDouble( final String _key )
	{
		return optDouble( object, _key ) ;
	}

	@Override
	public double optDouble( final String _key, final double _default )
	{
		if( has( _key ) )
		{
			return optDouble( object, _key ) ;
		}

		return _default ;
	}

	@Override
	public long getLong( final String _key )
	{
		return ( long )optInt( object, _key ) ;
	}

	@Override
	public long optLong( final String _key, final long _default )
	{
		if( has( _key ) )
		{
			return ( long )optInt( object, _key ) ;
		}

		return _default ;
	}

	@Override
	public String getString( final String _key )
	{
		return optString( _key, null ) ;
	}

	@Override
	public String optString( final String _key, final String _default )
	{
		if( has( _key ) )
		{
			return optString( object, _key ) ;
		}

		return _default ;
	}

	@Override
	public JSONObject getJSONObject( final String _key )
	{
		if( has( _key ) == false )
		{
			return null ;
		}

		return new WebJSONObject( optJSObject( object, _key ) ) ;
	}

	@Override
	public JSONObject optJSONObject( final String _key, final JSONObject _default )
	{
		if( has( _key ) == false )
		{
			return _default ;
		}

		return new WebJSONObject( optJSObject( object, _key ) ) ;
	}

	@Override
	public JSONArray getJSONArray( final String _key )
	{
		if( has( _key ) == false )
		{
			return null ;
		}

		return new WebJSONArray( optJSArray( object, _key ) ) ;
	}

	@Override
	public JSONArray optJSONArray( final String _key, final JSONArray _default )
	{
		if( has( _key ) == false )
		{
			return _default ;
		}

		return new WebJSONArray( optJSArray( object, _key ) ) ;
	}

	@Override
	public String toString()
	{
		return object.toString() ;
	}

	@Override
	public String toString( final int _indent )
	{
		return object.toString() ;
	}

	@JSBody( params = { "_obj", "_key" }, script = "return _obj.hasOwnProperty( _key ) ;" )
	public static native boolean hasKey( final JSObject _obj, String _key ) ;

	@JSBody( params = { "_obj" }, script = "return _obj.keys ;" )
	public static native String[] keys( final JSObject _obj ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final boolean _value ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final int _value ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final double _value ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final long _value ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final String _value ) ;

	@JSBody( params = { "_obj", "_key", "_val" }, script = "_obj[_key] = _val ;" )
	public static native void put( final JSObject _obj, final String _key, final JSObject _value ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native boolean optBoolean( final JSObject _obj, final String _key ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native int optInt( final JSObject _obj, final String _key ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native double optDouble( final JSObject _obj, final String _key ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native String optString( final JSObject _obj, final String _key ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native JSObject optJSObject( final JSObject _obj, final String _key ) ;

	@JSBody( params = { "_obj", "_key" }, script = "return _obj[_key] ;" )
	public static native JSObject optJSArray( final JSObject _obj, final String _key ) ;
}
