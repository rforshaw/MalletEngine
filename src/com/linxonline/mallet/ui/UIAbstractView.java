package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Set ;

import com.linxonline.mallet.ui.gui.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/**
	Provides a visual representation of data stored 
	within a UIAbstractModel.
	You can have multiple UIAbstractViews reference 
	one UIAbstractModel.
*/
public class UIAbstractView extends UIElement
{
	private ItemDelegate<?> defaultItemDelegate = createDefaultItemDelegate() ;
	private ItemDelegate<?>[] columnItemDelegate = new ItemDelegate<?>[1] ;
	private ItemDelegate<?>[] rowItemDelegate = new ItemDelegate<?>[1] ;

	private final UIList list = new UIList( ILayout.Type.VERTICAL ) ;

	private final IAbstractModel view = new UIAbstractModel() ;
	private IAbstractModel model = UIAbstractView.createEmptyModel() ;

	private final Vector3 cellLength = new Vector3( 1.0f, 1.0f, 0.0f ) ;

	public UIAbstractView()
	{
		super() ;
		initViewConnections() ;
	}

	private void initViewConnections()
	{
		/**
			Engaging a UILayout is somewhat redundant.
			You will most likely want to engage a child element 
			owned by the layout.
		*/
		UIElement.connect( this, elementEngaged(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.engage() ;
			}
		} ) ;

