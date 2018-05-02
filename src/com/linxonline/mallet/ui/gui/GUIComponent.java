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
	private DrawDelegate delegate = null ;
	private World world = null ;
	private Camera camera = null ;

	private int layerOffset = 0 ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 length = new Vector3() ;

	private boolean visible = true ;

	private final Connect.Slot addDrawSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _parent )
		{
			visible = true ;
			if( delegate != null )
			{
				addDraws( delegate, world ) ;
			}
		}
	} ;

	private final Connect.Slot removeDrawSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _parent )
		{
			visible = false ;
			if( delegate != null )
			{
				removeDraws( delegate ) ;
			}
		}
	} ;

	private final Connect.Slot positionSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _parent )
		{
			position.setXYZ( _parent.getPosition() ) ;
		}
	} ;

	private final Connect.Slot offsetSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _parent )
		{
			offset.setXYZ( _parent.getOffset() ) ;
		}
	} ;

	private final Connect.Slot lengthSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _parent )
		{
			length.setXYZ( _parent.getLength() ) ;
		}
	} ;

	public GUIComponent( final UIElement _parent )
	{
		_parent.super() ;
		visible = _parent.isVisible() ;

		position.setXYZ( _parent.getPosition() ) ;
		offset.setXYZ( _parent.getOffset() ) ;
		length.setXYZ( _parent.getLength() ) ;

		UIElement.connect( _parent, _parent.elementShown(),    addDrawSlot() ) ;
		UIElement.connect( _parent, _parent.elementHidden(),   removeDrawSlot() ) ;
		UIElement.connect( _parent, _parent.positionChanged(), positionSlot() ) ;
		UIElement.connect( _parent, _parent.offsetChanged(),   offsetSlot() ) ;
		UIElement.connect( _parent, _parent.lengthChanged(),   lengthSlot() ) ;
	}
	
	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	public abstract void addDraws( final DrawDelegate _delegate, final World _world ) ;

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	public abstract void removeDraws( final DrawDelegate _delegate ) ;

	@Override
	public void passDrawDelegate( final DrawDelegate _delegate, final World _world )
	{
		delegate = _delegate ;
		world = _world ;

		super.passDrawDelegate( _delegate, _world ) ;
		if( visible == true )
		{
			addDraws( delegate, _world ) ;
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

	/**
		This slot is called when the parent element has been made 
		visible and is expected to display something to the screen.
	*/
	public Connect.Slot<UIElement> addDrawSlot()
	{
		return addDrawSlot ;
	}

	/**
		This slot is called when the parent element has been made 
		invisible and is expected to not display anything to the screen.
	*/
	public Connect.Slot<UIElement> removeDrawSlot()
	{
		return removeDrawSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		position and the GUI is expected to reflect that.
	*/
	public Connect.Slot<UIElement> positionSlot()
	{
		return positionSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		offset and the GUI is expected to reflect that.
	*/
	public Connect.Slot<UIElement> offsetSlot()
	{
		return offsetSlot ;
	}

	/**
		This slot is called when the parent element has changed 
		length and the GUI is expected to reflect that.
	*/
	public Connect.Slot<UIElement> lengthSlot()
	{
		return lengthSlot ;
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

		removeDraws( delegate ) ;
		delegate = null ;
		world = null ;
	}

	public DrawDelegate getDrawDelegate()
	{
		return delegate ;
	}

	public World getWorld()
	{
		return world ;
	}

	public static class Meta extends UIElement.MetaComponent
	{
		private final Connect connect = new Connect() ;

		public Meta() {}

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

		/**
			Remove all connections made to this packet.
			Should only be called by the packets owner.
		*/
		public void shutdown()
		{
			UIElement.disconnect( this ) ;
		}

		@Override
		public Connect getConnect()
		{
			return connect ;
		}
	}
}
