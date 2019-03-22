package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

/**
	Allow the developer to track modificatins being made to 
	data associated with a particular object.

	An object in which its state changes and wishes to inform 
	others of this change is considered a signal.

	An object that is interested in being informed of another 
	objects state change is called a slot.

	A signal can contain multiple data-points that change - if a slot 
	registers a connection with just the signal then it will be informed 
	of any state change made on that object.

	A slot can connect with a specific variable of a signal and track only 
	changes made to that variable. Variables that are primitives or can change 
	reference i.e. non-final objects should not be connected to.

	A signal must call Connect.signal( signal, variable ) if it wishes to inform 
	slots that state has changed. A signal must also call Connect.disconnect( signal ) 
	if the signal is being destroyed.

	A slot that wishes to remove itself from receiving further changes can also 
	call Connect.disconnect( signal, variable, slot ).
*/
public class Connect
{
	private final Map<Object, Map<Signal, List<Slot<Object>>>> connections = MalletMap.<Object, Map<Signal, List<Slot<Object>>>>newMap() ;

	public Connect() {}

	/**
		Connect the slot to the signal, a signal may contain 
		multiple data-points if any of those data points change 
		the slot will be informed of the change.

		Signals or Variables can not be immutable - for example String.
	*/
	public <T> boolean connect( final T _element, final Signal _signal, final Slot<T> _slot )
	{
		if( _element == null || _signal == null )
		{
			Logger.println( "Unable to map signal to slot - signal is null.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		if( _slot == null )
		{
			Logger.println( "Unable to map signal to slot - slot is null.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		Map<Signal, List<Slot<Object>>> lookups = connections.get( _element ) ;
		if( lookups == null )
		{
			lookups = MalletMap.<Signal, List<Slot<Object>>>newMap() ;
			connections.put( _element, lookups ) ;
		}

		List<Slot<Object>> slots = lookups.get( _signal ) ;
		if( slots == null )
		{
			slots = MalletList.<Slot<Object>>newList() ;
			lookups.put( _signal, slots ) ;
		}

		if( slots.contains( _slot ) == true )
		{
			Logger.println( _slot + " already mapped to " + _element, Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		slots.add( ( Slot<Object> )_slot ) ;
		return true ;
	}

	/**
		Disconnect the specific slot from a particular signal
		and associated variable.

		Disconnect will not work if the signal or variable is 
		immutable.
	*/
	public <T> boolean disconnect( final T _element, final Signal _signal, final Slot<T> _slot )
	{
		if( _element == null )
		{
			Logger.println( "Unable to disconnect slot from signal - signal is null.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		if( _slot == null )
		{
			Logger.println( "Unable to disconnect slot from signal - slot is null.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		final Map<Signal, List<Slot<Object>>> lookups = connections.get( _element ) ;
		if( lookups == null )
		{
			Logger.println( _element + " element has no connections.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		final List<Slot<Object>> slots = lookups.get( _signal ) ;
		if( slots == null )
		{
			Logger.println( _signal + " signal has no connections.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		if( slots.remove( _slot ) == false )
		{
			Logger.println( _slot + " has no connection to " + _signal, Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		if( slots.isEmpty() == true )
		{
			// If there are no connections left we will automatically
			// remove the signal from the connection list.
			lookups.remove( _signal ) ;
		}

		return true ;
	}

	/**
		Disconnect all slots from a signal.
	*/
	public <T> boolean disconnect( final T _element )
	{
		if( _element == null )
		{
			Logger.println( "Unable to disconnect slots from signal - signal is null.", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		connections.remove( _element ) ;
		return true ;
	}

	/**
		Inform all slots connected to this signal and associated
		to the variable that the signal's state has changed.
	*/
	public <T> void signal( final T _element, final Signal _signal )
	{
		if( _element == null )
		{
			Logger.println( "Unable to inform connections - signal is null", Logger.Verbosity.MAJOR ) ;
			return ;
		}
	
		final Map<Signal, List<Slot<Object>>> lookups = connections.get( _element ) ;
		if( lookups == null )
		{
			return ;
		}

		final List<Slot<Object>> slots = lookups.get( _signal ) ;
		if( slots == null )
		{
			return ;
		}

		final int size = slots.size() ;
		for( int i = 0; i < size; i++ )
		{
			slots.get( i ).slot( _element ) ;
		}
	}

	public interface Connection
	{
		public Connect getConnect() ;
	}

	public static class Signal {}

	@FunctionalInterface
	public interface Slot<T>
	{
		public void slot( final T _signal ) ;
	}
}
