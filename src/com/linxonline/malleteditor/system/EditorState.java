package com.linxonline.malleteditor.system ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventProcessor ;

import com.linxonline.mallet.game.GameState ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.malleteditor.factory.EditorEntityFactory ;
import com.linxonline.malleteditor.factory.creators.EditorMouseCreator ;
import com.linxonline.malleteditor.factory.creators.EditorCreator ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

public class EditorState extends GameState
{
	public final static String[] EVENT_TYPES = { "OPEN_FILE", "IMPORT_FILE", "SAVE_FILE" } ;
	
	private final EditorEntityFactory factory = new EditorEntityFactory() ;

	public EditorState( final String _name )
	{
		super( _name ) ;
		initEventControllers() ;
		populateEntityFactory() ;
	}

	public void initGame()
	{
		createMouseAnimExample() ;
	}

	public void createMouseAnimExample()
	{
		final EditorMouseCreator creator = new EditorMouseCreator() ;
		addEntity( creator.create( null ) ) ;
	}

	private void initEventControllers()
	{
		eventController.setWantedEventTypes( EVENT_TYPES ) ;
		eventController.addEventProcessor( new EventProcessor( "FILE" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				if( _event.isEventByString( EVENT_TYPES[0] ) == true )
				{
					final String path = ( String )_event.getVariable() ;
					System.out.println( "Open File: " + path ) ;
					addEntities( factory.create( path ) ) ;
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
	
	private void populateEntityFactory()
	{
		factory.addCreator( new EditorCreator() ) ;
	}
}