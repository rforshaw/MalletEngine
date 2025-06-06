package com.linxonline.mallet.util.settings ;

import java.util.TreeMap ;
import java.util.Collection ;

/**
	Would like to add an Annotation that will switch the String comparison to 
	a hashcode check.

	This would be enabled or disabled at compile time.
**/
public final class Settings
{
	private final TreeMap<String, AVariable> variables = new TreeMap<String, AVariable>() ;
	private static final NullPointerException exception = new NullPointerException() ;			// Defined as static to reduce creation time

	public Settings() {}

	/* Add Value */

	public final void addBoolean( final String _name, final boolean _value )
	{
		final BooleanVariable obj = this.<BooleanVariable>getVar( _name, AVariable.BOOLEAN_TYPE ) ;
		if( obj != null )
		{
			obj.setBoolean( _value ) ;
			return ;
		}

		final BooleanVariable var = new BooleanVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}
	
	public final void addInteger( final String _name, final int _value )
	{
		final IntegerVariable obj = this.<IntegerVariable>getVar( _name, AVariable.INT_TYPE ) ;
		if( obj != null )
		{
			obj.setInteger( _value ) ;
			return ;
		}

		final IntegerVariable var = new IntegerVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final void addFloat( final String _name, final float _value )
	{
		final FloatVariable obj = this.<FloatVariable>getVar( _name, AVariable.FLOAT_TYPE ) ;
		if( obj != null )
		{
			obj.setFloat( _value ) ;
			return ;
		}

		final FloatVariable var = new FloatVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final void addString( final String _name, final String _value )
	{
		final StringVariable obj = this.<StringVariable>getVar( _name, AVariable.STRING_TYPE ) ;
		if( obj != null )
		{
			obj.setString( _value ) ;
			return ;
		}

		final StringVariable var = new StringVariable( _name, _value ) ;
		variables.put( _name, var ) ;
	}

	public final <T> void addObject( final String _name, final T _value )
	{
		final ObjectVariable<T> obj = this.<ObjectVariable<T>>getVar( _name, AVariable.OBJECT_TYPE ) ;
		if( obj != null )
		{
			obj.setObject( _value ) ;
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
		final BooleanVariable obj = this.<BooleanVariable>getVar( _name, AVariable.BOOLEAN_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		throw exception ;
	}

	public final boolean getBoolean( final String _name, final boolean _default )
	{
		final BooleanVariable obj = this.<BooleanVariable>getVar( _name, AVariable.BOOLEAN_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		return _default ;
	}

	public final int getInteger( final String _name ) throws NullPointerException
	{
		final IntegerVariable obj = this.<IntegerVariable>getVar( _name, AVariable.INT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		throw exception ;
	}

	public final int getInteger( final String _name, final int _default )
	{
		final IntegerVariable obj = this.<IntegerVariable>getVar( _name, AVariable.INT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		return _default ;
	}

	public final float getFloat( final String _name ) throws NullPointerException
	{
		final FloatVariable obj = this.<FloatVariable>getVar( _name, AVariable.FLOAT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		throw exception ;
	}

	public final float getFloat( final String _name, final float _default )
	{
		final FloatVariable obj = this.<FloatVariable>getVar( _name, AVariable.FLOAT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		return _default ;
	}

	public final String getString( final String _name ) throws NullPointerException
	{
		final StringVariable obj = this.<StringVariable>getVar( _name, AVariable.STRING_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		throw exception ;
	}

	public final String getString( final String _name, final String _default )
	{
		final StringVariable obj = this.<StringVariable>getVar( _name, AVariable.STRING_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		return _default ;
	}

	public final <T> T getObject( final String _name ) throws NullPointerException
	{
		final ObjectVariable<T> obj = this.<ObjectVariable<T>>getVar( _name, AVariable.OBJECT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		throw exception ;
	}

	/**
		WARNING: Will attempt to cast to requested type, this is NOT
		type safe.
	*/
	public final <T> T getObject( final String _name, final T _default )
	{
		final ObjectVariable<T> obj = this.<ObjectVariable<T>>getVar( _name, AVariable.OBJECT_TYPE ) ;
		if( obj != null )
		{
			return obj.value ;
		}

		return _default ;
	}

	@SuppressWarnings( "unchecked" )
	private final <T extends AVariable> T getVar( final String _name, final int _type )
	{
		final T t = ( T )variables.get( _name ) ;
		if( t != null )
		{
			if( t.type == _type )
			{
				return t ;
			}
		}

		return null ;
	}
	
	public final Collection<AVariable> toArray()
	{
		return variables.values() ;
	}

	public final String[] toArrayString()
	{
		final Collection<AVariable> collection = variables.values() ;
		final String[] list = new String[collection.size()] ;

		final StringBuilder buffer = new StringBuilder() ;

		int i = 0 ;
		for( final AVariable inter : collection )
		{
			buffer.append( inter.name ) ;
			buffer.append( ' ' ) ;
			buffer.append( '\"' ) ;
			buffer.append( inter.toString() ) ;
			buffer.append( '\"' ) ;

			list[i++] = buffer.toString() ;
			buffer.setLength( 0 ) ;
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
		final String[] strings = toArrayString() ;
		final StringBuilder buffer = new StringBuilder() ;

		for( int i = 0; i < strings.length; ++i )
		{
			buffer.append( strings[i] ) ;
			buffer.append( '\n' ) ;
		}

		return buffer.toString() ;
	}
}
