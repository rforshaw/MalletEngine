package com.linxonline.mallet.system ;

import java.util.HashMap ;
import java.util.Collection ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Designed to store running logistics of the game that can be 
	accessed in any area.
	Each statistic should be paired with a domain.  A domain 
	denotes a category like Rendering, AI, etc.
*/
public final class GlobalStats
{
	private final static HashMap<String, StatTrack> domains = new HashMap<String, StatTrack>() ;

	private GlobalStats() {}
	
	public static void registerDomain( final String _name )
	{
		if( domainExists( _name ) == false )
		{
			domains.put( _name, new StatTrack( _name ) ) ;
		}
	}

	public static void deleteDomain( final String _name )
	{
		if( domainExists( _name ) == true )
		{
			final StatTrack stats = domains.remove( _name ) ;
			if( stats != null )
			{
				stats.clear() ;
			}
		}
	}

	public static void addNotify( final String _domain, final String _name, final Notify<Domain> _notify )
	{
		final StatTrack stats = getStats( _domain ) ;
		stats.addNotify( _name, _notify ) ;
	}

	public static void removeNotify( final String _domain, final String _name, final Notify<Domain> _notify )
	{
		final StatTrack stats = getStats( _domain ) ;
		stats.removeNotify( _name, _notify ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final boolean _value )
	{
		final StatTrack stats = getStats( _domainName ) ;
		stats.addBoolean( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final int _value )
	{
		final StatTrack stats = getStats( _domainName ) ;
		stats.addInteger( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final float _value )
	{
		final StatTrack stats = getStats( _domainName ) ;
		stats.addFloat( _statName, _value ) ;
	}

	public static void updateDomainStat( final String _domainName, final String _statName, final String _value )
	{
		final StatTrack stats = getStats( _domainName ) ;
		stats.addString( _statName, _value ) ;
	}

	public static boolean getDomainStat( final String _domainName, final String _statName, final boolean _default )
	{
		final StatTrack stats = getStats( _domainName ) ;
		return stats.getBoolean( _statName, _default  ) ;
	}

	public static int getDomainStat( final String _domainName, final String _statName, final int _default )
	{
		final StatTrack stats = getStats( _domainName ) ;
		return stats.getInteger( _statName, _default ) ;
	}

	public static float getDomainStat( final String _domainName, final String _statName, final float _default )
	{
		final StatTrack stats = getStats( _domainName ) ;
		return stats.getFloat( _statName, _default ) ;
	}

	public static String getDomainStat( final String _domainName, final String _statName, final String _default )
	{
		final StatTrack stats = getStats( _domainName ) ;
		return stats.getString( _statName, _default ) ;
	}

	public static StatTrack getStats( final String _domainName )
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
		final Collection<StatTrack> values = domains.values() ;
		for( final StatTrack track : values )
		{
			buffer.append( track.toString() ) ;
		}
		return buffer.toString() ;
	}

	public static class StatTrack
	{
		private final String domain ;
		private final HashMap<String, Notification<Domain>> listeners = new HashMap<String, Notification<Domain>>() ;
		private final Settings stats = new Settings() ;

		public StatTrack( final String _domain )
		{
			domain = _domain ;
		}

		public void addNotify( final String _name, final Notify<Domain> _notify )
		{
			Notification<Domain> notification = listeners.get( _name ) ;
			if( notification == null )
			{
				notification = new Notification<Domain>() ;
				listeners.put( _name, notification ) ;
			}

			notification.addNotify( _notify ) ;
		}

		public void removeNotify( final String _name, final Notify<Domain> _notify )
		{
			final Notification<Domain> notification = listeners.get( _name ) ;
			if( notification != null )
			{
				notification.removeNotify( _notify ) ;
			}
		}

		public void addBoolean( final String _name, final boolean _value )
		{
			stats.addBoolean( _name, _value ) ;
			inform( _name ) ;
		}

		public void addInteger( final String _name, final int _value )
		{
			stats.addInteger( _name, _value ) ;
			inform( _name ) ;
		}

		public void addFloat( final String _name, final float _value )
		{
			stats.addFloat( _name, _value ) ;
			inform( _name ) ;
		}

		public void addString( final String _name, final String _value )
		{
			stats.addString( _name, _value ) ;
			inform( _name ) ;
		}

		public void addObject( final String _name, final Object _value )
		{
			stats.addObject( _name, _value ) ;
			inform( _name ) ;
		}

		public boolean getBoolean( final String _name, final boolean _default )
		{
			return stats.getBoolean( _name, _default ) ;
		}

		public int getInteger( final String _name, final int _default )
		{
			return stats.getInteger( _name, _default ) ;
		}

		public float getFloat( final String _name, final float _default )
		{
			return stats.getFloat( _name, _default ) ;
		}

		public String getString( final String _name, final String _default )
		{
			return stats.getString( _name, _default ) ;
		}

		public <T> T getObject( final String _name, final T _default )
		{
			return stats.getObject( _name, _default ) ;
		}

		public void clear()
		{
			final Collection<Notification<Domain>> values = listeners.values() ;
			for( final Notification<Domain> notifications : values )
			{
				notifications.clear() ;
			}
		}

		public String toString()
		{
			return stats.toString() ;
		}

		private void inform( final String _name )
		{
			final Notification<Domain> notification = listeners.get( _name ) ;
			if( notification != null )
			{
				notification.inform( new Domain( domain, _name ) ) ;
			}
		}
	}
	
	public static class Domain
	{
		private final String domain ;
		private final String statName ;

		public Domain( final String _domain, final String _statName )
		{
			domain = _domain ;
			statName = _statName ;
		}

		public String getDomain()
		{
			return domain ;
		}

		public String getStatName()
		{
			return statName ;
		}
	}
}
