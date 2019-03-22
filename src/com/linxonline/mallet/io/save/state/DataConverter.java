package com.linxonline.mallet.io.save.state ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.io.save.state.DataSet.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public class DataConverter
{
	private final Map<String, Track<?>> converters = MalletMap.<String, Track<?>>newMap() ;
	private final List<Track<?>> tracks = MalletList.<Track<?>>newList() ;

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

		// Add the DataSet to the DataOut/In tracker
		events.addProcessor( "TRACK_" + _handler, ( final T _var ) -> track.add( _var ) ) ;

		// Remove the DataSet to the DataOut/In tracker
		events.addProcessor( "UNTRACK_" + _handler, ( final T _var ) -> track.remove( _var ) ) ;
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
		private final List<T> data = MalletList.<T>newList() ;						// Data to be saved/loaded
		private final List<DataOut<T>> outs = MalletList.<DataOut<T>>newList() ;		// Save implementations
		private final List<DataIn<T>> ins = MalletList.<DataIn<T>>newList() ;			// Load implementations

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
				for( final DataOut<T> out : outs )
				{
					out.write( t ) ;
				}
			}
		}

		public void read()
		{
			for( final T t : data )
			{
				for( final DataIn<T> in : ins )
				{
					in.read( t ) ;
				}
			}
		}
	}
}