		UIElement.connect( this, elementDisengaged(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.disengage() ;
			}
		} ) ;
	
		/**
			Set the layer that the visual elements are 
			expected to be placed on.
			
			This also applies to the layouts children.
			Causes the elements to be flagged as dirty.
		*/
		UIElement.connect( this, layerChanged(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.setLayer( getLayer() + 1 ) ;
			}
		} ) ;

		UIElement.connect( this, elementShown(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.setVisible( true ) ;
			}
		} ) ;

		UIElement.connect( this, elementHidden(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.setVisible( false ) ;
			}
		} ) ;

		/**
			Cleanup any resources, handlers that the listeners 
			may have acquired.

			Will also call shutdown on all children. Call clear 
			if you wish to also remove all children from layout.
		*/
		UIElement.connect( this, elementShutdown(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.shutdown() ;
			}
		} ) ;

		/**
			Clear out each of the systems.
			Remove all slots connected to signals.
			Remove all listeners - note call shutdown if they have 
			any resources attached.
			Remove any events that may be in the event stream.
		*/
		UIElement.connect( this, elementClear(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.clear() ;
			}
		} ) ;

		/**
			Reset the UILayout as if it has just been constructed.
			This does not remove listeners, connections or children.

			Call reset on all children.
		*/
		UIElement.connect( this, elementReset(), new Connect.Slot<UIAbstractView>()
		{
			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.list.reset() ;
			}
		} ) ;

		UIElement.connect( this, positionChanged(), new Connect.Slot<UIAbstractView>()
		{
			private final Vector3 unit = new Vector3() ;

			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.getPosition( unit ) ;
				_this.list.setPosition( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, offsetChanged(), new Connect.Slot<UIAbstractView>()
		{
			private final Vector3 unit = new Vector3() ;

			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.getOffset( unit ) ;
				_this.list.setOffset( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, lengthChanged(), new Connect.Slot<UIAbstractView>()
		{
			private final Vector3 unit = new Vector3() ;

			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.getLength( unit ) ;
				_this.list.setLength( unit.x, unit.y, unit.z ) ;
			}
		} ) ;

		UIElement.connect( this, marginChanged(), new Connect.Slot<UIAbstractView>()
		{
			private final Vector3 unit = new Vector3() ;

			@Override
			public void slot( final UIAbstractView _this )
			{
				_this.getMargin( unit ) ;
				_this.list.setMargin( unit.x, unit.y, unit.z ) ;
			}
		} ) ;
	}

	public ItemDelegate setDefaultItemDelegate( final ItemDelegate<?> _delegate )
	{
		defaultItemDelegate = ( _delegate != null ) ? _delegate : createDefaultItemDelegate() ;
		return defaultItemDelegate ;
	}

	public ItemDelegate setColumnItemDelegate( final int _column, final ItemDelegate<?> _delegate )
	{
		columnItemDelegate = setItemDelegate( _column, columnItemDelegate, _delegate ) ;
		return _delegate ;
	}

	public ItemDelegate setRowItemDelegate( final int _row, final ItemDelegate<?> _delegate )
	{
		rowItemDelegate = setItemDelegate( _row, rowItemDelegate, _delegate ) ;
		return _delegate ;
	}

	private ItemDelegate[] setItemDelegate( final int _index, final ItemDelegate<?>[] _delegates, final ItemDelegate<?> _delegate )
	{
		final int size = _delegates.length ;
		if( _index < size )
		{
			_delegates[_index] = _delegate ;
			return _delegates ;
		}

		final ItemDelegate<?>[] delegates = new ItemDelegate<?>[_index + 1] ;
		System.arraycopy( _delegates, 0, delegates, 0, delegates.length ) ;
		_delegates[_index] = _delegate ;
		return delegates ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate _delegate, final World _world, final Camera _camera )
	{
		super.passDrawDelegate( _delegate, _world, _camera ) ;
		list.passDrawDelegate( _delegate, _world, _camera ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
		{
			return InputEvent.Action.CONSUME ;
		}

		return list.passInputEvent( _event ) ;
	}

	/**
		Refreshing a UIAbstractView will most likely 
		result in all child elements requiring to be 
		refreshed.

		Flag the child elements as dirty and during 
		the next update cycle they will be refreshed.
	*/
	@Override
	protected void refresh()
	{
		super.refresh() ;
		list.refresh() ;
	}

	/**
		Check to see if the InputEvent intersects with 
		either the UIAbstractView or one of its children.

		We check the children as there is a chance that 
		the child is beyond the UIAbstractView boundaries, for 
		example a dropdown menu.
	*/
	@Override
	public boolean isIntersectInput( final InputEvent _event )
	{
		if( super.isIntersectInput( _event ) == true )
		{
			return true ;
		}

		// There is a chance that a layout has children 
		// that go beyond the layouts boundaries.
		// For example UIMenu dropdown.
		return list.isIntersectInput( _event ) ;
	}

	@Override
	public void update( final float _dt, final List<Event<?>> _events )
	{
		final boolean dirty = isDirty() ;
		super.update( _dt, _events ) ;
		if( dirty == true )
		{
			updateView( model.root(), this, _dt, _events ) ;
		}

		list.update( _dt, _events ) ;
	}

	/**
		Assuming the passed in _node is a parent update 
		all child elements to fit within the passed in _parent element.

		The node passed in may not have any children, if this is 
		the case then row/column count will return 0.
	*/
	private void updateView( final UIModelIndex _node,
							 final UIElement _parent,
							 final float _dt,
							 final List<Event<?>> _events )
	{
		final int rowCount = model.rowCount( _node ) ;
		final int columnCount = model.columnCount( _node ) ;

		final UIRatio ratio = getRatio() ;
		final Vector3 length = getLength() ;

		final float width = length.x / ( ( columnCount == 0 ) ? 1 : columnCount ) ;
		cellLength.x = ratio.toUnitX( width ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _node, i, j ) ;
				final UIElement element = getCellElement( index, getItemDelegate( index ) ) ;
				updateView( index, element, _dt, _events ) ;
			}
		}
	}

	private void removeView( final UIModelIndex _node )
	{
		final int rowCount = model.rowCount( _node ) ;
		final int columnCount = model.columnCount( _node ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _node, i, j ) ;
				removeView( index ) ;

				removeCellElement( index, getItemDelegate( index ) ) ;
			}
		}
	}

	public void setModel( final IAbstractModel _model )
	{
		removeView( model.root() ) ;
		model = ( _model != null ) ? _model : UIAbstractView.createEmptyModel() ;
		makeDirty() ;
	}

	public IAbstractModel getModel()
	{
		return model ;
	}

	public static <T extends UIAbstractView> T applyMeta( final UIAbstractView.Meta _meta, final T _view )
	{
		return UIElement.applyMeta( _meta, _view ) ;
	}

	/**
		Return a cell that is associated with the passed in index.
		If no cell exist construct one and apply display data from 
		the model to it.
	*/
	private UIElement getCellElement( final UIModelIndex _index, final ItemDelegate _delegate )
	{
		if( view.exists( _index ) == false )
		{
			view.createData( _index.getParent(), _index.getRow() + 1, _index.getColumn() + 1 ) ;
		}

		IVariant variant = view.getData( _index, IAbstractModel.Role.Display ) ;
		if( variant == null )
		{
			final UIElement cell = _delegate.createItem( this ) ;
			//cell.setPosition( _index.getColumn(), _index.getRow(), 0 ) ;
			//cell.setLength( cellLength.x, cellLength.y, cellLength.z ) ;

			variant = new UIVariant( "CELL", cell ) ;
			view.setData( _index, variant, IAbstractModel.Role.Display ) ;
			list.addElement( cell ) ;
			list.makeDirty() ;
			System.out.println( "Add cell to list" ) ;
		}

		final UIElement cell = variant.toObject( UIElement.class )  ;
		_delegate.setItemData( cell, model, _index ) ;
		return cell ;
	}

	/**
		Remove a cell that is associated with the passed in index.
	*/
	private void removeCellElement( final UIModelIndex _index, final ItemDelegate _delegate )
	{
		if( view.exists( _index ) == false )
		{
			// The view has not yet constructed an item for this index
			return ;
		}

		final IVariant variant = view.getData( _index, IAbstractModel.Role.Display ) ;
		if( variant == null )
		{
			return ;
		}

		view.removeData( _index ) ;		// Remove any variants assigned to that index
		
		final UIElement cell = variant.toObject( UIElement.class )  ;
		//_delegate.destroyItem( cell ) ;
		list.removeElement( cell ) ;
	}

	private ItemDelegate<?> getItemDelegate( final UIModelIndex _index )
	{
		final int row = _index.getRow() ;
		if( row < rowItemDelegate.length )
		{
			if( rowItemDelegate[row] != null )
			{
				return rowItemDelegate[row] ;
			}
		}

		final int column = _index.getRow() ;
		if( column < columnItemDelegate.length )
		{
			if( columnItemDelegate[column] != null )
			{
				return columnItemDelegate[column] ;
			}
		}

		return defaultItemDelegate ;
	}

	/**
		When an abstract view has been initialised but has yet 
		to be given a model to display it will use this stub 
		implementation instead.
	*/
	private static IAbstractModel createEmptyModel()
	{
		return new IAbstractModel()
		{
			public UIModelIndex root()
			{
				return new UIModelIndex() ;
			}

			public int rowCount( final UIModelIndex _parent )
			{
				return 0 ;
			}

			public int columnCount( final UIModelIndex _parent )
			{
				return 0 ;
			}

			public boolean createData( final UIModelIndex _parent, final int _row, final int _column )
			{
				return false ;
			}

			public void setData( final UIModelIndex _index, final IVariant _variant, final Role _role ) {}

			public IVariant getData( final UIModelIndex _index, final Role _role )
			{
				return null ;
			}

			public boolean exists( final UIModelIndex _index )
			{
				return false ;
			}

			public void removeData( final UIModelIndex _index ) {}

			public Set<ItemFlags> getDataFlags( final UIModelIndex _index, final Set<ItemFlags> _flags )
			{
				return null ;
			}

			public boolean hasChildren( final UIModelIndex _parent )
			{
				return false ;
			}
		} ;
	}

	private static ItemDelegate createDefaultItemDelegate()
	{
		return new ItemDelegate<UIElement>()
		{
			@Override
			public UIElement createItem( final UIAbstractView _parent )
			{
				final UILayout.Meta metaLayout = new UILayout.Meta() ;
				metaLayout.setType( ILayout.Type.HORIZONTAL ) ;

				final UILayout layout = UIGenerator.<UILayout>create( metaLayout ) ;

				{
					final UIButton.Meta meta = new UIButton.Meta() ;
					final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
					edge.setSheet( "base/textures/edge_button.png" ) ;

					final GUIText.Meta text = meta.addComponent( new GUIText.Meta() ) ;
					text.setGroup( "ENGINE" ) ;
					text.setName( "TITLE" ) ;
					text.setText( "Test" ) ;

					layout.addElement( UIGenerator.<UIButton>create( meta ) ) ;
				}

				{
					final UITextField.Meta meta = new UITextField.Meta() ;
					final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
					edge.setSheet( "base/textures/edge_button.png" ) ;

					final GUIEditText.Meta text = meta.addComponent( new GUIEditText.Meta() ) ;
					text.setAlignment( UI.Alignment.LEFT, UI.Alignment.CENTRE ) ;
					text.setGroup( "ENGINE" ) ;
					text.setName( "VALUE" ) ;
					text.setText( "Test" ) ;

					layout.addElement( UIGenerator.<UITextField>create( meta ) ) ;
				}

				return layout ;
			}

			@Override
			public void destroyItem( final UIElement _item )
			{
				_item.shutdown() ;
				_item.clear() ;
			}

			@Override
			public void setItemData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index )
			{
				final IVariant variant = _model.getData( _index, IAbstractModel.Role.User ) ;

				final UILayout layout = ( UILayout )_item ;
				{
					final UIButton title = ( UIButton )layout.getElements().get( 0 ) ;
					final GUIText gui = title.getComponent( "ENGINE", "TITLE", GUIText.class ) ;
					final StringBuilder text = gui.getText() ;
					text.setLength( 0 ) ;
					text.append( variant.getName() ) ;
				}

				{
					final UITextField value = ( UITextField )layout.getElements().get( 1 ) ;
					final StringBuilder text = value.getText() ;
					text.setLength( 0 ) ;
					text.append( variant.toString() ) ;
				}
			}

			@Override
			public void setModelData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index ) {}
		} ;
	}

	public static class Meta extends UIElement.Meta
	{
		public Meta()
		{
			super() ;
		}

		@Override
		public String getElementType()
		{
			return "UIABSTRACTVIEW" ;
		}
	}

	public interface ItemDelegate<T extends UIElement>
	{
		/**
			Create the item that is to be used within the cell.
		*/
		public T createItem( final UIAbstractView _parent ) ;

		/**
			Clean up any resources that may have been used by this cell.
		*/
		public void destroyItem( final T _item ) ;

		/**
			Apply any initial data to the item.
		*/
		public void setItemData( final T _item, final IAbstractModel _model, final UIModelIndex _index ) ;

		/**
			Update the model based on the modifications by the item.
		*/
		public void setModelData( final T _item, final IAbstractModel _model, final UIModelIndex _index ) ;
	}
}
