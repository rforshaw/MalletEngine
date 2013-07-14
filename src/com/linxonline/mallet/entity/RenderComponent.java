package com.linxonline.mallet.entity ;

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
	private final ArrayList<Event> content = new ArrayList<Event>() ;

	public RenderComponent()
	{
		super( "RENDER" ) ;
	}

	public RenderComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	public void add( final Event _draw )
	{
		content.add( _draw ) ;
	}

	public void remove( final Event _draw )
	{
		content.remove( _draw ) ;
	}

	@Override
	public void sendInitialEvents()
	{
		final int length = content.size() ;
		for( int i = 0; i < length; ++i )
		{
			passEvent( content.get( i ) ) ;
		}
	}

	@Override
	public void sendFinishEvents()
	{
		final int length = content.size() ;
		Settings draw = null ;
		Event event = null ;

		for( int i = 0; i < length; ++i )
		{
			event = content.get( i ) ;
			draw = event.getVariable( Settings.class ) ;
			draw.addInteger( REQUEST_TYPE, DrawRequestType.REMOVE_DRAW ) ;
			passEvent( event ) ;
		}
	}

	public int drawSize()
	{
		return content.size() ;
	}

	public Settings getDrawAt( final int _pos )
	{
		return content.get( _pos ).getVariable( Settings.class ) ;
	}
}