package com.linxonline.mallet.io.formats.json ;

public interface IJSONObject
{
	public String[] keys() ;
	public boolean has( final String _key ) ;

	public JSONObject put( final String _key, final boolean _value ) ;
	public JSONObject put( final String _key, final int _value ) ;
	public JSONObject put( final String _key, final double _value ) ;
	public JSONObject put( final String _key, final long _value ) ;
	public JSONObject put( final String _key, final String _value ) ;
	public JSONObject put( final String _key, final JSONObject _value ) ;
	public JSONObject put( final String _key, final JSONArray _value ) ;

	public boolean getBoolean( final String _key ) ;
	public boolean optBoolean( final String _key, final boolean _default ) ;

	public int getInt( final String _key ) ;
	public int optInt( final String _key, final int _default ) ;

	public double getDouble( final String _key ) ;
	public double optDouble( final String _key, final double _default ) ;

	public long getLong( final String _key ) ;
	public long optLong( final String _key, final long _default ) ;

	public String getString( final String _key ) ;
	public String optString( final String _key, final String _default ) ;

	public JSONObject getJSONObject( final String _key ) ;
	public JSONObject optJSONObject( final String _key, final JSONObject _default ) ;

	public JSONArray getJSONArray( final String _key ) ;
	public JSONArray optJSONArray( final String _key, final JSONArray _default ) ;

	public String toString() ;
	public String toString( final int _indent ) ;

	public interface ConstructCallback
	{
		public void callback( final JSONObject _object ) ;
	}
}
