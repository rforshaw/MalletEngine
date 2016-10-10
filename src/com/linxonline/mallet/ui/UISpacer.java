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
}
