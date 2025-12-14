package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;

/**
	Makes a request to receive a DrawDelegate from the 
	active Rendering System. addDraws() will be called 
	once delegate has been provided and the parent is 
	visible, if the parent is invisible removeDraws is 
	called instead.

	The slots defined in this class can be overridden and 
	custom logic can be implemented if required.

	Ensure that you initialise your slot upfront and return 
	it in the overridden function, do not return a new slot 
	on each function call - else the slots will not be removed 
	on shutdown.
*/
public abstract class GUIComponent extends UIElement.Component
{
	private World world = null ;
	private Camera camera = null ;

	private int layerOffset = 0 ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 length = new Vector3() ;
	private final Vector3 margin = new Vector3() ;

	private boolean visible = true ;

	private final Connect.Slot<UIElement> addDrawSlot = ( final UIElement _parent ) ->
	{
		visible = true ;
		addDraws( world ) ;
	} ;

	private final Connect.Slot<UIElement> removeDrawSlot = ( final UIElement _parent ) ->
	{
		visible = false ;
		removeDraws() ;
	} ;

	private final Connect.Slot<UIElement> positionSlot = ( final UIElement _parent ) ->
	{
		position.setXYZ( _parent.getPosition() ) ;
	} ;

	private final Connect.Slot<UIElement> offsetSlot = ( final UIElement _parent ) ->
	{
		offset.setXYZ( _parent.getOffset() ) ;
	} ;

	private final Connect.Slot<UIElement> lengthSlot = ( final UIElement _parent ) ->
	{
		length.setXYZ( _parent.getLength() ) ;
	} ;

	private final Connect.Slot<UIElement> marginSlot = ( final UIElement _parent ) ->
	{
		margin.setXYZ( _parent.getMargin() ) ;
	} ;

	private final Connect.Slot<UIElement> layerSlot = ( final UIElement _parent ) ->
	{
		layerUpdated( getLayer() ) ;
	} ;

	public GUIComponent( final UIElement.MetaComponent _meta, final UIElement _parent )
	{
		_parent.super( _meta ) ;
		visible = _parent.isVisible() ;

		position.setXYZ( _parent.getPosition() ) ;
		offset.setXYZ( _parent.getOffset() ) ;
		length.setXYZ( _parent.getLength() ) ;
		margin.setXYZ( _parent.getMargin() ) ;

		UIElement.connect( _parent, _parent.elementShown(),    addDrawSlot() ) ;
		UIElement.connect( _parent, _parent.elementHidden(),   removeDrawSlot() ) ;
		UIElement.connect( _parent, _parent.positionChanged(), positionSlot() ) ;
		UIElement.connect( _parent, _parent.offsetChanged(),   offsetSlot() ) ;
		UIElement.connect( _parent, _parent.lengthChanged(),   lengthSlot() ) ;
		UIElement.connect( _parent, _parent.marginChanged(),   marginSlot() ) ;
	}

	/**
		Called when component receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	public abstract void addDraws( final World _world ) ;

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	public abstract void removeDraws() ;

	public abstract void layerUpdated( int _layer ) ;
	
	@Override
	public void setWorld( final World _world )
	{
		world = _world ;

		super.setWorld( _world ) ;
		if( visible == true )
		{
			addDraws( _world ) ;
		}
	}

	public void setLayerOffset( final int _layer )
	{
		layerOffset = _layer ;
	}

	/**
		Called the next time the parent element is updated 
		and the element is flagged as dirty.

		It's very likely that a listener will want to track 
		changes made to the state of an element but will only 
		want to refresh the Draw state once.

		This allows multiple element state changes to be 
		bundled together when updating Draw state.
	*/
	@Override
	public void refresh() {}

	/**
		Return the a layer that any draw objects 
		constructed are expected to use.
	*/
	public int getLayer()
	{
		final UIElement parent = getParent() ;
		final int layer = ( parent != null ) ? parent.getLayer() : 0 ;
		return layer + layerOffset ;
	}

	public Vector3 getPosition()
	{
		return position ;
	}

	public Vector3 getOffset()
	{
		return offset ;
	}

	public Vector3 getLength()
	{
		return length ;
	}

	public Vector3 getMargin()
	{
		return margin ;
	}

	/**
		This slot is called when the parent element has been made 
		visible and is expected to display something to the screen.
	*/
	public final Connect.Slot<UIElement> addDrawSlot()
	{
		return addDrawSlot ;
	}

	/**
		This slot is called when the parent element has been made 
		invisible and is expected to not display anything to the screen.
	*/
	public final Connect.Slot<UIElement> removeDrawSlot()
	{
		return removeDrawSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		position and the GUI is expected to reflect that.
	*/
	public final Connect.Slot<UIElement> positionSlot()
	{
		return positionSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		offset and the GUI is expected to reflect that.
	*/
	public final Connect.Slot<UIElement> offsetSlot()
	{
		return offsetSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		length and the GUI is expected to reflect that.
	*/
	public final Connect.Slot<UIElement> lengthSlot()
	{
		return lengthSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		margin and the GUI is expected to reflect that.
	*/
	public final Connect.Slot<UIElement> marginSlot()
	{
		return marginSlot ;
	}

	@Override
	public void shutdown()
	{
		final UIElement parent = getParent() ;
		UIElement.disconnect( parent, parent.elementShown(),    addDrawSlot() ) ;
		UIElement.disconnect( parent, parent.elementHidden(),   removeDrawSlot() ) ;
		UIElement.disconnect( parent, parent.positionChanged(), positionSlot() ) ;
		UIElement.disconnect( parent, parent.offsetChanged(),   offsetSlot() ) ;
		UIElement.disconnect( parent, parent.lengthChanged(),   lengthSlot() ) ;

		removeDraws() ;

		world = null ;
	}

	public World getWorld()
	{
		return world ;
	}

	public static class Meta extends UIElement.MetaComponent
	{
		/**
			Base type is used to figure out what GUIGenerator is 
			required to create the correct GUI listener.
			When extending any Meta object override this function.
			If you do not override this then it will fallback to the 
			parent base-type.
		*/
		@Override
		public String getType()
		{
			return "UIELEMENT_GUIBASE" ;
		}
	}
}
