package com.linxonline.mallet.entity ;

import com.linxonline.mallet.io.serialisation.* ;
import com.linxonline.mallet.event.* ;

/*==============================================================*/
// Component - Root class for all Componets used by Entity.	  //
/*==============================================================*/

public abstract class Component implements SerialisableForm
{
	protected final EventController componentEvents = new EventController() ;	// Handles events from parent
	protected Entity parent = null ;											// Owner of this component
	private String name ;														// Componets name, isn't unique
	private String group ;														// Componets group name, 

	private int nameID = -1 ;
	private int groupID = -1 ;
	
	public Component()
	{
		name = "NONE" ;
		group = "NONE" ;

		nameID = name.hashCode() ;
		groupID = group.hashCode() ;
	}

	public Component( final String _name, final String _group )
	{
		name = _name ;
		group = _group ;

		nameID = name.hashCode() ;
		groupID = group.hashCode() ;
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
		return name.equals( _name ) ;
	}

	public final boolean isNameID( final int _nameID )
	{
		return nameID == _nameID ;
	}

	public final boolean isGroup( final String _group )
	{
		return group.equals( _group ) ;
	}

	public final boolean isGroupID( final int _groupID )
	{
		return groupID == _groupID ;
	}

	public EventController getComponentEventController()
	{
		return componentEvents ;
	}
	
	/**
		Used to write out the byte stream of the Component object
	**/
	public boolean writeObject( final SerialiseOutput _output )
	{
		_output.writeString( name ) ;
		_output.writeString( group ) ;
		_output.writeInt( nameID ) ;
		_output.writeInt( groupID ) ;
		return true ;
	}

	/**
		Used to read an Component byte stream and reconstruct it.
		It does NOT build the Component from scratch.
	**/
	public boolean readObject( final SerialiseInput _input )
	{
		name = _input.readString() ;
		group = _input.readString() ;
		nameID = _input.readInt() ;
		groupID = _input.readInt() ;
		return true ;
	}
}
