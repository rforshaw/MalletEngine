package com.linxonline.mallet.util.buffers ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public interface IFloatBuffer
{
	public int size() ;

	public float set( final int _index, final float _val ) ;
	public Vector2 set( final int _index, final Vector2 _val ) ;
	public Vector3 set( final int _index, final Vector3 _val ) ;

	public void set( final int _index, final float _x, final float _y ) ;
	public void set( final int _index, final float _x, final float _y, final float _z ) ;

	public float get( final int _index ) ;

	public Vector3 fill( final Vector3 _fill, final int _at ) ; 
	public Vector2 fill( final Vector2 _fill, final int _at ) ;

	public void swap( final int _lhs, final int _rhs ) ;
	public float multiply( final int _lhs, final float _val ) ;
	public float multiply( final int _lhs, final int _rhs ) ;
	public float multiply( final int _lhs, final int _mhs, final int _rhs ) ;
	public float multiply( final int _lhs, final FloatBuffer _buffer, final int _rhs ) ;

	public void divide( final int _lhs, final float _val ) ;
}
