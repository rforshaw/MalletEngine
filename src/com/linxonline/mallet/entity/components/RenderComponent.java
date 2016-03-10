package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.system.* ;

/**
	RenderComponent is a container class for all Events calls 
	used by the Entity to display graphics via the Renderer.

	These events contain a Settings object that defines what and 
	how it should be displayed.

	By default addDrawCalls() is called when an Entity is hooked 
	into a Game State.

	Use removeDrawCalls() to remove all Events from the renderer 
	that is located in content.
**/
public class RenderComponent extends EventComponent implements IDInterface
{
	private final SourceTracker tracker = new SourceTracker() ;
	private final ArrayList<Event<Settings>> content = new ArrayList<Event<Settings>>() ;

	private boolean initialEventDone = false ;				// false when not hooked up, true when initial events have been called.
	private Component.ReadyCallback toDestroy = null ;

	public RenderComponent()
	{
		this( "RENDER" ) ;
	}

	public RenderComponent( final String _name )
	{
		super( _name ) ;
	}

	public RenderComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	public Event<Settings> add( final Event<Settings> _draw )
	{
		content.add( _draw ) ;
		if( initialEventDone == true )
		{
			passEvent( _draw ) ;
		}
		return _draw ;
	}

	public void remove( final Event<Settings> _draw )
	{
		content.remove( _draw ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		if( toDestroy != null && tracker.isStable() == true )
		{
			toDestroy.ready( this ) ;
			toDestroy = null ;
		}
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		initialEventDone = true ;

		super.passInitialEvents( _events ) ;
		final int length = content.size() ;
		for( int i = 0; i < length; ++i )
		{
			_events.add( content.get( i ) ) ;
		}
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		final int length = content.size() ;

		Settings draw = null ;
		Event<Settings> event = null ;

		for( int i = 0; i < length; ++i )
		{
			event = content.get( i ) ;
			draw = event.<Settings>getVariable() ;
			draw.addObject( "REQUEST_TYPE", DrawRequestType.REMOVE_DRAW ) ;
			_events.add( event ) ;
		}
		content.clear() ;
	}

	@Override
	public void recievedID( final int _id )
	{
		tracker.recievedID( _id ) ;
	}

	/**
		We need to make sure we aren't waiting for any 
		render ID's before we allow the parent to destroy 
		themselves.
	*/
	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		toDestroy = _callback ;
	}

	public int drawSize()
	{
		return content.size() ;
	}

	public Event<Settings> getEventAt( final int _pos )
	{
		return content.get( _pos ) ;
	}

	public Settings getDrawAt( final int _pos )
	{
		return content.get( _pos ).<Settings>getVariable() ;
	}

	/**
		Keep track of the render id's ensure that the Render 
		Component is stable.
		Important to prevent the parent entity destroying 
		itself without cleaning up render requests.
	*/
	private class SourceTracker implements IDInterface
	{
		private int recieved = 0 ;

		public SourceTracker() {}

		@Override
		public void recievedID( final int _id )
		{
			++recieved ;
		}

		public boolean isStable()
		{
			return recieved >= RenderComponent.this.drawSize() ;
		}
	}
}