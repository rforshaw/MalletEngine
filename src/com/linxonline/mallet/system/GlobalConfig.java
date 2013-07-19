package com.linxonline.mallet.system ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.Settings ;

/**
	Provides global access to system settings.
	Global Config should contain information that 
	a range of system may be interested in. This 
	information could be render/display dimensions, 
	player name, server settings, etc.
**/
public class GlobalConfig
{
	private static Settings config = new Settings() ;

	private GlobalConfig() {}

	public static void setConfig( final Settings _config )
	{
		config = _config ;
	}

	public static void addBoolean( final String _name, final boolean _value )
	{
		config.addBoolean( _name, _value ) ;
	}

	public static void addInteger( final String _name, final int _value )
	{
		config.addInteger( _name, _value ) ;
	}

	public static void addFloat( final String _name, final float _value )
	{
		config.addFloat( _name, _value ) ;
	}

	public static void addString( final String _name, final String _value )
	{
		config.addString( _name, _value ) ;
	}

	public static void addObject( final String _name, final Object _value )
	{
		config.addObject( _name, _value ) ;
	}

	public static boolean getBoolean( final String _name, final boolean _default )
	{
		return config.getBoolean( _name, _default ) ;
	}

	public static int getInteger( final String _name, final int _default )
	{
		return config.getInteger( _name, _default ) ;
	}

	public static float getFloat( final String _name, final float _default )
	{
		return config.getFloat( _name, _default ) ;
	}

	public static String getString( final String _name, final String _default )
	{
		return config.getString( _name, _default ) ;
	}

	public static <T> T getObject( final String _name, final Class<T> _type, final T _default )
	{
		return config.getObject( _name, _type, _default ) ;
	}

	public static ArrayList<String> toArrayString()
	{
		return config.toArrayString() ;
	}

	public final String toString()
	{
		return config.toString() ;
	}
}