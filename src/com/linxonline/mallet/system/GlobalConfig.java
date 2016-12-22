package com.linxonline.mallet.system ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Provides global access to system settings.
	Global Config should contain information that 
	a range of system may be interested in. This 
	information could be render/display dimensions, 
	player name, server settings, etc.
**/
public final class GlobalConfig
{
	private static HashMap<String, Notification<String>> listeners = new HashMap<String, Notification<String>>() ;
	private static Settings config = new Settings() ;

	private GlobalConfig() {}

	public static void setConfig( final Settings _config )
	{
		assert _config != null ;
		config = _config ;
	}

	public static Settings getConfig()
	{
		return config ;
	}
	
	/**
		Allow an object to be informed when a setting 
		has been changed.
		_notify can be used for multiple settings.
		_notify will recieve the name of the setting 
		that has been changed. 
	*/
	public static void addNotify( final String _name, final Notify<String> _notify )
	{
		Notification<String> notification = listeners.get( _name ) ;
		if( notification == null )
		{
			notification = new Notification<String>() ;
			listeners.put( _name, notification ) ;
		}

		notification.addNotify( _notify ) ;
	}

	public static void removeNotify( final String _name, final Notify<String> _notify )
	{
		final Notification<String> notification = listeners.get( _name ) ;
		if( notification != null )
		{
			notification.removeNotify( _notify ) ;
		}
	}

	/**
		Inform all listeners to a particular setting 
		that it has been changed.
		One notify can be used for multiple settings.
		So pass the name of the setting that has been 
		changed.
	*/
	private static void inform( final String _name )
	{
		final Notification<String> notification = listeners.get( _name ) ;
		if( notification != null )
		{
			notification.inform( _name ) ;
		}
	}

	public static void addBoolean( final String _name, final boolean _value )
	{
		config.addBoolean( _name, _value ) ;
		inform( _name ) ;
	}

	public static void addInteger( final String _name, final int _value )
	{
		config.addInteger( _name, _value ) ;
		inform( _name ) ;
	}

	public static void addFloat( final String _name, final float _value )
	{
		config.addFloat( _name, _value ) ;
		inform( _name ) ;
	}

	public static void addString( final String _name, final String _value )
	{
		config.addString( _name, _value ) ;
		inform( _name ) ;
	}

	public static void addObject( final String _name, final Object _value )
	{
		config.addObject( _name, _value ) ;
		inform( _name ) ;
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

	public static <T> T getObject( final String _name, final T _default )
	{
		return config.getObject( _name, _default ) ;
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
