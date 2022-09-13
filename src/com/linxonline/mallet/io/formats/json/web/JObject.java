package com.linxonline.mallet.io.formats.json ;

import org.teavm.jso.json.JSON ;
import org.teavm.jso.JSObject ;
import org.teavm.jso.JSBody ;

import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringInStream ;
import com.linxonline.mallet.io.filesystem.StringInCallback ;

public final class JObject
{
	protected final JSObject object ;

	protected JObject()
	{
		object = JSON.parse( "{}" ) ;
	}

	protected JObject( final String _source )
	{
		object = JSON.parse( _source ) ;
	}

	protected JObject( final JSObject _object )
	{
		object = _object ;
	}

	public static JObject construct()
	{
		return new JObject() ;
	}

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

	public static JObject construct( final String _source )
	{
		return new JObject( _source ) ;
	}

	public String[] keys()
	{
		return keys( object ) ;
	}

	public boolean has( final String _key )
	{
		return hasKey( object, _key ) ;
	}

	public JObject put( final String _key, final boolean _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	public JObject put( final String _key, final int _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	public JObject put( final String _key, final double _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	public JObject put( final String _key, final long _value )
	{
		put( object, _key, ( double )_value ) ;
		return this ;
	}

	public JObject put( final String _key, final String _value )
	{
		put( object, _key, _value ) ;
		return this ;
	}

	public JObject put( final String _key, final JObject _value )
	{
		put( object, _key, _value.object ) ;
		return this ;
	}

	public JObject put( final String _key, final JArray _value )
	{
		put( object, _key, _value.array ) ;
		return this ;
	}

	public boolean getBoolean( final String _key )
	{
		return optBoolean( object, _key ) ;
	}

	public boolean optBoolean( final String _key, final boolean _default )
	{
		if( has( _key ) )
		{
			return optBoolean( object, _key ) ;
		}

		return _default ;
	}

	public int getInt( final String _key )
	{
		return optInt( object, _key ) ;
	}

	public int optInt( final String _key, final int _default )
	{
		if( has( _key ) )
		{
			return optInt( object, _key ) ;
		}

		return _default ;
	}

	public double getDouble( final String _key )
	{
		return optDouble( object, _key ) ;
	}

	public double optDouble( final String _key, final double _default )
	{
		if( has( _key ) )
		{
			return optDouble( object, _key ) ;
		}

		return _default ;
	}

	public long getLong( final String _key )
	{
		return ( long )optInt( object, _key ) ;
	}

	public long optLong( final String _key, final long _default )
	{
		if( has( _key ) )
		{
			return ( long )optInt( object, _key ) ;
		}

		return _default ;
	}

	public String getString( final String _key )
	{
		return optString( _key, null ) ;
	}

	public String optString( final String _key, final String _default )
	{
		if( has( _key ) )
		{
			return optString( object, _key ) ;
		}

		return _default ;
	}

	public JObject getJObject( final String _key )
	{
		if( has( _key ) == false )
		{
			return null ;
		}

		return new JObject( optJSObject( object, _key ) ) ;
	}

	public JObject optJObject( final String _key, final JObject _default )
	{
		if( has( _key ) == false )
		{
			return _default ;
		}

		return new JObject( optJSObject( object, _key ) ) ;
	}

	public JArray getJArray( final String _key )
	{
		if( has( _key ) == false )
		{
			return null ;
		}

		return new JArray( optJSArray( object, _key ) ) ;
	}

	public JArray optJArray( final String _key, final JArray _default )
	{
		if( has( _key ) == false )
		{
			return _default ;
		}

		return new JArray( optJSArray( object, _key ) ) ;
	}

	public String toString()
	{
		return JSON.stringify( object ) ;
	}

	public String toString( final int _indent )
	{
		return JSON.stringify( object ) ;
	}

	public interface ConstructCallback
	{
		public void callback( JObject _object ) ;
	}

	@JSBody( params = { "_obj", "_key" }, script = "return _obj.hasOwnProperty( _key ) ;" )
	public static native boolean hasKey( final JSObject _obj, String _key ) ;

	@JSBody( params = { "_obj" }, script = "return Object.keys( _obj ) ;" )
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
