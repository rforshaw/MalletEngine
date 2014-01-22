package com.linxonline.malleteditor.system ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventProcessor ;

import com.linxonline.mallet.game.GameState ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.malleteditor.system.MainPanel ;
import com.linxonline.malleteditor.factory.EditorEntityFactory ;
import com.linxonline.malleteditor.factory.creators.EditorMouseCreator ;
import com.linxonline.malleteditor.factory.creators.EditorCreator ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

public class EditorState extends GameState
{
	public final static String[] EVENT_TYPES = { "OPEN_FILE", "IMPORT_FILE", "SAVE_FILE", "ADD_ENTITY", "REMOVE_ENTITY" } ;
	
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
					// Load Entities from file
					final String path = ( String )_event.getVariable() ;
					final ArrayList<Entity> entities = factory.create( path ) ;

					for( final Entity entity : entities )
					{
						addEntity( entity ) ;
						system.addEvent( new Event( MainPanel.EVENT_TYPES[0], entity ) ) ;
					}
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

		eventController.addEventProcessor( new EventProcessor( "ENTITY" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				if( _event.isEventByString( EVENT_TYPES[3] ) == true )
				{
					// Add Entity to Game State
					final Entity entity = ( Entity )_event.getVariable() ;
					addEntity( entity ) ;
				}
				else if( _event.isEventByString( EVENT_TYPES[4] ) == true )
				{
					// Remove Entity to Game State
					final Entity entity = ( Entity )_event.getVariable() ;
					removeEntity( entity ) ;
				}
			}
		} ) ;
	}
	
	private void populateEntityFactory()
	{
		factory.addCreator( new EditorCreator() ) ;
	}
}