package com.linxonline.malleteditor.core ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.ui.gui.* ;

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

		final SingleEngageComponent engage = addComponent( new SingleEngageComponent( this ) ) ;
		final GUILineDraw line = addComponent( new GUILineDraw( this ) ) ;

		addComponent( new InputComponent( this )
		{
			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				if( engage.isEngaged() == true )
				{
					return InputEvent.Action.PROPAGATE ;
				}

				final UIWrapper parent = ( UIWrapper )getParent() ;
				sendEvent( new Event<UIWrapper>( "DISPLAY_META", parent ) ) ;

				line.setColour( MalletColour.blue() ) ;
				parent.makeDirty() ;
				return InputEvent.Action.CONSUME ;
			}
		} ) ;

		UIElement.connect( this, elementDisengaged(), new Connect.Slot<UIWrapper>()
		{
			@Override
			public void slot( final UIWrapper _parent )
			{
				line.setColour( MalletColour.white() ) ;
				_parent.makeDirty() ;
			}
		} ) ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate _delegate, final World _world, final Camera _camera )
	{
		super.passDrawDelegate( _delegate, _world, _camera ) ;
		element.passDrawDelegate( _delegate, _world, _camera ) ;
	}

	@Override
	public void engage()
	{
		super.engage() ;
		current = State.CHILD_ENGAGED ;
	}

	public void disengage()
	{
		super.disengage() ;
		if( children != null )
		{
			final int size = children.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIWrapper element = children.get( i ) ;
				if( element.isEngaged() == true )
				{
					element.disengage() ;
				}
			}
		}
	}

	@Override
	public void update( final float _dt, final List<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
		element.update( _dt, _events ) ;

		if( meta.supportsChildren() == true )
		{
			final int size = children.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIWrapper wrapper = children.get( i ) ;
				wrapper.update( _dt, _events ) ;
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

	public UIElement.Meta getMeta()
	{
		return meta ;
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

	private static class GUILineDraw extends GUIComponent
	{
		protected UI.Alignment drawAlignmentX = UI.Alignment.LEFT ;
		protected UI.Alignment drawAlignmentY = UI.Alignment.LEFT ;

		private MalletColour colour = MalletColour.white() ;

		protected Draw draw = null ;

		public GUILineDraw( final UIWrapper _parent )
		{
			super( UIFactory.createMeta( "GUILINEDRAW" ), _parent ) ;
			updateLength( _parent.getLength(), getLength() ) ;
			updateOffset( _parent.getOffset(), getOffset() ) ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawAlignmentX = ( _x == null ) ? UI.Alignment.LEFT : _x ;
			drawAlignmentY = ( _y == null ) ? UI.Alignment.LEFT : _y ;
		}

		public void setColour( final MalletColour _colour )
		{
			colour = ( _colour != null ) ? _colour : MalletColour.white() ;
			final Draw draw = getDraw() ;
			if( draw != null )
			{
				final Shape shape = DrawAssist.getDrawShape( getDraw() ) ;
				if( shape != null )
				{
					GUI.updateColour( shape, colour ) ;
				}
			}
		}

		@Override
		public void constructDraws()
		{
			draw = DrawAssist.createDraw( getPosition(),
										  getOffset(),
										  new Vector3(),
										  new Vector3( 1, 1, 1 ),
										  getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			DrawAssist.amendShape( draw, Shape.constructOutlinePlane( getLength(), colour ) ) ;

			final Program program = ProgramAssist.create( "SIMPLE_GEOMETRY" ) ;
			DrawAssist.attachProgram( draw, program ) ;

			super.constructDraws() ;
		}

		@Override
		public void addDraws( final DrawDelegate _delegate, final World _world )
		{
			if( draw != null )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}
		}

		@Override
		public void removeDraws( final DrawDelegate _delegate )
		{
			_delegate.removeDraw( draw ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			final UIElement parent = getParent() ;

			if( draw != null && parent.isVisible() == true )
			{
				updateLength( parent.getLength(), getLength() ) ;
				updateOffset( parent.getOffset(), getOffset() ) ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), getLength() ) ;
				DrawAssist.amendOrder( draw, getLayer() ) ;
				DrawAssist.forceUpdate( draw ) ;
			}
		}

		private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
		{
			_toUpdate.setXYZ( _length ) ;
		}

		private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
		{
			UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
			_toUpdate.add( _offset ) ;
		}

		public Draw getDraw()
		{
			return draw ;
		}

		public UI.Alignment getAlignmentX()
		{
			return drawAlignmentX ;
		}

		public UI.Alignment getAlignmentY()
		{
			return drawAlignmentY ;
		}

		public MalletColour getColour()
		{
			return colour ;
		}
	}

	public static class SingleEngageComponent extends InputComponent
	{
		private UIWrapper currentEngaged = null ;

		public SingleEngageComponent( final UIWrapper _parent )
		{
			super( _parent ) ;
			UIElement.connect( _parent, _parent.elementDisengaged(), new Connect.Slot<UIWrapper>()
			{
				@Override
				public void slot( final UIWrapper _wrapper )
				{
					// If the parent has been disengaged then it is 
					// safe to say that all children of the layout 
					// should also be disengaged.
					setEngaged( null ) ;
					disengageOthers( null, _wrapper.children ) ;
				}
			} ) ;
		}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			final UIWrapper parent = getParentWrapper() ;
			if( parent.children == null )
			{
				return InputEvent.Action.PROPAGATE ;
			}

			final UIWrapper current = getEngaged() ;
			if( current != null )
			{
				if( current.isVisible() == true && current.isDisabled() == false )
				{
					if( current.isIntersectInput( _input ) == true )
					{
						return passInput( current, _input ) ;
					}
				}
			}

			setEngaged( null ) ;
			disengageOthers( null, parent.children ) ;

			final int size = parent.children.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIWrapper element = parent.children.get( i ) ;
				if( element.isVisible() == true )
				{
					if( element.isIntersectInput( _input ) == true )
					{
						element.engage() ;
						setEngaged( element.isDisabled() ? null : element ) ;
						disengageOthers( getEngaged(), parent.children ) ;

						return passInput( element, _input ) ;
					}
				}
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchMove( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action keyPressed( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		@Override
		public InputEvent.Action scroll( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		@Override
		public InputEvent.Action keyReleased( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		private InputEvent.Action passInput( final UIWrapper _current, final InputEvent _input )
		{
			return ( _current != null ) ? _current.passInputEvent( _input ) : InputEvent.Action.PROPAGATE ;
		}

		public UIWrapper getParentWrapper()
		{
			return ( UIWrapper )getParent() ;
		}

		public void setEngaged( final UIWrapper _toEngage )
		{
			currentEngaged = _toEngage ;
		}

		public UIWrapper getEngaged()
		{
			return currentEngaged ;
		}
		
		public boolean isEngaged()
		{
			return getEngaged() != null ;
		}

		private static void disengageOthers( final UIWrapper _current, final List<UIWrapper> _others )
		{
			if( _others == null )
			{
				return ;
			}

			final int size = _others.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIWrapper wrapper = _others.get( i ) ;
				if( wrapper != _current &&
					wrapper.isEngaged() == true )
				{
					// Only disengage the elements that were previously
					// engaged and are not the currently engaged element.
					wrapper.disengage() ;
				}
			}
		}
	}
}