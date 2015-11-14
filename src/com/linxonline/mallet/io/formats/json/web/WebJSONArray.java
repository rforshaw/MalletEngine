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
	
	protected JSONArray create()
	{
		return new WebJSONArray() ;
	}

	protected JSONArray create( final String _source )
	{
		return new WebJSONArray( _source ) ;
	}

	public static void init()
	{
		constructor = new WebJSONArray() ;
	}
	
	public int length()
	{
		return length( array ) ;
	}

	public JSONArray put( final boolean _value )
	{
		putBoolean( array, _value ) ;
		return this ;
	}

	public JSONArray put( final int _value )
	{
		putInt( array, _value ) ;
		return this ;
	}

	public JSONArray put( final double _value )
	{
		putDouble( array, _value ) ;
		return this ;
	}

	public JSONArray put( final long _value )
	{
		putLong( array, _value ) ;
		return this ;
	}

	public JSONArray put( final String _value )
	{
		putString( array, _value ) ;
		return this ;
	}

	public JSONArray put( final JSONObject _value )
	{
		putObject( array, ( ( WebJSONObject )_value ).object ) ;
		return this ;
	}

	public JSONArray put( final JSONArray _value )
	{
		putArray( array, ( ( WebJSONArray )_value ).array ) ;
		return this ;
	}

	public boolean optBoolean( final int _index )
	{
		return optBoolean( array, _index ) ;
	}

	public boolean optBoolean( final int _index, final boolean _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optBoolean( array, _index ) ;
	}

	public int optInt( final int _index )
	{
		return optInt( array, _index ) ;
	}

	public int optInt( final int _index, final int _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optInt( array, _index ) ;
	}

	public double optDouble( final int _index )
	{
		return optDouble( array, _index ) ;
	}

	public double optDouble( final int _index, final double _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optDouble( array, _index ) ;
	}

	public long optLong( final int _index )
	{
		return ( long )optInt( array, _index ) ;
	}

	public long optLong( final int _index, final long _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return ( long )optInt( array, _index ) ;
	}

	public String optString( final int _index )
	{
		return optString( array, _index ) ;
	}

	public String optString( final int _index, final String _default )
	{
		if( _index > length() )
		{
			return _default ;
		}

		return optString( array, _index ) ;
	}

	public JSONObject optJSONObject( final int _index )
	{
		final JSObject obj = optJSObject( array, _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return new WebJSONObject( obj ) ;
	}

	public JSONArray optJSONArray( final int _index )
	{
		final JSObject arr = optJSObject( array, _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return new WebJSONArray( arr ) ;
	}

	public String toString()
	{
		return array.toString() ;
	}

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
	public static native void putLong( final JSObject _array, final long _value ) ;

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