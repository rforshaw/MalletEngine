package com.linxonline.mallet.io.formats.json ;

import org.teavm.jso.json.JSON ;
import org.teavm.jso.JSObject ;
import org.teavm.jso.JSBody ;

public final class JArray
{
	protected final JSObject array ;

	protected JArray()
	{
		array = JSON.parse( "[]" ) ;
	}

	protected JArray( final String _source )
	{
		array = JSON.parse( _source ) ;
	}

	protected JArray( final JSObject _array )
	{
		array = _array ;
	}

	public static JArray construct()
	{
		return new JArray() ;
	}

	public static JArray construct( final String _source )
	{
		return new JArray( _source ) ;
	}

	public int length()
	{
		return length( array ) ;
	}

	public JArray put( final boolean _value )
	{
		putBoolean( array, _value ) ;
		return this ;
	}

	public JArray put( final int _value )
	{
		putInt( array, _value ) ;
		return this ;
	}

	public JArray put( final double _value )
	{
		putDouble( array, _value ) ;
		return this ;
	}

	public JArray put( final long _value )
	{
		putDouble( array, ( double )_value ) ;
		return this ;
	}

	public JArray put( final String _value )
	{
		putString( array, _value ) ;
		return this ;
	}

	public JArray put( final JObject _value )
	{
		putObject( array, ( ( JObject )_value ).object ) ;
		return this ;
	}

	public JArray put( final JArray _value )
	{
		putArray( array, ( ( JArray )_value ).array ) ;
		return this ;
	}

	public boolean getBoolean( final int _index )
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

	public int getInt( final int _index )
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

	public double getDouble( final int _index )
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

	public long getLong( final int _index )
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

	public String getString( final int _index )
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

	public JObject getJObject( final int _index )
	{
		final JSObject obj = optJSObject( array, _index ) ;
		if( obj == null )
		{
			return null ;
		}

		return new JObject( obj ) ;
	}

	public JObject optJObject( final int _index, final JObject _default )
	{
		final JSObject obj = optJSObject( array, _index ) ;
		if( obj == null )
		{
			return _default ;
		}

		return new JObject( obj ) ;
	}

	public JArray getJArray( final int _index )
	{
		final JSObject arr = optJSObject( array, _index ) ;
		if( arr == null )
		{
			return null ;
		}

		return new JArray( arr ) ;
	}

	public JArray optJArray( final int _index, final JArray _default )
	{
		final JSObject arr = optJSObject( array, _index ) ;
		if( arr == null )
		{
			return _default ;
		}

		return new JArray( arr ) ;
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
