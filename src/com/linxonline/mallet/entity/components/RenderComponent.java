package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.Settings ;
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
public class RenderComponent extends EventComponent
{
	private final static String REQUEST_TYPE = "REQUEST_TYPE" ;
	private final ArrayList<Event<Settings>> content = new ArrayList<Event<Settings>>() ;

	public RenderComponent()
	{
		super( "RENDER" ) ;
	}

	public RenderComponent( final String _name )
	{
		super( _name ) ;
	}
	
	public RenderComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	public void add( final Event<Settings> _draw )
	{
		content.add( _draw ) ;
	}

	public void remove( final Event<Settings> _draw )
	{
		content.remove( _draw ) ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
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
			draw.addInteger( REQUEST_TYPE, DrawRequestType.REMOVE_DRAW ) ;
			_events.add( event ) ;
		}
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
}