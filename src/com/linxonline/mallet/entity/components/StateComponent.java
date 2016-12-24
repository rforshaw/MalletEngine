package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.util.Utility ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.io.save.state.DataSet ;

/**
	Stores implementations of DataSet in a centralised location.

	This allows a component state to be easily saved/loaded 
	without needing to know the internal composition of the entity.

	Use StateComponent to store state that must persist between 
	games or be transfered across a network to keep an entity insync.
	
	State may include the entity's position, direction, active animation, 
	current dimensions, etc. The StateComponent should not detail how the 
	entity is constructed, but how it differs from a default factory version.

	Once a DataSet has been added it can only be removed by destroying the Entity.
*/
public class StateComponent extends EventComponent
{
	private boolean initialEventDone = false ;
	private final List<Tuple<String, DataSet>> data = Utility.<Tuple<String, DataSet>>newArrayList() ;

	public StateComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	/**
		Add the DataSet for tracking, works in concert with DataConverter.

		An implementation of DataSet works in concert with implementations of 
		DataOut and DataIn that know how to save/read the DataSet.

		Add the DataSet with a matching handler that matches the 
		same handler used by the DataOut/DataIn when it was added to the 
		GameStates DataConverter.
	*/
	public <T extends DataSet> T add( final String _handler, final T _data )
	{
		final Tuple<String, DataSet> set = new Tuple<String, DataSet>( _handler, _data ) ;
		data.add( set ) ;

		if( initialEventDone == true )
		{
			passEvent( constructEvent( "TRACK_", set ) ) ;
		}

		return _data ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		for( final Tuple<String, DataSet> set : data )
		{
			_events.add( constructEvent( "TRACK_", set ) ) ;
		}

		initialEventDone = true ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		for( final Tuple<String, DataSet> set : data )
		{
			_events.add( constructEvent( "UNTRACK_", set ) ) ;
		}
	}

	private static Event<DataSet> constructEvent( final String _type, final Tuple<String, DataSet> _set )
	{
		return new Event<DataSet>( _type + _set.getLeft(), _set.getRight() ) ;
	}
}
