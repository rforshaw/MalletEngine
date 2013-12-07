package com.linxonline.malleteditor.system ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventProcessor ;

import com.linxonline.mallet.game.GameState ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.util.factory.creators.AnimMouseCreator ;
import com.linxonline.mallet.util.settings.Settings ;

public class EditorState extends GameState
{
	private final static String[] EVENT_TYPES = { "OPEN_FILE", "IMPORT_FILE", "SAVE_FILE" } ;

	public EditorState( final String _name )
	{
		super( _name ) ;
		InitEventControllers() ;
	}

	public void initGame()
	{
		createMouseAnimExample() ;
	}

	public void createMouseAnimExample()
	{
		final Settings mouse = new Settings() ;
		mouse.addString( "ANIM", "base/anim/moomba.anim" ) ;
		mouse.addObject( "DIM", new Vector2( 32, 32 ) ) ;
		mouse.addObject( "OFFSET", new Vector2( -16, -16 ) ) ;

		final AnimMouseCreator creator = new AnimMouseCreator() ;
		addEntity( creator.create( mouse ) ) ;
	}

	private void InitEventControllers()
	{
		eventController.setWantedEventTypes( EVENT_TYPES ) ;
		eventController.addEventProcessor( new EventProcessor( "FILE" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				if( _event.isEventByString( EVENT_TYPES[0] ) == true )
				{
					System.out.println( "Open File Request" ) ;
				}
				else if( _event.isEventByString( EVENT_TYPES[1] ) == true )
				{
					System.out.println( "Import File Request" ) ;
				}
				else if( _event.isEventByString( EVENT_TYPES[2] ) == true )
				{
					System.out.println( "Save File Request" ) ;
				}
			}
		} ) ;
	}
}