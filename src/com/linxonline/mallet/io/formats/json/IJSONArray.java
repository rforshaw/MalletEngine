package com.linxonline.mallet.io.formats.json ;

/**
	A wrapper class around a platform specific implementation 
	of the JSON format.
	Allows different platforms to use a JSON library built for 
	that platform.
*/
public interface IJSONArray
{
	public int length() ;

	public JSONArray put( final boolean _value ) ;
	public JSONArray put( final int _value ) ;
	public JSONArray put( final double _value ) ;
	public JSONArray put( final long _value ) ;
	public JSONArray put( final String _value ) ;
	public JSONArray put( final JSONObject _value ) ;
	public JSONArray put( final JSONArray _value ) ;

	public boolean getBoolean( final int _index ) ;
	public boolean optBoolean( final int _index, final boolean _default ) ;

	public int getInt( final int _index ) ;
	public int optInt( final int _index, final int _default ) ;

	public double getDouble( final int _index ) ;
	public double optDouble( final int _index, final double _default ) ;

	public long getLong( final int _index ) ;
	public long optLong( final int _index, final long _default ) ;
	
	public String getString( final int _index ) ;
	public String optString( final int _index, final String _default ) ;

	public JSONObject getJSONObject( final int _index ) ;
	public JSONObject optJSONObject( final int _index, final JSONObject _default ) ;

	public JSONArray getJSONArray( final int _index ) ;
	public JSONArray optJSONArray( final int _index, final JSONArray _default ) ;

	public String toString() ;
	public String toString( final int _indent ) ;
}
