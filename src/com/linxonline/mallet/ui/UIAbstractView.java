package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

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
		super() ;
		setPosition( _position.x, _position.y, _position.z ) ;
		setOffset( _offset.x, _offset.y, _offset.z ) ;
		setLength( _length.x, _length.y, _length.z ) ;
	}

	public void update( final float _dt, final ArrayList<Event<?>> _events )
	{
		super.update( _dt, _events ) ;
	}

	public void setModel( UIAbstractModel _model )
	{
		model = _model ;
	}

	public UIAbstractModel getModel()
	{
		return model ;
	}
}