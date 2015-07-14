package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.io.save.state.DataSet ;

/**
	Stores individual component state in a centralised location.

	This allows a component state to be easily saved/loaded 
	without needing to know the internal composition of the entity.

	Use StateComponent to store state that must persist between 
	games or be transfered across a network to keep an entity insync.
	
	State may include the entity's position, direction, active animation, 
	current dimensions, etc. The StateComponent should not detail how the 
	entity is constructed, but how it differs from a default factory version.
*/
public class StateComponent extends Component
{
	private final ArrayList<DataSet> data = new ArrayList<DataSet>() ;

	public StateComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	public <T extends DataSet> T add( final T _data )
	{
		data.add( _data ) ;
		return _data ;
	}

	public <T extends DataSet> boolean remove( final T _data )
	{
		return data.remove( _data ) ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		_events.add( new Event<ArrayList<DataSet>>( "REGISTER_RUNTIME_STATE", data ) ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( new Event<ArrayList<DataSet>>( "UNREGISTER_RUNTIME_STATE", data ) ) ;
	}
}
