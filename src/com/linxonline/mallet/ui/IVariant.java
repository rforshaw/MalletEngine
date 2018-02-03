package com.linxonline.mallet.ui ;

import com.linxonline.mallet.maths.* ;

public interface IVariant
{
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
	public void setVector3( final float _x, final float _y, final float _z ) ;
	public void setVector2( final float _x, final float _y ) ;

	public String toString() ;
	public boolean toBool() ;
	public float toFloat() ;
	public int toInt() ;
	public Vector3 toVector3() ;
	public Vector2 toVector2() ;
}
