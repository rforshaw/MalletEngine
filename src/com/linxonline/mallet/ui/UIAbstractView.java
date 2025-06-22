package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Set ;

import com.linxonline.mallet.util.settings.* ;
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
	private final static ItemDelegate FALLBACK_ITEM_DELEGATE = createDefaultItemDelegate() ;

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
	public void setWorldAndCamera( final World _world, final Camera _camera )
	{
		super.setWorldAndCamera( _world, _camera ) ;
		list.setWorldAndCamera( _world, _camera ) ;
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
		if( super.isIntersectInput( _event ) )
		{
			return true ;
		}

		// There is a chance that a layout has children 
		// that go beyond the layouts boundaries.
		// For example UIMenu dropdown.
		return list.isIntersectInput( _event ) ;
	}

	@Override
	public void update( final float _dt )
	{
		final boolean dirty = isDirty() ;
		super.update( _dt ) ;
		if( dirty )
		{
			updateView( model.root(), this, _dt ) ;
		}

		list.update( _dt ) ;
	}

	/**
		Assuming the passed in _node is a parent update 
		all child elements to fit within the passed in _parent element.

		The node passed in may not have any children, if this is 
		the case then row/column count will return 0.
	*/
	private void updateView( final UIModelIndex _node,
							 final UIElement _parent,
							 final float _dt )
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
				updateView( index, element, _dt ) ;
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
			UIElement cell = _delegate.createItem( this, model, _index ) ;
			if( cell == null )
			{
				cell = FALLBACK_ITEM_DELEGATE.createItem( this, model, _index ) ;
			}

			variant = new UIVariant( "CELL", cell ) ;
			view.setData( _index, variant, IAbstractModel.Role.Display ) ;
			list.addElement( cell ) ;
			list.makeDirty() ;
		}

		final UIElement cell = variant.toObject( UIElement.class )  ;
		if( _delegate.setItemData( cell, model, _index ) == false )
		{
			FALLBACK_ITEM_DELEGATE.setItemData( cell, model, _index ) ;
		}
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
		if( _delegate.destroyItem( cell ) == false )
		{
			FALLBACK_ITEM_DELEGATE.destroyItem( cell ) ;
		}
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
			private final Connect connect = new Connect() ;

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

			@Override
			public Connect getConnect()
			{
				return connect ;
			}
		} ;
	}

	/**
		Not generic - expects the data to be stored in the 
		format used by UI Meta objects. 
	*/
	private static ItemDelegate<?> createDefaultItemDelegate()
	{
		return new ItemDelegate<UIElement>()
		{
			@Override
			public UIElement createItem( final UIAbstractView _parent, final IAbstractModel _model, final UIModelIndex _index )
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

				final UILayout valLayout = layout.addElement( UIGenerator.<UILayout>create( metaLayout ) ) ;
				addElements( valLayout, _model, _index ) ;
				return layout ;
			}

			private void addElements( final UILayout _layout, final IAbstractModel _model, final UIModelIndex _index )
			{
				final IVariant variant = _model.getData( _index, IAbstractModel.Role.User ) ;
				switch( variant.getType() )
				{
					default :
					{
						hook( _layout.addElement( createTextField() ), _layout, _model, _index ) ;
						break ;
					}
					case AVariable.BOOLEAN_TYPE :
					{
						hook( _layout.addElement( createCheckbox() ), _layout, _model, _index ) ;
						break ;
					}
					case AVariable.INT_TYPE :
					{
						hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						break ;
					}
					case AVariable.FLOAT_TYPE :
					{
						hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						break ;
					}
					case AVariable.OBJECT_TYPE :
					{
						final Object obj = variant.toObject() ;
						if( obj instanceof Vector2 )
						{
							// x, y
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						}
						else if( obj instanceof Vector3 )
						{
							// x, y, z
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						}
						else if( obj instanceof Colour )
						{
							// r, g, b, a
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						}
						else if( obj instanceof Font )
						{
							// name, font size
							hook( _layout.addElement( createTextField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createTextField() ), _layout, _model, _index ) ;
						}
						else if( obj instanceof UIElement.UV )
						{
							// minx, miny, maxx, maxy
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
							hook( _layout.addElement( createNumberField() ), _layout, _model, _index ) ;
						}
						else
						{
							hook( _layout.addElement( createTextField() ), _layout, _model, _index ) ;
						}
						break ;
					}
				}
			}

			private UICheckbox hook( final UICheckbox _check, final UILayout _layout, final IAbstractModel _model, final UIModelIndex _index )
			{
				UIElement.connect( _check, _check.checkChanged(), new Connect.Slot<UICheckbox>()
				{
					@Override
					public void slot( final UICheckbox _this )
					{
						setModelData( _layout, _model, _index ) ;
					}
				} ) ;

				return _check ;
			}

			private UITextField hook( final UITextField _field, final UILayout _layout, final IAbstractModel _model, final UIModelIndex _index )
			{
				UIElement.connect( _field, _field.elementDisengaged(), new Connect.Slot<UITextField>()
				{
					@Override
					public void slot( final UITextField _this )
					{
						setModelData( _layout, _model, _index ) ;
					}
				} ) ;

				return _field ;
			}

			private UICheckbox createCheckbox()
			{
				final UICheckbox.Meta meta = new UICheckbox.Meta() ;

				final GUITick.Meta tick = meta.addComponent( new GUITick.Meta() ) ;
				tick.setUV( 0.5f, 0.0f, 1.0f, 0.5f ) ;
				tick.setAlignment( UI.Alignment.CENTRE, UI.Alignment.CENTRE ) ;
				tick.setRetainRatio( true ) ;
				tick.setSheet( "base/textures/checkbox_sheet.png" ) ;

				final GUIPanelDraw.Meta draw = meta.addComponent( new GUIPanelDraw.Meta() ) ;
				draw.setNeutralUV( 0.0f, 0.0f, 0.5f, 0.5f ) ;
				draw.setRolloverUV( 0.0f, 0.5f, 0.5f, 1.0f ) ;
				draw.setClickedUV( 0.0f, 0.5f, 0.5f, 1.0f ) ;
				draw.setSheet( "base/textures/checkbox_sheet.png" ) ;
				draw.setAlignment( UI.Alignment.CENTRE, UI.Alignment.CENTRE ) ;
				draw.setRetainRatio( true ) ;

				return UIGenerator.<UICheckbox>create( meta ) ;
			}

			private UITextField createNumberField()
			{
				final UITextField.Meta meta = new UITextField.Meta() ;
				final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
				edge.setSheet( "base/textures/edge_button.png" ) ;

				final GUIEditText.Meta text = meta.addComponent( new GUIEditText.Meta() ) ;
				text.setAlignment( UI.Alignment.LEFT, UI.Alignment.CENTRE ) ;
				text.setOnlyNumbers( true ) ;
				text.setGroup( "ENGINE" ) ;
				text.setName( "VALUE" ) ;
				text.setText( "Test" ) ;
			
				return UIGenerator.<UITextField>create( meta ) ;
			}

			private UITextField createTextField()
			{
				final UITextField.Meta meta = new UITextField.Meta() ;
				final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
				edge.setSheet( "base/textures/edge_button.png" ) ;

				final GUIEditText.Meta text = meta.addComponent( new GUIEditText.Meta() ) ;
				text.setAlignment( UI.Alignment.LEFT, UI.Alignment.CENTRE ) ;
				text.setGroup( "ENGINE" ) ;
				text.setName( "VALUE" ) ;
				text.setText( "Test" ) ;
			
				return UIGenerator.<UITextField>create( meta ) ;
			}

			@Override
			public boolean destroyItem( final UIElement _item )
			{
				// We don't need to explicitly remove the item from 
				// the list, calling destroy will cause it to be removed 
				// during the lists next update cycle.
				_item.destroy() ;
				return true ;
			}

			@Override
			public boolean setItemData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index )
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
					final UILayout vLayout = ( UILayout )layout.getElements().get( 1 ) ;
					switch( variant.getType() )
					{
						default                             :
						{
							setTextTo( getText( vLayout, 0 ), variant.toString() ) ;
							break ;
						}
						case AVariable.BOOLEAN_TYPE :
						{
							final UICheckbox value = ( UICheckbox )vLayout.getElements().get( 0 ) ;
							value.setChecked( variant.toBool() ) ;
							break ;
						}
						case AVariable.OBJECT_TYPE :
						{
							final Object obj = variant.toObject() ;
							if( obj instanceof Vector2 )
							{
								final Vector2 vec = ( Vector2 )obj ;
								setTextTo( getText( vLayout, 0 ), vec.x ) ;
								setTextTo( getText( vLayout, 1 ), vec.y ) ;
							}
							else if( obj instanceof Vector3 )
							{
								final Vector3 vec = ( Vector3 )obj ;
								setTextTo( getText( vLayout, 0 ), vec.x ) ;
								setTextTo( getText( vLayout, 1 ), vec.y ) ;
								setTextTo( getText( vLayout, 2 ), vec.z ) ;
							}
							else if( obj instanceof Colour )
							{
								final Colour colour = ( Colour )obj ;
								setTextTo( getText( vLayout, 0 ), colour.getRed() ) ;
								setTextTo( getText( vLayout, 1 ), colour.getGreen() ) ;
								setTextTo( getText( vLayout, 2 ), colour.getBlue() ) ;
								setTextTo( getText( vLayout, 3 ), colour.getAlpha() ) ;
							}
							else if( obj instanceof Font )
							{
								final Font font = variant.toObject( Font.class ) ;
								setTextTo( getText( vLayout, 0 ), font.getFontName() ) ;
								setTextTo( getText( vLayout, 1 ), font.getPointSize() ) ;
							}
							else if( obj instanceof UIElement.UV )
							{
								final UIElement.UV uv = ( UIElement.UV )obj ;
								setTextTo( getText( vLayout, 0 ), uv.min.x ) ;
								setTextTo( getText( vLayout, 1 ), uv.min.y ) ;
								setTextTo( getText( vLayout, 2 ), uv.max.x ) ;
								setTextTo( getText( vLayout, 3 ), uv.max.y ) ;
							}
							else
							{
								setTextTo( getText( vLayout, 0 ), variant.toString() ) ;
							}
							break ;
						}
					}
				}

				return true ;
			}

			@Override
			public boolean setModelData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index )
			{
				final IVariant variant = _model.getData( _index, IAbstractModel.Role.User ) ;

				try
				{
					final UILayout vLayout = ( UILayout )_item ;
					switch( variant.getType() )
					{
						case AVariable.STRING_TYPE  :
						{
							final StringBuilder text = getText( vLayout, 0 ) ;
							variant.setString( text.toString() ) ;
							signal( _model, variant ) ;
							break ;
						}
						case AVariable.BOOLEAN_TYPE :
						{
							variant.setBool( getBoolean( vLayout, 0 ) ) ;
							signal( _model, variant ) ;
							break ;
						}
						case AVariable.INT_TYPE     :
						{
							variant.setInt( toInt( getText( vLayout, 0 ) ) ) ;
							signal( _model, variant ) ;
							break ;
						}
						case AVariable.FLOAT_TYPE   :
						{
							variant.setFloat( toFloat( getText( vLayout, 0 ) ) ) ;
							signal( _model, variant ) ;
							break ;
						}
						case AVariable.OBJECT_TYPE  :
						{
							final Object obj = variant.toObject() ;
							if( obj instanceof Vector2 )
							{
								final float x = toFloat( getText( vLayout, 0 ) ) ;
								final float y = toFloat( getText( vLayout, 1 ) ) ;
								variant.setVector2( x, y ) ;
								signal( _model, variant ) ;
							}
							else if( obj instanceof Vector3 )
							{
								final float x = toFloat( getText( vLayout, 0 ) ) ;
								final float y = toFloat( getText( vLayout, 1 ) ) ;
								final float z = toFloat( getText( vLayout, 2 ) ) ;
								variant.setVector3( x, y, z ) ;
								signal( _model, variant ) ;
							}
							else if( obj instanceof Colour )
							{
								final Colour colour = ( Colour )obj ;
								final byte r = ( byte )toInt( getText( vLayout, 0 ) ) ;
								final byte g = ( byte )toInt( getText( vLayout, 1 ) ) ;
								final byte b = ( byte )toInt( getText( vLayout, 2 ) ) ;
								final byte a = ( byte )toInt( getText( vLayout, 3 ) ) ;
								colour.changeColour( r, g, b, a ) ;
								signal( _model, variant ) ;
							}
							else if( obj instanceof Font )
							{
								final String name = getText( vLayout, 0 ).toString() ;
								final float size = toFloat( getText( vLayout, 1 ) ) ;
								variant.setObject( new Font( name, 12 ) ) ;
								signal( _model, variant ) ;
							}
							else if( obj instanceof UIElement.UV )
							{
								final float minx = toFloat( getText( vLayout, 0 ) ) ;
								final float miny = toFloat( getText( vLayout, 1 ) ) ;
								final float maxx = toFloat( getText( vLayout, 2 ) ) ;
								final float maxy = toFloat( getText( vLayout, 3 ) ) ;

								final UIElement.UV uv = ( UIElement.UV )obj ;
								uv.min.setXY( minx, miny ) ;
								uv.max.setXY( maxx, maxy ) ;
								signal( _model, variant ) ;
							}
							else if( obj instanceof Enum<?> )
							{
								try
								{
									final Enum<?> en = ( Enum<?> )obj ;
									final String val = getText( vLayout, 0 ).toString() ;
									variant.setObject( en.valueOf( en.getClass(), val ) ) ;
									signal( _model, variant ) ;
								}
								catch( IllegalArgumentException ex )
								{
									System.out.println( "Value not compatible with enum." ) ;
								}
							}
							else
							{
								System.out.println( "Unable to set.." ) ;
							}
							break ;
						}
						default                             : break ;
					}
				}
				catch( Exception ex )
				{
					signal( _model, variant ) ;
					return false ;
				}

				return true ;
			}

			private void signal( final IAbstractModel _model, final IVariant _variant )
			{
				final Connect.Signal signal = _variant.getSignal() ;
				if( signal != null )
				{
					UIElement.signal( _model, signal ) ;
				}
			}

			private byte toByte( final StringBuilder _text )
			{
				return Byte.parseByte( _text.toString() ) ;
			}

			private int toInt( final StringBuilder _text )
			{
				return Integer.parseInt( _text.toString() ) ;
			}

			private float toFloat( final StringBuilder _text )
			{
				final String trimmed = _text.toString().trim() ;
				return (trimmed.length() > 0) ? Float.parseFloat( _text.toString() ) : 0.0f ;
			}

			private boolean getBoolean( final UILayout _layout, final int _index )
			{
				final UICheckbox value = ( UICheckbox )_layout.getElements().get( _index ) ;
				return value.isChecked() ;
			}

			private StringBuilder getText( final UILayout _layout, final int _index )
			{
				final UITextField value = ( UITextField )_layout.getElements().get( _index ) ;
				final GUIText gui = value.getComponent( "ENGINE", "VALUE", GUIEditText.class ) ;
				return gui.getText() ;
			}

			private void setTextTo( final StringBuilder _text, final float _value )
			{
				_text.setLength( 0 ) ;
				_text.append( Float.toString( _value ) ) ;
			}

			private void setTextTo( final StringBuilder _text, final int _value )
			{
				_text.setLength( 0 ) ;
				_text.append( Integer.toString( _value ) ) ;
			}

			private void setTextTo( final StringBuilder _text, final String _value )
			{
				_text.setLength( 0 ) ;
				_text.append( _value ) ;
			}
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
		public T createItem( final UIAbstractView _parent, final IAbstractModel _model, final UIModelIndex _index ) ;

		/**
			Clean up any resources that may have been used by this cell.
		*/
		public boolean destroyItem( final T _item ) ;

		/**
			Apply any initial data to the item.
		*/
		public boolean setItemData( final T _item, final IAbstractModel _model, final UIModelIndex _index ) ;

		/**
			Update the model based on the modifications by the item.
		*/
		public boolean setModelData( final T _item, final IAbstractModel _model, final UIModelIndex _index ) ;
	}
}
