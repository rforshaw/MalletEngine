package com.linxonline.mallet.ui ;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Set ;

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
	private final Set<UIAbstractModel.ItemFlags> cellFlags = new HashSet<UIAbstractModel.ItemFlags>() ;
	private UIAbstractModel model ;

	public UIAbstractView( final Vector3 _offset,
						   final Vector3 _length )
	{
		this( new Vector3(), _offset, _length ) ;
	}

	public UIAbstractView( final Vector3 _position,
						   final Vector3 _offset,
						   final Vector3 _length )
	{
		super( _position, _offset, _length ) ;
	}

	public void update( final float _dt, final ArrayList<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
		if( model != null )
		{
			updateModel( null ) ;
		}
	}

	private void updateModel( final UIModelIndex _node )
	{
		final int rowCount = model.rowCount( _node ) ;
		final int columnCount = model.columnCount( _node ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _node, i, j ) ;
				if( model.hasChildren( index ) == true )
				{
					updateModel( index ) ;
				}

				model.getDataFlags( index, cellFlags ) ;

				final UIVariant display = model.getData( index, UIAbstractModel.Role.Display ) ;
				final UIVariant edit    = model.getData( index, UIAbstractModel.Role.Edit ) ;
				final UIVariant user    = model.getData( index, UIAbstractModel.Role.User ) ;

				displayCell( index, cellFlags, display, edit, user ) ;
				cellFlags.clear() ;
			}
		}
	}

	/**
		Determine what should be displayed and it's location 
		for being rendered.
	*/
	public abstract void displayCell( final UIModelIndex _index,
									  final Set<UIAbstractModel.ItemFlags> _flags,
									  final UIVariant _display,
									  final UIVariant _edit,
									  final UIVariant _user ) ;

	public void setModel( UIAbstractModel _model )
	{
		model = _model ;
	}

	public UIAbstractModel getModel()
	{
		return model ;
	}
}