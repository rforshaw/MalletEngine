package com.linxonline.mallet.io.formats.json ;

/**
	A wrapper class around a platform specific implementation 
	of the JSON format.
	Allows different platforms to use a JSON library built for 
	that platform.
*/
public abstract class JSONArray
{
	protected static JSONArray constructor = null ;

	public JSONArray() {}

	public static JSONArray construct()
	{
		return constructor.create() ;
	}

	public static JSONArray construct( final String _source )
	{
		return constructor.create( _source ) ;
	}

	protected abstract JSONArray create() ;
	protected abstract JSONArray create( final String _source ) ;

	public abstract int length() ;

	public abstract JSONArray put( final boolean _value ) ;
	public abstract JSONArray put( final int _value ) ;
	public abstract JSONArray put( final double _value ) ;
	public abstract JSONArray put( final long _value ) ;
	public abstract JSONArray put( final String _value ) ;
	public abstract JSONArray put( final JSONObject _value ) ;
	public abstract JSONArray put( final JSONArray _value ) ;

	public abstract boolean getBoolean( final int _index ) ;
	public abstract boolean optBoolean( final int _index, final boolean _default ) ;

	public abstract int getInt( final int _index ) ;
	public abstract int optInt( final int _index, final int _default ) ;

	public abstract double getDouble( final int _index ) ;
	public abstract double optDouble( final int _index, final double _default ) ;

	public abstract long getLong( final int _index ) ;
	public abstract long optLong( final int _index, final long _default ) ;
	
	public abstract String getString( final int _index ) ;
	public abstract String optString( final int _index, final String _default ) ;

	public abstract JSONObject getJSONObject( final int _index ) ;
	public abstract JSONObject optJSONObject( final int _index, final JSONObject _default ) ;

	public abstract JSONArray getJSONArray( final int _index ) ;
	public abstract JSONArray optJSONArray( final int _index, final JSONArray _default ) ;

	public abstract String toString() ;
	public abstract String toString( final int _indent ) ;
}
