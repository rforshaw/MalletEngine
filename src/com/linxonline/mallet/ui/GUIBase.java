package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;

/**
	Makes a request to receive a DrawDelegate from the 
	active Rendering System. addDraws() will be called 
	once delegate has been provided and the parent is 
	visible, if the parent is invisible removeDraws is 
	called instead.
*/
public abstract class GUIBase<T extends UIElement> extends ABase<T>
{
	private DrawDelegate<World, Draw> delegate = null ;
	private World world = null ;
	private Camera camera = null ;

	private int layerOffset = 0 ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 length = new Vector3() ;

	private boolean visible = true ;

	private final Connect.Slot addDrawSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _parent )
		{
			visible = true ;
			if( delegate != null )
			{
				addDraws( delegate, world ) ;
			}
		}
	} ;

	private final Connect.Slot removeDrawSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _parent )
		{
			visible = false ;
			if( delegate != null )
			{
				removeDraws( delegate ) ;
			}
		}
	} ;

	private final Connect.Slot positionSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _parent )
		{
			position.setXYZ( _parent.getPosition() ) ;
		}
	} ;

	private final Connect.Slot offsetSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _parent )
		{
			offset.setXYZ( _parent.getOffset() ) ;
		}
	} ;

	private final Connect.Slot lengthSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _parent )
		{
			length.setXYZ( _parent.getLength() ) ;
		}
	} ;
	
	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	public abstract void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world ) ;

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	public abstract void removeDraws( final DrawDelegate<World, Draw> _delegate ) ;

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
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

	@Override
	public void setParent( final T _parent )
	{
		super.setParent( _parent ) ;
		visible = _parent.isVisible() ;

		position.setXYZ( _parent.getPosition() ) ;
		offset.setXYZ( _parent.getOffset() ) ;
		length.setXYZ( _parent.getLength() ) ;

		UIElement.connect( _parent, _parent.elementShown(),    addDrawSlot ) ;
		UIElement.connect( _parent, _parent.elementHidden(),   removeDrawSlot ) ;
		UIElement.connect( _parent, _parent.positionChanged(), positionSlot ) ;
		UIElement.connect( _parent, _parent.offsetChanged(),   offsetSlot ) ;
		UIElement.connect( _parent, _parent.lengthChanged(),   lengthSlot ) ;
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
		final T parent = getParent() ;
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

	@Override
	public void shutdown()
	{
		final T parent = getParent() ;
		UIElement.disconnect( parent, parent.elementShown(),    addDrawSlot ) ;
		UIElement.disconnect( parent, parent.elementHidden(),   removeDrawSlot ) ;
		UIElement.disconnect( parent, parent.positionChanged(), positionSlot ) ;
		UIElement.disconnect( parent, parent.offsetChanged(),   offsetSlot ) ;
		UIElement.disconnect( parent, parent.lengthChanged(),   lengthSlot ) ;
	
		delegate = null ;
		world = null ;
	}

	public DrawDelegate<World, Draw> getDrawDelegate()
	{
		return delegate ;
	}

	public World getWorld()
	{
		return world ;
	}
}
