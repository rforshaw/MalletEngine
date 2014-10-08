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
import com.linxonline.mallet.event.EventController ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

public class EditorState extends GameState
{
	public final static String[] EVENT_TYPES = { "OPEN_FILE", "IMPORT_FILE", "SAVE_FILE", "ADD_ENTITY", "REMOVE_ENTITY" } ;
	
	private final EventController editorController = new EventController( "EDITOR_CONTROLLER" ) ;						// Used to process Events, gateway between internal eventSystem and root event-system
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

	@Override
	protected void initModes()
	{
		useApplicationMode() ;
	}

	public void createMouseAnimExample()
	{
		final EditorMouseCreator creator = new EditorMouseCreator() ;
		addEntity( creator.create( null ) ) ;
	}

	private void initEventControllers()
	{
		editorController.addEventProcessor( new EventProcessor( "EVENT" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				System.out.println( "Event: " + _event ) ;
			}
		} ) ;

		editorController.setWantedEventTypes( EVENT_TYPES ) ;
		editorController.addEventProcessor( new EventProcessor<String>( "FILE", "OPEN_FILE" )
		{
			@Override
			public void processEvent( final Event<String> _event )
			{
				System.out.println( "OPEN FILE" ) ;
				// Load Entities from file
				final String path = _event.getVariable() ;
				final ArrayList<Entity> entities = factory.create( path ) ;

				for( final Entity entity : entities )
				{
					addEntity( entity ) ;
					system.addEvent( new Event( MainPanel.EVENT_TYPES[0], entity ) ) ;
				}
			}
		} ) ;

		editorController.addEventProcessor( new EventProcessor( "FILE", "IMPORT_FILE" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				System.out.println( "Import File Request" ) ;
			}
		} ) ;

		editorController.addEventProcessor( new EventProcessor( "FILE", "SAVE_FILE" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				System.out.println( "Save File Request" ) ;
			}
		} ) ;

		editorController.addEventProcessor( new EventProcessor<Entity>( "ENTITY", "ADD_ENTITY" )
		{
			@Override
			public void processEvent( final Event<Entity> _event )
			{
				// Add Entity to Game State
				addEntity( _event.getVariable() ) ;
			}
		} ) ;

		editorController.addEventProcessor( new EventProcessor<Entity>( "ENTITY", "REMOVE_ENTITY" )
		{
			@Override
			public void processEvent( final Event<Entity> _event )
			{
				// Remove Entity to Game State
				removeEntity( _event.getVariable() ) ;
			}
		} ) ;
	}

	@Override
	protected void useApplicationMode()
	{
		currentUpdate = new UpdateInterface()
		{
			@Override
			public void update( final double _dt )
			{
				final boolean hasInput = inputSystem.hasInputs() ;
				final boolean hasEvents = eventSystem.hasEvents() ;
				if( hasInput == false && hasEvents == false )
				{
					system.sleep( 10 ) ;
				}

				// Update Default : 15Hz
				updateAccumulator += _dt ;
				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
					inputSystem.update() ;
					eventSystem.update() ;

					eventController.update() ;
					editorController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				if( renderAccumulator > DEFAULT_FRAMERATE )
				{
					//System.out.println( ( int )( 1.0f / renderAccumulator ) ) ;
					animationSystem.update( DEFAULT_FRAMERATE ) ;
					system.draw( DEFAULT_FRAMERATE ) ;
					renderAccumulator = 0.0f ;
				}
			}
		} ;
	}

	@Override
	protected void hookHandlerSystems()
	{
		super.hookHandlerSystems() ;
		eventSystem.addEventHandler( editorController ) ;
		system.addEventHandler( editorController ) ;
	}

	@Override
	protected void unhookHandlerSystems()
	{
		super.unhookHandlerSystems() ;
		eventSystem.removeEventHandler( editorController ) ;
		system.removeEventHandler( editorController ) ;
	}
	
	private void populateEntityFactory()
	{
		factory.addCreator( new EditorCreator() ) ;
	}
}