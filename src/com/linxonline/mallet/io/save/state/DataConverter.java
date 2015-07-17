package com.linxonline.mallet.io.save.state ;

import java.util.HashMap ;
import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.io.save.state.DataSet.* ;

public class DataConverter
{
	private final HashMap<String, Track<?>> converters = new HashMap<String, Track<?>>() ;
	private final ArrayList<Track<?>> tracks = new ArrayList<Track<?>>() ;

	private final EventController events = new EventController() ;

	/**
		Add a DataOut and DataIn that works in concert with a DataSet.

		StateComponent will most likely be used for adding DataSets to 
		a DataConverter. As a DataSet will probably represent an Entity 
		or an individual Component.
		
		For DataOut and DataIn to recieve the DataSet they both must have 
		identical handlers. For example:
		
		DataConverter::track( "GENERIC_ENTITY_JSON", ... ) ;
		StateComponent::add( "GENERIC_ENTITY_JSON", genericEntityDat ) ;
	*/
	public <T extends DataSet> void track( final String _handler, final DataOut<T> _out, final DataIn<T> _in )
	{
		assert( converters.containsKey( _handler ) ) ;

		if( converters.containsKey( _handler ) == false )
		{
			final Track<T> track = new Track<T>() ;
			converters.put( _handler, track ) ;
			tracks.add( track ) ;
		}

		final Track<T> track = ( Track<T> )converters.get( _handler ) ;
		track.add( _out, _in ) ;

		events.addEventProcessor( new EventProcessor<T>( "TRACK_" + _handler, "TRACK_" + _handler )
		{
			public void processEvent( final Event<T> _event )
			{
				// Add the DataSet to the DataOut/In tracker
				track.add( _event.getVariable() ) ;
			}
		} ) ;

		events.addEventProcessor( new EventProcessor<T>( "UNTRACK_" + _handler, "UNTRACK_" + _handler )
		{
			public void processEvent( final Event<T> _event )
			{
				// Remove the DataSet to the DataOut/In tracker
				track.remove( _event.getVariable() ) ;
			}
		} ) ;
	}

	public void write()
	{
		final int size = tracks.size() ;
		for( int i = 0; i < size; ++i )
		{
			tracks.get( i ).write() ;
		}
	}

	public void read()
	{
		final int size = tracks.size() ;
		for( int i = 0; i < size; ++i )
		{
			tracks.get( i ).read() ;
		}
	}

	/**
		Process any events the Data Converter may have recieved.
	*/
	public void update()
	{
		events.update() ;
	}

	public EventController getEventController()
	{
		return events ;
	}

	private static class Track<T extends DataSet>
	{
		private final ArrayList<T> data = new ArrayList<T>() ;						// Data to be saved/loaded
		private final ArrayList<DataOut<T>> outs = new ArrayList<DataOut<T>>() ;	// Save implementations
		private final ArrayList<DataIn<T>> ins = new ArrayList<DataIn<T>>() ;		// Load implementations

		public Track() {}

		public void add( final DataOut<T> _out, final DataIn<T> _in )
		{
			outs.add( _out ) ;
			ins.add( _in ) ;
		}

		public void add( final T _toAdd )
		{
			data.add( _toAdd ) ;
		}

		public void remove( final T _toRemove )
		{
			data.remove( _toRemove ) ;
		}

		public void write()
		{
			for( final T t : data )
			{
				for( DataOut<T> out : outs )
				{
					out.write( t ) ;
				}
			}
		}

		public void read()
		{
			for( final T t : data )
			{
				for( DataIn<T> in : ins )
				{
					in.read( t ) ;
				}
			}
		}
	}
}