package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.Colour ;

public interface IVariant
{
	public void setName( String _name ) ;
	public String getName() ;

	/**
		Return the type of object that is being stored 
		by this Variant.
	*/
	public int getType() ;

	public void setString( final String _value ) ;
	public void setBool( final boolean _value ) ;
	public void setFloat( final float _value ) ;
	public void setInt( final int _value ) ;
	public void setObject( final Object _value ) ;
	public void setVector3( final float _x, final float _y, final float _z ) ;
	public void setVector2( final float _x, final float _y ) ;
	public void setColour( final byte _r, final byte _g, final byte _b, final byte _a ) ;
	public void setEnum( final Enum _value ) ;

	public String toString() ;
	public boolean toBool() ;
	public float toFloat() ;
	public int toInt() ;
	public <T> T toObject( final Class<T> _class ) ;
	public Object toObject() ;
	public Vector3 toVector3() ;
	public Vector2 toVector2() ;
	public Colour toColour() ;
	public Enum toEnum() ;
	public <E> E toEnum( final Class<E> _class ) ;

	public Connect.Signal getSignal() ;
}
