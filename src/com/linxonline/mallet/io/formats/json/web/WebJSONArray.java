package com.linxonline.mallet.io.formats.json.web ;

import org.teavm.jso.json.JSON ;
import org.teavm.jso.JSObject ;
import org.teavm.jso.JSBody ;

import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

public class WebJSONArray extends JSONArray
{
	protected final JSObject array ;

	public WebJSONArray()
	{
		array = JSON.parse( "[]" ) ;
	}

	public WebJSONArray( final String _source )
	{
		array = JSON.parse( _source ) ;
	}

	protected WebJSONArray( final JSObject _array )
	{
		array = _array ;
	}

	@Override
	protected JSONArray create()
	{
		return new WebJSONArray() ;
	}

	@Override
	protected JSONArray create( final String _source )
	{
		return new WebJSONArray( _source ) ;
	}

	public static void init()
	{
		setConstructor( new WebJSONArray() ) ;
	}

	@Override
	public int length()
	{
		return length( array ) ;
	}

	@Override
	public JSONArray put( final boolean _value )
	{
		putBoolean( array, _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final int _value )
	{
		putInt( array, _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final double _value )
	{
		putDouble( array, _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final long _value )
	{
		putDouble( array, ( double )_value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final String _value )
	{
		putString( array, _value ) ;
		return this ;
	}

	@Override
	public JSONArray put( final JSONObject _value )
	{
		putObject( array, ( ( WebJSONObject )_value ).object ) ;
		return this ;
	}

	@Override
	public JSONArray put( final JSONArray _value )
	{
		putArray( array, ( ( WebJSONArray )_value ).array ) ;
		return this ;
	}

	@Override
	public boolean getBoolean( final int _index )
	{
		return optBoolean( array, _index ) ;
	}

	@Override
	public boolean optBoolean( final int _index, final boolean _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optBoolean( array, _index ) ;
	}

	@Override
	public int getInt( final int _index )
	{
		return optInt( array, _index ) ;
	}

	@Override
	public int optInt( final int _index, final int _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optInt( array, _index ) ;
	}

	@Override
	public double getDouble( final int _index )
	{
		return optDouble( array, _index ) ;
	}

	@Override
	public double optDouble( final int _index, final double _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optDouble( array, _index ) ;
	}

	@Override
	public long getLong( final int _index )
	{
		return ( long )optInt( array, _index ) ;
	}

	@Override
	public long optLong( final int _index, final long _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return ( long )optInt( array, _index ) ;
	}

	@Override
	public String getString( final int _index )
	{
		return optString( array, _index ) ;
	}

	@Override
	public String optString( final int _index, final String _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optString( array, _index ) ;
	}

	@Override
	public JSONObject getJSONObject( final int _index )
	{
		final JSObject obj = optJSObject( array, _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return new WebJSONObject( obj ) ;
	}

	@Override
	public JSONObject optJSONObject( final int _index, final JSONObject _default )
	{
		final JSObject obj = optJSObject( array, _index ) ;
		if( obj == null )
		{
			return _default ;
		}

		return new WebJSONObject( obj ) ;
	}

	@Override
	public JSONArray getJSONArray( final int _index )
	{
		final JSObject arr = optJSObject( array, _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return new WebJSONArray( arr ) ;
	}

	@Override
	public JSONArray optJSONArray( final int _index, final JSONArray _default )
	{
		final JSObject arr = optJSObject( array, _index ) ;
		if( arr == null )
		{
			return _default ;
		}

		return new WebJSONArray( arr ) ;
	}

	@Override
	public String toString()
	{
		return array.toString() ;
	}

	@Override
	public String toString( final int _indent )
	{
		return array.toString() ;
	}

	@JSBody( params = { "_array" }, script = "return _array.length ;" )
	public static native int length( final JSObject _array ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putBoolean( final JSObject _array, final boolean _value ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putInt( final JSObject _array, final int _value ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putDouble( final JSObject _array, final double _value ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putString( final JSObject _array, final String _value ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putObject( final JSObject _array, final JSObject _value ) ;

	@JSBody( params = { "_array", "_value" }, script = "_array.push( _value ) ;" )
	public static native void putArray( final JSObject _array, final JSObject _value ) ;

	@JSBody( params = { "_array", "_index" }, script = "return _array[_index] ;" )
	public static native boolean optBoolean( final JSObject _array, final int _index ) ;

	@JSBody( params = { "_array", "_index" }, script = "return _array[_index] ;" )
	public static native int optInt( final JSObject _array, final int _index ) ;

	@JSBody( params = { "_array", "_index" }, script = "return _array[_index] ;" )
	public static native double optDouble( final JSObject _array, final int _index ) ;

	@JSBody( params = { "_array", "_index" }, script = "return _array[_index] ;" )
	public static native String optString( final JSObject _array, final int _index ) ;

	@JSBody( params = { "_array", "_index" }, script = "return _array[_index] ;" )
	public static native JSObject optJSObject( final JSObject _array, final int _index ) ;
}
