package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.save.Save ;
import com.linxonline.mallet.io.save.Reference ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.io.serialisation.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.id.ID ;

/*==============================================================*/
// Component - Root class for all Componets used by Entity.	  //
/*==============================================================*/

public abstract class Component implements SerialisableForm
{
	protected @Save final EventController componentEvents = new EventController() ;		// Handles events from parent
	protected @Reference Entity parent = null ;											// Owner of this component
	protected @Save final ID id ;														// Name and Group Name

	public Component()
	{
		id = new ID( "NONE", "NONE" ) ;
	}

	/**
		Engine specified codes for nameID and groupID.
		Not guaranteed to be unique over 16 characters.
	*/
	public Component( final String _name, final String _group )
	{
		id = new ID( _name, _group ) ;
	}

	/**
		Developer specified unique codes for nameID and groupID.
	*/
	public Component( final String _name, final int _nameID,
					  final String _group, final int _groupID )
	{
		id = new ID( _name, _nameID, _group, _groupID ) ;
	}

	public void setParent( final Entity _parent )
	{
		parent = _parent ;
	}

	/**
		If overriding, you must call super.update( _dt ), 
		else componentEvents will not be updated.
	**/
	public void update( final float _dt )
	{
		componentEvents.update() ;
	}

	public final boolean isName( final String _name )
	{
		return id.isName( _name ) ;
	}

	public final boolean isNameID( final int _nameID )
	{
		return id.isNameID( _nameID ) ;
	}

	public final boolean isGroup( final String _group )
	{
		return id.isGroup( _group ) ;
	}

	public final boolean isGroupID( final int _groupID )
	{
		return id.isGroupID( _groupID ) ;
	}

	/**
		Create events and add them to _events.
		The events will be passed to the Event System
		of the Game State. Use an Event to register the 
		component or variables it holds to external systems.
		Can also be used for any other events that may need 
		to be passed when the component is added to the Entity System. 
	*/
	public void passInitialEvents( final ArrayList<Event<?>> _events ) {}

	/**
		Create events and add them to _events.
		The events will be passed to the Event System
		of the Game State. Use an Event to cleanup the 
		component or variables it holds to external systems.
		Can also be used for any other events that may need 
		to be passed when the component is removed from the Entity System. 
	*/
	public void passFinalEvents( final ArrayList<Event<?>> _events ) {}

	/**
		The parent can be flagged for destruction at anytime, a 
		component could be in an unstable state, for example 
		waiting for the id of a render request.
		readyToDestroy allows the component to inform the parent 
		it is safe to destroy itself. When overriding don't call super.
	*/
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		_callback.ready( this ) ;
	}

	/**
		Return the internal Event Controller for this component.
	*/
	public EventController getComponentEventController()
	{
		return componentEvents ;
	}

	/**
		Used to write out the byte stream of the Component object
	**/
	public boolean writeObject( final SerialiseOutput _output )
	{
		_output.writeString( id.name ) ;
		_output.writeString( id.group ) ;
		_output.writeInt( id.nameID ) ;
		_output.writeInt( id.groupID ) ;
		return true ;
	}

	/**
		Used to read an Component byte stream and reconstruct it.
		It does NOT build the Component from scratch.
	**/
	public boolean readObject( final SerialiseInput _input )
	{
		id.name = _input.readString() ;
		id.group = _input.readString() ;
		id.nameID = _input.readInt() ;
		id.groupID = _input.readInt() ;
		return true ;
	}

	public static interface ReadyCallback<T extends Component>
	{
		public void ready( final T _component ) ;
	}
}
