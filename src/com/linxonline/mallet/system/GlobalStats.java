package com.linxonline.mallet.system ;

import java.util.HashMap ;
import java.util.Collection ;

import com.linxonline.mallet.util.settings.Settings ;

/**
	Designed to store running logistics of the game that can be 
	accessed in any area.
	Each statistic should be paired with a domain.  A domain 
	denotes a category like Rendering, AI, etc.
*/
public class GlobalStats
{
	private final static HashMap<String, Settings> domains = new HashMap<String, Settings>() ;

	private GlobalStats() {}
	
	public static void registerDomain( final String _name )
	{
		if( domainExists( _name ) == false )
		{
			domains.put( _name, new Settings() ) ;
		}
	}

	public static void deleteDomain( final String _name )
	{
		if( domainExists( _name ) == true )
		{
			domains.remove( _name ) ;
		}
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final boolean _value )
	{
		final Settings stats = getStats( _domainName ) ;
		stats.addBoolean( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final int _value )
	{
		final Settings stats = getStats( _domainName ) ;
		stats.addInteger( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final float _value )
	{
		final Settings stats = getStats( _domainName ) ;
		stats.addFloat( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final String _value )
	{
		final Settings stats = getStats( _domainName ) ;
		stats.addString( _statName, _value ) ;
	}

	public static boolean getDomainStat( final String _domainName, final String _statName, final boolean _default )
	{
		final Settings stats = getStats( _domainName ) ;
		return stats.getBoolean( _statName, _default  ) ;
	}

	public static int getDomainStat( final String _domainName, final String _statName, final int _default )
	{
		final Settings stats = getStats( _domainName ) ;
		return stats.getInteger( _statName, _default ) ;
	}

	public static float getDomainStat( final String _domainName, final String _statName, final float _default )
	{
		final Settings stats = getStats( _domainName ) ;
		return stats.getFloat( _statName, _default ) ;
	}

	public static String getDomainStat( final String _domainName, final String _statName, final String _default )
	{
		final Settings stats = getStats( _domainName ) ;
		return stats.getString( _statName, _default ) ;
	}

	public static Settings getStats( final String _domainName )
	{
		if( domainExists( _domainName ) == false )
		{
			registerDomain( _domainName ) ;
		}

		return domains.get( _domainName ) ;
	}

	public static boolean domainExists( final String _name )
	{
		return domains.containsKey( _name ) ;
	}

	public static String getString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		final Collection<Settings> values = domains.values() ;
		for( Settings set : values )
		{
			buffer.append( set.toString() ) ;
		}
		return buffer.toString() ;
	}
}