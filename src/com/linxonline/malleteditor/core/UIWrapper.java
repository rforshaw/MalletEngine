package com.linxonline.malleteditor.core ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class UIWrapper extends UIElement
{
	private final UIElement.Meta meta ;
	private final UIElement element ;	// Element that represents packet

	private final List<UIWrapper> children ;

	public UIWrapper( final UIElement.Meta _meta )
	{
		meta = _meta ;
		element = UIGenerator.create( _meta ) ;

		children = ( meta.supportsChildren() ) ? MalletList.<UIWrapper>newList() : null ;

		UIElement.connect( this, positionChanged(), new Connect.Slot<UIWrapper>()
		{
			private final Vector3 unit = new Vector3() ;
		
			@Override
			public void slot( final UIWrapper _parent )
			{
				_parent.getPosition( unit ) ;
				element.setPosition( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, offsetChanged(), new Connect.Slot<UIWrapper>()
		{
			private final Vector3 unit = new Vector3() ;
		
			@Override
			public void slot( final UIWrapper _parent )
			{
				_parent.getOffset( unit ) ;
				element.setOffset( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, lengthChanged(), new Connect.Slot<UIWrapper>()
		{
			private final Vector3 unit = new Vector3() ;
		
			@Override
			public void slot( final UIWrapper _parent )
			{
				_parent.getLength( unit ) ;
				element.setLength( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, layerChanged(), new Connect.Slot<UIWrapper>()
		{
			@Override
			public void slot( final UIWrapper _parent )
			{
				element.setLayer( _parent.getLayer() ) ;
			}
		} ) ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		super.passDrawDelegate( _delegate, _world, _camera ) ;
		element.passDrawDelegate( _delegate, _world, _camera ) ;
	}

	/**
		A UIPacket can only be inserted into a UIWrapper 
		if the packet that the UIWrapper represents supports 
		children - if it doesn't then false is returned.

		Passing in the intended location that the packet 
		should reside will potentially pass the packet up 
		through the children if the position resides within 
		an existing child.
	*/
	public boolean insertUIWrapper( final UIWrapper _child, final float _x, final float _y )
	{
		// We can only insert further packets if
		// the Wrapper contains a meta that supports
		// child elements - this should extend UILayout.
		if( meta.supportsChildren() == false ||
		    intersectPoint( _x, _y ) == false )
		{
			return false ;
		}

		// If the meta does support children then 
		// attempt to insert the meta into any of 
		// the currently added children... It will 
		// return true if the meta is appropriate.
		final int size = children.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIWrapper wrapper = children.get( i ) ;
			if( wrapper.insertUIWrapper( _child, _x, _y ) == true )
			{
				return true ;
			}
		}

		// If none of the children want the packet then 
		// add it to the parent instead.
		// If it's capable of adding children then we 
		// assume the element implements IChildren.
		// TODO: This will need to be modified to determine whether 
		// _child should be inserted between other child elements.
		final IChildren layout = ( IChildren )element ;
		final UIWrapper child = layout.addElement( _child ) ;
		children.add( child ) ;
		return true ;
	}

	/**
		A UIPacket can only be inserted into a UIWrapper 
		if the packet that the UIWrapper represents supports 
		children - if it doesn't then false is returned.

		It assumes that the packet is to be directly added as 
		a child to the current UIWrapper, the packet will 
		be added at the end of the children list.
	*/
	public boolean insertUIWrapper( final UIWrapper _child )
	{
		// We can only insert further packets if
		// the Wrapper contains a packet that supports
		// child elements - this should extend UILayout.
		if( meta.supportsChildren() == false )
		{
			return false ;
		}

		// If it's capable of adding children then we 
		// assume the element implements IChildren.
		final IChildren layout = ( IChildren )element ;
		final UIWrapper child = layout.addElement( _child ) ;
		children.add( child ) ;
		return true ;
	}

	/**
		Can only remove a wrapper if the wrapper is a 
		child.
	*/
	public boolean removeUIWrapper( final UIWrapper _child )
	{
		if( _child == null || this == _child )
		{
			return false ;
		}

		final int size = children.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIWrapper child = children.get( i ) ;
			if( child == _child )
			{
				children.remove( i ) ;
				child.destroy() ;
				return true ;
			}
		}

		return false ;
	}

	@Override
	public void update( final float _dt, final List<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
		element.update( _dt, _events ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		element.shutdown() ;
	}

	@Override
	public void clear()
	{
		super.clear() ;
		element.clear() ;
	}
}
