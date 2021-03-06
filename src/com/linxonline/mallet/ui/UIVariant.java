package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.maths.* ;

public class UIVariant implements IVariant
{
	private final AVariable variable ;
	
	/**
		A UIVariant can contain a signal however the variant 
		itself does not support its own Connections as the 
		UIVariant is expected to be a child object of a larger 
		state - which would support the connections.
	*/
	private final Connect.Signal signal ;

	public UIVariant( final String _name, final boolean _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final boolean _val, Connect.Signal _signal )
	{
		variable = new BooleanVariable( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final int _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final int _val, Connect.Signal _signal )
	{
		variable = new IntegerVariable( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final float _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final float _val, Connect.Signal _signal )
	{
		variable = new FloatVariable( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final String _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final String _val, Connect.Signal _signal )
	{
		variable = new StringVariable( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final Vector2 _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final Vector2 _val, Connect.Signal _signal )
	{
		variable = new ObjectVariable<Vector2>( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final Vector3 _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final Vector3 _val, Connect.Signal _signal )
	{
		variable = new ObjectVariable<Vector3>( _name, _val ) ;
		signal = _signal ;
	}

	public UIVariant( final String _name, final MalletColour _val )
	{
		this( _name, _val, null ) ;
	}

	public UIVariant( final String _name, final MalletColour _val, Connect.Signal _signal )
	{
		variable = new ObjectVariable<MalletColour>( _name, _val ) ;
		signal = _signal ;
	}

	public <T> UIVariant( final String _name, final T _val )
	{
		this( _name, _val, null ) ;
	}
	
	public <T> UIVariant( final String _name, final T _val, Connect.Signal _signal )
	{
		variable = new ObjectVariable<T>( _name, _val ) ;
		signal = _signal ;
	}

	@Override
	public String getName()
	{
		return variable.getName() ;
	}

	public Connect.Signal getSignal()
	{
		return signal ;
	}

	@Override
	public int getType()
	{
		return variable.getType() ;
	}

	@Override
	public void setString( final String _value )
	{
		switch( getType() )
		{
			case AVariable.STRING_TYPE : ( ( StringVariable )variable ).value = _value ; break ;
		}
	}

	@Override
	public void setBool( final boolean _value )
	{
		switch( getType() )
		{
			case AVariable.BOOLEAN_TYPE : ( ( BooleanVariable )variable ).value = _value ; break ;
		}
	}

	@Override
	public void setFloat( final float _value )
	{
		switch( getType() )
		{
			case AVariable.FLOAT_TYPE : ( ( FloatVariable )variable ).value = _value ; break ;
		}
	}

	@Override
	public void setInt( final int _value )
	{
		switch( getType() )
		{
			case AVariable.INT_TYPE : ( ( IntegerVariable )variable ).value = _value ; break ;
		}
	}

	public void setObject( final Object _value )
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE :
			{
				( ( ObjectVariable )variable ).value = _value;
				break ;
			}
		}
	}
	
	@Override
	public void setVector3( final float _x, final float _y, final float _z )
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE :
			{
				final Vector3 value = ( Vector3 )( ( ObjectVariable )variable ).value ;
				value.setXYZ( _x, _y, _z ) ;
				break ;
			}
		}
	}

	@Override
	public void setVector2( final float _x, final float _y )
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE :
			{
				final Vector2 value = ( Vector2 )( ( ObjectVariable )variable ).value ;
				value.setXY( _x, _y ) ;
				break ;
			}
		}
	}

	@Override
	public void setColour( final byte _r, final byte _g, final byte _b, final byte _a )
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE :
			{
				final MalletColour value = ( MalletColour )( ( ObjectVariable )variable ).value ;
				value.changeColour( _r, _g, _b, _a ) ;
				break ;
			}
		}
	}
	
	@Override
	public String toString()
	{
		return variable.toString() ;
	}

	@Override
	public boolean toBool()
	{
		switch( getType() )
		{
			case AVariable.BOOLEAN_TYPE : return ( ( BooleanVariable )variable ).value ;
			default                     : return false ;
		}
	}

	@Override
	public float toFloat()
	{
		switch( getType() )
		{
			case AVariable.FLOAT_TYPE  : return ( ( FloatVariable )variable ).value ;
			default                    : return 0.0f ;
		}
	}

	@Override
	public int toInt()
	{
		switch( getType() )
		{
			case AVariable.INT_TYPE    : return ( ( IntegerVariable )variable ).value ;
			default                    : return 0 ;
		}
	}

	@Override
	public <T> T toObject( final Class<T> _class )
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE : return _class.cast( ( ( ObjectVariable )variable ).value ) ;
			default                    : return null ;
		}
	}
	
	@Override
	public Object toObject()
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE : return ( ( ObjectVariable )variable ).value ;
			default                    : return null ;
		}
	}

	@Override
	public Vector3 toVector3()
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE : return ( Vector3 )( ( ObjectVariable )variable ).value ;
			default                    : return null ;
		}
	}

	@Override
	public Vector2 toVector2()
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE : return ( Vector2 )( ( ObjectVariable )variable ).value ;
			default                    : return null ;
		}
	}

	@Override
	public MalletColour toColour()
	{
		switch( getType() )
		{
			case AVariable.OBJECT_TYPE : return ( MalletColour )( ( ObjectVariable )variable ).value ;
			default                    : return null ;
		}
	}
}
