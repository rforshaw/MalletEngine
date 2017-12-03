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
	private final UIPacket packet ;
	private final UIElement element ;	// Element that represents packet

	private final List<UIWrapper> children ;

	public UIWrapper( final UIPacket _packet )
	{
		packet = _packet ;
		element = packet.createElement() ;
		wrapPacket( packet, element ) ;

		children = ( packet.supportsChildren() ) ? MalletList.<UIWrapper>newList() : null ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		super.passDrawDelegate( _delegate, _world, _camera ) ;
		element.passDrawDelegate( _delegate, _world, _camera ) ;

		if( packet.supportsChildren() == true )
		{
			final int size = children.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIWrapper wrapper = children.get( i ) ;
				wrapper.passDrawDelegate( _delegate, _world, _camera ) ;
			}
		}
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
		// the Wrapper contains a packet that supports
		// child elements - this should extend UILayout.
		if( packet.supportsChildren() == false ||
		    intersectPoint( _x, _y ) == false )
		{
			return false ;
		}

		// If the packet does support children then 
		// attempt to insert the packet into any of 
		// the currently added children... It will 
		// return true if the packet is appropriate.
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
		if( packet.supportsChildren() == false )
		{
			return false ;
		}

		// If it's capable of adding children then we 
		// assume the element implements IChildren.
		final IChildren layout = ( IChildren )element ;
		final UIWrapper child = layout.addElement( _child ) ;
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

	protected UIPacket getPacket()
	{
		return packet ;
	}

	@Override
	public void update( final float _dt, final List<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
		element.update( _dt, _events ) ;

		if( packet.supportsChildren() == true )
		{
			for( final UIWrapper child : children )
			{
				child.update( _dt, _events ) ;
			}
		}
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		packet.shutdown() ;
		element.shutdown() ;

		for( final UIWrapper child : children )
		{
			child.shutdown() ;
		}
	}

	@Override
	public void clear()
	{
		super.clear() ;
		element.clear() ;

		for( final UIWrapper child : children )
		{
			child.clear() ;
		}
	}

	/**
		Whenever anything modifies the state of the UIPacket 
		it should update the UIElement that is meant to 
		reflect it.
		
		The UIWrapper is the ultimate arbiter as it ultimately 
		owns the packet. 
	*/
	private static void wrapPacket( final UIPacket _packet, final UIElement _element )
	{
		UIPacket.connect( _packet, _packet.positionChanged(), new Connect.Slot<UIPacket>()
		{
			final Vector3 temp = new Vector3() ;

			public void slot( final UIPacket _packet )
			{
				_packet.getPosition( temp ) ;
				_element.setPosition( temp.x, temp.y, temp.z ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.offsetChanged(), new Connect.Slot<UIPacket>()
		{
			final Vector3 temp = new Vector3() ;

			public void slot( final UIPacket _packet )
			{
				_packet.getOffset( temp ) ;
				_element.setOffset( temp.x, temp.y, temp.z ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.marginChanged(), new Connect.Slot<UIPacket>()
		{
			final Vector3 temp = new Vector3() ;

			public void slot( final UIPacket _packet )
			{
				_packet.getMargin( temp ) ;
				_element.setMargin( temp.x, temp.y, temp.z ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.minimumLengthChanged(), new Connect.Slot<UIPacket>()
		{
			final Vector3 temp = new Vector3() ;

			public void slot( final UIPacket _packet )
			{
				_packet.getMinimumLength( temp ) ;
				_element.setMinimumLength( temp.x, temp.y, temp.z ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.maximumLengthChanged(), new Connect.Slot<UIPacket>()
		{
			final Vector3 temp = new Vector3() ;

			public void slot( final UIPacket _packet )
			{
				_packet.getMaximumLength( temp ) ;
				_element.setMaximumLength( temp.x, temp.y, temp.z ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.layerChanged(), new Connect.Slot<UIPacket>()
		{
			public void slot( final UIPacket _packet )
			{
				_element.setLayer( _packet.getLayer() ) ;
			}
		} ) ;

		UIPacket.connect( _packet, _packet.disableChanged(), new Connect.Slot<UIPacket>()
		{
			public void slot( final UIPacket _packet )
			{
				if( _packet.getDisableFlag() )
				{
					_element.disable() ;
				}
				else
				{
					_element.enable() ;
				}
			}
		} ) ;

		UIPacket.connect( _packet, _packet.visibleChanged(), new Connect.Slot<UIPacket>()
		{
			public void slot( final UIPacket _packet )
			{
				_element.setVisible( _packet.getVisibleFlag() ) ;
			}
		} ) ;
	}
}
