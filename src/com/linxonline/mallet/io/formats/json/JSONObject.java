package com.linxonline.mallet.io.formats.json ;

/**
	A wrapper class around a platform specific implementation 
	of the JSON format.
	Allows different platforms to use a JSON library built for 
	that platform, but provide a consistent interface on all platforms.
	Note: The wrapper does not support multiple json libraries 
	simultaneously.
*/
public abstract class JSONObject
{
	protected static JSONObject constructor = null ;

	public JSONObject() {}

	/**
		Create a blank JSON Object
	*/
	public static JSONObject construct()
	{
		return constructor.create() ;
	}

	/**
		Create a JSON Object/s from source.
	*/
	public static JSONObject construct( final String _source )
	{
		return constructor.create( _source ) ;
	}

	protected abstract JSONObject create() ;
	protected abstract JSONObject create( final String _source ) ;

	public abstract String[] keys() ;
	public abstract boolean has( final String _key ) ;

	public abstract JSONObject put( final String _key, final boolean _value ) ;
	public abstract JSONObject put( final String _key, final int _value ) ;
	public abstract JSONObject put( final String _key, final double _value ) ;
	public abstract JSONObject put( final String _key, final long _value ) ;
	public abstract JSONObject put( final String _key, final String _value ) ;
	public abstract JSONObject put( final String _key, final JSONObject _value ) ;
	public abstract JSONObject put( final String _key, final JSONArray _value ) ;

	public abstract boolean optBoolean( final String _key ) ;
	public abstract boolean optBoolean( final String _key, final boolean _default ) ;

	public abstract int optInt( final String _key ) ;
	public abstract int optInt( final String _key, final int _default ) ;

	public abstract double optDouble( final String _key ) ;
	public abstract double optDouble( final String _key, final double _default ) ;

	public abstract long optLong( final String _key ) ;
	public abstract long optLong( final String _key, final long _default ) ;

	public abstract String optString( final String _key ) ;
	public abstract String optString( final String _key, final String _default ) ;

	public abstract JSONObject optJSONObject( final String _key ) ;
	public abstract JSONArray optJSONArray( final String _key ) ;

	public abstract String toString() ;
	public abstract String toString( final int _indent ) ;
}