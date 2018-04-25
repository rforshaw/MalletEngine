package com.linxonline.mallet.ui ;

import java.util.List ;

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
public abstract class UIAbstractView extends UIElement
{
	private final FrameBuffer frame ;

	private ItemDelegate<?> defaultItemDelegate = createDefaultItemDelegate() ;
	private ItemDelegate<?>[] columnItemDelegate = new ItemDelegate<?>[1] ;
	private ItemDelegate<?>[] rowItemDelegate = new ItemDelegate<?>[1] ;

	private final IAbstractModel view = new UIAbstractModel() ;
	private IAbstractModel model ;

	public UIAbstractView()
	{
		super() ;

		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		frame = new FrameBuffer( position.x, position.y, position.z,
								 offset.x,   offset.y,   offset.z,
								 length.x,   length.y,   getLayer() + 1 ) ;
		initFrameConnections() ;
	}

	private void initFrameConnections()
	{
		addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				frame.setDrawDelegate( _delegate ) ;
				UIAbstractView.this.passViewDrawDelegate( model.root(), frame.getDrawDelegate(), frame.getWorld(), frame.getCamera() ) ;
			}
		} ) ) ;

		FrameBuffer.connect( this, frame ) ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		// Listeners to the UIAbstractView will be given the 
		// DrawDelegate passed in here - only children 
		// will be given the UIAbstractView DrawDelegate.
		super.passDrawDelegate( _delegate, _world, _camera ) ;

		// UIAbstractView will give its children its 
		// own DrawDelegate the UIAbstractView will give the 
		// DrawDelegate passed in here the Draw pane.
		_delegate.addBasicDraw( frame.getFrame(), _world ) ;
	}

	private void passViewDrawDelegate( final UIModelIndex _node, final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		final int rowCount = model.rowCount( _node ) ;
		final int columnCount = model.columnCount( _node ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _node, i, j ) ;

				final UIElement element = getCellElement( index, getItemDelegate( index ) ) ;
				element.passDrawDelegate( _delegate, _world, _camera ) ;

				// Pass the DrawDelegate of UIAbstractView to the 
				// UIAbstractView children instead of the DrawDelegate
				// that is provided by passDrawDelegate.
				passViewDrawDelegate( index, _delegate, _world, _camera ) ;
			}
		}
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
	public void update( final float _dt, final List<Event<?>> _events )
	{
		final boolean dirty = isDirty() ;
		super.update( _dt, _events ) ;
		if( model != null && dirty == true )
		{
			updateView( model.root(), this, _dt, _events ) ;
		}
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

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _node, i, j ) ;

				final UIElement element = getCellElement( index, getItemDelegate( index ) ) ;
				element.update( _dt, _events ) ;

				updateView( index, element, _dt, _events ) ;
			}
		}
	}

	public void setModel( final IAbstractModel _model )
	{
		model = _model ;
	}

	public IAbstractModel getModel()
	{
		return model ;
	}

	/**
		Return a cell that is associated with the passed in index.
		If no cell exist construct one and apply display data from 
		the model to it.
	*/
	private UIElement getCellElement( final UIModelIndex _index, final ItemDelegate _delegate )
	{
		IVariant variant = view.getData( _index, IAbstractModel.Role.Display ) ;
		if( variant == null )
		{
			if( view.createData( _index.getParent(), _index.getRow(), _index.getColumn() ) )
			{
				final UIElement cell = _delegate.createItem( this ) ;
				if( frame.getDrawDelegate() != null )
				{
					cell.passDrawDelegate( frame.getDrawDelegate(), frame.getWorld(), frame.getCamera() ) ;
				}
				
				_delegate.setItemData( cell, model, _index ) ;

				variant = new UIVariant( "CELL", cell ) ;
				view.setData( _index, variant, IAbstractModel.Role.Display ) ;
			}
		}

		final UIElement cell = variant.toObject( UIElement.class )  ;
		_delegate.setItemData( cell, model, _index ) ;
		return cell ;
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

	private static ItemDelegate createDefaultItemDelegate()
	{
		return new ItemDelegate<UIElement>()
		{
			public UIElement createItem( final UIAbstractView _parent )
			{
				return new UIElement() ;
			}

			public void setItemData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index ) {}

			public void setModelData( final UIElement _item, final IAbstractModel _model, final UIModelIndex _index ) {}
		} ;
	}

	public interface ItemDelegate<T extends UIElement>
	{
		/**
			Create the item that is to be used within the cell.
		*/
		public T createItem( final UIAbstractView _parent ) ;

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
