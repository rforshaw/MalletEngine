package com.linxonline.mallet.util.buffers ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class FloatBuffer
{
	private FloatBuffer() {}

	public static float[] allocate( final int _size )
	{
		return new float[_size] ;
	}

	public static float[] expand( final float[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final float[] to = new float[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}

	public static void copy( final float[] _from, final float[] _to )
	{
		// A copy is an expand without increasing the array size.
		System.arraycopy( _from, 0, _to, 0, _from.length ) ;
	}

	public static Vector2 set( final float[] _modify, final int _index, final Vector2 _val )
	{
		_modify[_index + 0] = _val.x ;
		_modify[_index + 1] = _val.y ;
		return _val ;
	}

	public static Vector3 set( final float[] _modify, final int _index, final Vector3 _val )
	{
		_modify[_index + 0] = _val.x ;
		_modify[_index + 1] = _val.y ;
		_modify[_index + 2] = _val.z ;
		return _val ;
	}

	public static void set( final float[] _modify, final int _index, final float _val )
	{
		_modify[_index] = _val ;
	}
	
	public static void set( final float[] _modify,
							final int _index,
							final float _x,
							final float _y )
	{
		_modify[_index + 0] = _x ;
		_modify[_index + 1] = _y ;
	}

	public static void set( final float[] _modify,
							final int _index,
							final float _x,
							final float _y,
							final float _z )
	{
		_modify[_index + 0] = _x ;
		_modify[_index + 1] = _y ;
		_modify[_index + 2] = _z ;
	}

	public static void add( final float[] _modify,
							final int _index,
							final float _x,
							final float _y )
	{
		_modify[_index + 0] += _x ;
		_modify[_index + 1] += _y ;
	}

	public static void add( final float[] _modify,
							final int _index,
							final float _x,
							final float _y,
							final float _z )
	{
		_modify[_index + 0] += _x ;
		_modify[_index + 1] += _y ;
		_modify[_index + 2] += _z ;
	}

	public static void add( final float[] _modify,
							final int _index,
							final float _x,
							final float _y,
							final float _z,
							final float _w )
	{
		_modify[_index + 0] += _x ;
		_modify[_index + 1] += _y ;
		_modify[_index + 2] += _z ;
		_modify[_index + 3] += _w ;
	}

	public static float get( final float[] _get, final int _index )
	{
		return _get[_index] ;
	}

	public static Vector3 fill( final float[] _get, final Vector3 _fill, final int _at )
	{
		_fill.x = _get[_at] ;
		_fill.y = _get[_at + 1] ;
		_fill.z = _get[_at + 2] ;
		return _fill ;
	}

	public static Vector2 fill( final float[] _get, final Vector2 _fill, final int _at )
	{
		_fill.x = _get[_at] ;
		_fill.y = _get[_at + 1] ;
		return _fill ;
	}

	public static void swap( final float[] _set, final int _lhs, final int _rhs )
	{
		final float t = _set[_lhs] ;
		_set[_lhs] = _set[_rhs] ;
		_set[_rhs] = t ;
	}

	public static float multiply( final float[] _get, final int _lhs, final float _val )
	{
		return _get[_lhs] * _val ;
	}

	public static float multiply( final float[] _get, final int _lhs, final int _rhs )
	{
		return _get[_lhs] * _get[_rhs] ;
	}

	public static float multiply( final float[] _get, final int _lhs, final int _mhs, final int _rhs )
	{
		return _get[_lhs] * _get[_mhs] * _get[_rhs] ;
	}

	public static float multiply( final float[] _getLHS, final int _lhs,
								  final float[] _getRHS, final int _rhs )
	{
		return _getLHS[_lhs] * _getRHS[_rhs] ;
	}

	public static void divide( final float[] _set, final int _lhs, final float _val )
	{
		_set[_lhs] /= _val ;
	}
}
