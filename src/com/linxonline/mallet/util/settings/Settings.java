package com.linxonline.mallet.util.settings ;

import java.util.ArrayList ;
import java.util.TreeMap ;
import java.util.Collection ;

/**
	Would like to add an Annotation that will switch the String comparison to 
	a hashcode check.

	This would be enabled or disabled at compile time.
**/
public final class Settings
{
	private final TreeMap<String, VariableInterface> variables = new TreeMap<String, VariableInterface>() ;
	private static final NullPointerException exception = new NullPointerException() ;			// Defined as static to reduce creation time
	private VariableInterface inter = null ;

	public Settings() {}

	/* Add Value */

	public final void addBoolean( final String _name, final boolean _value )
	{
		inter = getVariable( _name, VariableInterface.BOOLEAN_TYPE ) ;
		if( inter != null )
		{
			final BooleanVariable var = ( BooleanVariable )inter ;
			var.setBoolean( _value ) ;
			return ;
		}

		final BooleanVariable var = new BooleanVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}
	
	public final void addInteger( final String _name, final int _value )
	{
		inter = getVariable( _name, VariableInterface.INT_TYPE ) ;
		if( inter != null )
		{
			final IntegerVariable var = ( IntegerVariable )inter ;
			var.setInteger( _value ) ;
			return ;
		}

		final IntegerVariable var = new IntegerVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final void addFloat( final String _name, final float _value )
	{
		inter = getVariable( _name, VariableInterface.FLOAT_TYPE ) ;
		if( inter != null )
		{
			final FloatVariable var = ( FloatVariable )inter ;
			var.setFloat( _value ) ;
			return ;
		}

		final FloatVariable var = new FloatVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final void addString( final String _name, final String _value )
	{
		inter = getVariable( _name, VariableInterface.STRING_TYPE ) ;
		if( inter != null )
		{
			final StringVariable var = ( StringVariable )inter ;
			var.setString( _value ) ;
			return ;
		}

		final StringVariable var = new StringVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final <T> void addObject( final String _name, final T _value )
	{
		inter = getVariable( _name, VariableInterface.OBJECT_TYPE ) ;
		if( inter != null )
		{
			final ObjectVariable var = ( ObjectVariable )inter ;
			var.setObject( _value ) ;
			return ;
		}

		final ObjectVariable<T> var = new ObjectVariable<T>( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	/* Remove Variable */

	public boolean remove( final String _name )
	{
		return ( variables.remove( _name ) != null ) ? true : false ;
	}

	/* Return value, default or throw exception */

	public final boolean getBoolean( final String _name ) throws NullPointerException
	{
		inter = getVariable( _name, VariableInterface.BOOLEAN_TYPE ) ;
		if( inter != null )
		{
			final BooleanVariable var = ( BooleanVariable )inter ;
			return var.value ;
		}

		throw exception ;
	}

	public final boolean getBoolean( final String _name, final boolean _default )
	{
		inter = getVariable( _name, VariableInterface.BOOLEAN_TYPE ) ;
		if( inter != null )
		{
			final BooleanVariable var = ( BooleanVariable )inter ;
			return var.value ;
		}

		return _default ;
	}

	public final int getInteger( final String _name ) throws NullPointerException
	{
		inter = getVariable( _name, VariableInterface.INT_TYPE ) ;
		if( inter != null )
		{
			final IntegerVariable var = ( IntegerVariable )inter ;
			return var.value ;
		}

		throw exception ;
	}

	public final int getInteger( final String _name, final int _default )
	{
		inter = getVariable( _name, VariableInterface.INT_TYPE ) ;
		if( inter != null )
		{
			final IntegerVariable var = ( IntegerVariable )inter ;
			return var.value ;
		}

		return _default ;
	}

	public final float getFloat( final String _name ) throws NullPointerException
	{
		inter = getVariable( _name, VariableInterface.FLOAT_TYPE ) ;
		if( inter != null )
		{
			final FloatVariable var = ( FloatVariable )inter ;
			return var.value ;
		}

		throw exception ;
	}

	public final float getFloat( final String _name, final float _default )
	{
		inter = getVariable( _name, VariableInterface.FLOAT_TYPE ) ;
		if( inter != null )
		{
			final FloatVariable var = ( FloatVariable )inter ;
			return var.value ;
		}

		return _default ;
	}

	public final String getString( final String _name ) throws NullPointerException
	{
		inter = getVariable( _name, VariableInterface.STRING_TYPE ) ;
		if( inter != null )
		{
			final StringVariable var = ( StringVariable )inter ;
			return var.value ;
		}

		throw exception ;
	}

	public final String getString( final String _name, final String _default )
	{
		inter = getVariable( _name, VariableInterface.STRING_TYPE ) ;
		if( inter != null )
		{
			final StringVariable var = ( StringVariable )inter ;
			return var.value ;
		}

		return _default ;
	}

	public final Object getObject( final String _name ) throws NullPointerException
	{
		inter = getVariable( _name, VariableInterface.OBJECT_TYPE ) ;
		if( inter != null )
		{
			final ObjectVariable var = ( ObjectVariable )inter ;
			return var.value ;
		}

		throw exception ;
	}

	/**
		WARNING: Will attempt to cast to requested type, this is NOT
		type safe.
	*/
	public final <T> T getObject( final String _name, final T _default )
	{
		inter = getVariable( _name, VariableInterface.OBJECT_TYPE ) ;
		if( inter != null )
		{
			final ObjectVariable var = ( ObjectVariable )inter ;
			return ( T )var.getObject() ;
		}

		return _default ;
	}

	private final VariableInterface getVariable( final String _name, final int _type )
	{
		inter = variables.get( _name ) ;
		if( inter != null )
		{
			if( inter.type == _type )
			{
				return inter ;
			}
		}

		return null ;
	}

	public final ArrayList<String> toArrayString()
	{
		final Collection<VariableInterface> collection = variables.values() ;
		final ArrayList<String> list = new ArrayList<String>() ;
		final StringBuilder buffer = new StringBuilder() ;

		for( final VariableInterface inter : collection )
		{
			buffer.append( inter.name + " " + "\"" + inter.toString() + "\"" ) ;
			list.add( buffer.toString() ) ;
			buffer.delete( 0, buffer.length() ) ;
		}

		return list ;
	}

	/**
		Return an array of keys used within
		the Settings object.
	*/
	public final String[] keys()
	{
		return variables.keySet().toArray( new String[0] ) ;
	}

	public final String toString()
	{
		final ArrayList<String> strings = toArrayString() ;
		final StringBuilder buffer = new StringBuilder() ;
		final int size = strings.size() ;
		String value = null ;

		for( int i = 0; i < size; ++i )
		{
			value = strings.get( i ) ;
			buffer.append( value + "\n" ) ;
		}

		return buffer.toString() ;
	}
}
