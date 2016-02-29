package com.linxonline.mallet.ui ;

import com.linxonline.mallet.util.settings.* ;

public class UIVariant
{
	private final VariableInterface variable ;

	public UIVariant( final String _name, final boolean _val )
	{
		variable = new BooleanVariable( _name, _val ) ;
	}

	public UIVariant( final String _name, final int _val )
	{
		variable = new IntegerVariable( _name, _val ) ;
	}

	public UIVariant( final String _name, final float _val )
	{
		variable = new FloatVariable( _name, _val ) ;
	}

	public UIVariant( final String _name, final String _val )
	{
		variable = new StringVariable( _name, _val ) ;
	}

	public <T> UIVariant( final String _name, final T _val )
	{
		variable = new ObjectVariable<T>( _name, _val ) ;
	}

	public boolean toBool()
	{
		switch( variable.getType() )
		{
			case VariableInterface.INT_TYPE     : return ( ( IntegerVariable )variable ).value >= 0 ? true : false ;
			case VariableInterface.FLOAT_TYPE   : return ( ( FloatVariable )variable ).value >= 0.0f ? true : false ;
			case VariableInterface.STRING_TYPE  : return "true".equals( ( ( StringVariable )variable ).value ) ;
			case VariableInterface.BOOLEAN_TYPE : return ( ( BooleanVariable )variable ).value ;
			default           : return false ;
		}
	}
	
	@Override
	public String toString()
	{
		return variable.toString() ;
	}
}