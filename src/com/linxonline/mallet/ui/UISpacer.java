package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UISpacer extends UIElement
{
	public enum Axis
	{
		ONLY_X,
		ONLY_Y,
		ONLY_Z,
		ONLY_XYZ ;

		public static Axis derive( final String _type )
		{
			if( _type == null )
			{
				return ONLY_XYZ ;
			}
			
			if( _type.isEmpty() == true )
			{
				return ONLY_XYZ ;
			}

			return Axis.valueOf( _type ) ;
		}
	}

	private final UISpacer.Axis axis ;

	public UISpacer()
	{
		this( Axis.ONLY_XYZ ) ; 
	}

	public UISpacer( final UISpacer.Axis _axis )
	{
		axis = _axis ; 
	}

	public UISpacer.Axis getAxis()
	{
		return axis ;
	}

	@Override
	public void refresh() {}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void shutdown() {}

	public static UISpacer applyMeta( final UISpacer.Meta _meta, final UISpacer _spacer )
	{
		UIElement.applyMeta( _meta, _spacer ) ;
		return _spacer ;
	}

	public static class Meta extends UIElement.Meta
	{
		private final UIVariant axis = new UIVariant( "AXIS", Axis.ONLY_X, new Connect.Signal() ) ;

		public Meta()
		{
			super() ;
			int row = rowCount( root() ) ;
			createData( null, row + 1, 1 ) ;
			setData( new UIModelIndex( root(), row++, 0 ), axis, UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getElementType()
		{
			return "UISPACER" ;
		}

		public void setAxis( final Axis _axis )
		{
			if( _axis.equals( axis.toObject() ) == false )
			{
				axis.setObject( _axis ) ;
				UIElement.signal( this, axis.getSignal() ) ;
			}
		}

		public Axis getAxis()
		{
			return axis.toObject( Axis.class ) ;
		}

		public Connect.Signal axisChanged()
		{
			return axis.getSignal() ;
		}
	}
}
