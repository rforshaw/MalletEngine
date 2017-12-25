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
		private Axis axis = Axis.ONLY_X ;
		
		private Connect.Signal axisChanged = new Connect.Signal() ;

		public Meta() {}

		@Override
		public String getElementType()
		{
			return "UISPACER" ;
		}

		public void setAxis( final Axis _axis )
		{
			if( _axis != axis )
			{
				axis = _axis ;
			}
		}

		public Axis getAxis()
		{
			return axis ;
		}

		public Connect.Signal axisChanged()
		{
			return axisChanged ;
		}
	}
}
