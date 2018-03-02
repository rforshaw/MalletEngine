package com.linxonline.mallet.ui ;

import java.util.List ;
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
	private final Set<IAbstractModel.ItemFlags> cellFlags = new HashSet<IAbstractModel.ItemFlags>() ;
	private IAbstractModel model ;
	private boolean refresh = false ;

	public UIAbstractView()
	{
		super() ;
	}

	public void update( final float _dt, final List<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
		if( model != null && refresh == true )
		{
			updateModel( null ) ;
			refresh = false ;
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

				final IVariant display = model.getData( index, IAbstractModel.Role.Display ) ;
				final IVariant edit    = model.getData( index, IAbstractModel.Role.Edit ) ;
				final IVariant user    = model.getData( index, IAbstractModel.Role.User ) ;

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
									  final Set<IAbstractModel.ItemFlags> _flags,
									  final IVariant _display,
									  final IVariant _edit,
									  final IVariant _user ) ;

	public void refresh()
	{
		refresh = true ;
	}

	public void setModel( final IAbstractModel _model )
	{
		model = _model ;
	}

	public IAbstractModel getModel()
	{
		return model ;
	}
}
