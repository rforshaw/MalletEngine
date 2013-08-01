package com.linxonline.mallet.maths ;

public class Matrix3
{
	/**
		* Ordered by row, if directly using write it down.
		* [] = array location, () = row, column
		* (0, 0)[0], (0, 1)[1], (0, 2)[2]
		* (1, 0)[3], (0, 1)[4], (0, 2)[5]
		* (2, 0)[6], (0, 1)[7], (0, 2)[8]
	*/
	public final float[] matrix = new float[9] ;

	public Matrix3()
	{
		setRow( 0.0f, 0.0f, 0.0f, 0 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 1 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 2 ) ;
	}

	public Matrix3( final Matrix3 _matrix )
	{
		Matrix3.copy( _matrix.matrix, matrix ) ;
	}
	
	public Matrix3( final float[] _matrix )
	{
		Matrix3.copy( _matrix, matrix ) ;
	}

	public Matrix3( final Vector3 _row1, final Vector3 _row2, final Vector3 _row3 )
	{
		setRow( _row1.x, _row1.y, _row1.z, 0 ) ;
		setRow( _row2.x, _row2.y, _row2.z, 1 ) ;
		setRow( _row3.x, _row3.y, _row3.z, 2 ) ;
	}

	public Matrix3( final float _a00, final float _a01, final float _a02, 
					final float _a10, final float _a11, final float _a12,
					final float _a20, final float _a21, final float _a22 )
	{
		setRow( _a00, _a01, _a02, 0 ) ;
		setRow( _a10, _a11, _a12, 1 ) ;
		setRow( _a20, _a21, _a22, 2 ) ;
	}

	public void setIdentity()
	{
		setRow( 1.0f, 0.0f, 0.0f, 0 ) ;
		setRow( 0.0f, 1.0f, 0.0f, 1 ) ;
		setRow( 0.0f, 0.0f, 1.0f, 2 ) ;
	}

	public void setScale( final float _scale )
	{
		setScale( _scale, _scale, _scale ) ;
	}

	public void setScale( final Vector3 _vec )
	{
		setScale( _vec.x, _vec.y, _vec.z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		set( _x, 0, 0 ) ;	//	_x |  v |  v
		set( _y, 1, 1 ) ;	//	 v | _y |  v
		set( _z, 2, 2 ) ;	//	 v |  v | _z
	}

	public void multiply( final Matrix3 _mat )
	{
		final float a00 = ( matrix[0] * _mat.matrix[0] ) + ( matrix[1] * _mat.matrix[3] ) + ( matrix[2] * _mat.matrix[6] ) ;
		final float a01 = ( matrix[0] * _mat.matrix[1] ) + ( matrix[1] * _mat.matrix[4] ) + ( matrix[2] * _mat.matrix[7] ) ;
		final float a02 = ( matrix[0] * _mat.matrix[2] ) + ( matrix[1] * _mat.matrix[5] ) + ( matrix[2] * _mat.matrix[8] ) ;

		final float a10 = ( matrix[3] * _mat.matrix[0] ) + ( matrix[4] * _mat.matrix[3] ) + ( matrix[5] * _mat.matrix[6] ) ;
		final float a11 = ( matrix[3] * _mat.matrix[1] ) + ( matrix[4] * _mat.matrix[4] ) + ( matrix[5] * _mat.matrix[7] ) ;
		final float a12 = ( matrix[3] * _mat.matrix[2] ) + ( matrix[4] * _mat.matrix[5] ) + ( matrix[5] * _mat.matrix[8] ) ;

		final float a20 = ( matrix[6] * _mat.matrix[0] ) + ( matrix[7] * _mat.matrix[3] ) + ( matrix[8] * _mat.matrix[6] ) ;
		final float a21 = ( matrix[6] * _mat.matrix[1] ) + ( matrix[7] * _mat.matrix[4] ) + ( matrix[8] * _mat.matrix[7] ) ;
		final float a22 = ( matrix[6] * _mat.matrix[2] ) + ( matrix[7] * _mat.matrix[5] ) + ( matrix[8] * _mat.matrix[8] ) ;

		setRow( a00, a01, a02, 0 ) ;
		setRow( a10, a11, a12, 1 ) ;
		setRow( a20, a21, a22, 2 ) ;
	}

	public void add( final Matrix3 _mat )
	{
		final float[] t = new float[9] ;
		t[0] = matrix[0] + _mat.matrix[0] ; t[1] = matrix[1] + _mat.matrix[1] ; t[2] = matrix[2] + _mat.matrix[2] ;
		t[3] = matrix[3] + _mat.matrix[3] ; t[4] = matrix[4] + _mat.matrix[4] ; t[5] = matrix[5] + _mat.matrix[5] ;
		t[6] = matrix[6] + _mat.matrix[6] ; t[7] = matrix[7] + _mat.matrix[7] ; t[8] = matrix[8] + _mat.matrix[8] ;
		copy( t, matrix ) ;
	}
	
	public void subtract( final Matrix3 _mat )
	{
		final float[] t = new float[9] ;
		t[0] = matrix[0] - _mat.matrix[0] ; t[1] = matrix[1] - _mat.matrix[1] ; t[2] = matrix[2] - _mat.matrix[2] ;
		t[3] = matrix[3] - _mat.matrix[3] ; t[4] = matrix[4] - _mat.matrix[4] ; t[5] = matrix[5] - _mat.matrix[5] ;
		t[6] = matrix[6] - _mat.matrix[6] ; t[7] = matrix[7] - _mat.matrix[7] ; t[8] = matrix[8] - _mat.matrix[8] ;
		copy( t, matrix ) ;
	}
	
	public void invert()
	{
		final float d = ( ( matrix[6] * matrix[1] * matrix[5] ) - ( matrix[6] * matrix[2] * matrix[4] ) - 
						 ( matrix[3] * matrix[1] * matrix[8] ) + ( matrix[3] * matrix[2] * matrix[7] ) + 
						 ( matrix[0] * matrix[4] * matrix[8] ) - ( matrix[0] * matrix[5] * matrix[7] ) ) ;

		final float a00 =  ( (  matrix[4] * matrix[8] ) - ( matrix[5] * matrix[7] ) ) / d ;
		final float a01 = -( (  matrix[1] * matrix[8] ) - ( matrix[2] * matrix[7] ) ) / d ;
		final float a02 =  ( (  matrix[1] * matrix[5] ) - ( matrix[2] * matrix[4] ) ) / d ;

		final float a10 = -( ( -matrix[6] * matrix[5] ) + ( matrix[3] * matrix[8] ) ) / d ;
		final float a11 =  ( ( -matrix[6] * matrix[2] ) + ( matrix[0] * matrix[8] ) ) / d ;
		final float a12 =  ( ( -matrix[3] * matrix[2] ) + ( matrix[0] * matrix[5] ) ) / d ;

		final float a20 =  ( ( -matrix[6] * matrix[2] ) + ( matrix[0] * matrix[5] ) ) / d ;
		final float a21 = -( ( -matrix[6] * matrix[1] ) + ( matrix[0] * matrix[7] ) ) / d ;
		final float a22 =  ( ( -matrix[3] * matrix[1] ) + ( matrix[0] * matrix[4] ) ) / d ;
	}

	public void transpose()
	{
		float t ;
		t = matrix[1] ; matrix[1] = matrix[3] ; matrix[3] = t ;
		t = matrix[2] ; matrix[2] = matrix[6] ; matrix[6] = t ;
		t = matrix[5] ; matrix[5] = matrix[7] ; matrix[7] = t ;
	}
	
	public float determinant()
	{
		return ( matrix[0] * matrix[4] * matrix[8] ) - ( matrix[0] * matrix[5] * matrix[7] ) + ( matrix[3] * matrix[7] * matrix[2] ) - 
			   ( matrix[0] * matrix[1] * matrix[8] ) + ( matrix[6] * matrix[1] * matrix[4] ) - ( matrix[6] * matrix[4] * matrix[2] ) ;
	}

	public float[] toArray()
	{
		final float[] t = new float[9] ;
		copy( matrix, t ) ;
		return t ;
	}
	
	public String toString()
	{
		final String row1 = "[" + matrix[0] + "|" + matrix[1] + "|" + matrix[2]  + "]\n" ;
		final String row2 = "[" + matrix[3] + "|" + matrix[4] + "|" + matrix[5]  + "]\n" ;
		final String row3 = "[" + matrix[6] + "|" + matrix[7] + "|" + matrix[8]  + "]\n" ;
		return row1 + row2 + row3 ;
	}

	private void setRow( final float _val1, final float _val2, final float _val3, final int _row )
	{
		matrix[( _row * 3) + 0] = _val1 ;
		matrix[( _row * 3) + 1] = _val2 ;
		matrix[( _row * 3) + 2] = _val3 ;
	}

	private void setColumn( final float _val1, final float _val2, final float _val3, final int _col )
	{
		matrix[( 0 * 3) + _col] = _val1 ;		// Optimise 0 * 3 = 0
		matrix[( 1 * 3) + _col] = _val2 ;		// Optimise 1 * 3 = 3
		matrix[( 2 * 3) + _col] = _val3 ;		// Optimise 2 * 3 = 4
	}

	private void set( final float _val, final int _row, final int _col )
	{
		matrix[( _row * 3) + _col] = _val ;
	}
	
	public static Matrix3 transpose( final Matrix3 _a, final Matrix3 _result )
	{
		copy( _a.matrix, _result.matrix ) ;
		_result.transpose() ;
		return _result ;
	}

	public static Matrix3 invert( final Matrix3 _a, final Matrix3 _result )
	{
		copy( _a.matrix, _result.matrix ) ;
		_result.invert() ;
		return _result ;
	}

	public static Matrix3 multiply( final Matrix3 _a, final Matrix3 _b, final Matrix3 _result )
	{
		copy( _a.matrix, _result.matrix ) ;
		_result.multiply( _b ) ;
		return _result ;
	}

	public static Vector3 multiply( final Vector3 _a, final Matrix3 _b, final Vector3 _result )
	{
		_result.x = ( _a.x * _b.matrix[0] ) + ( _a.y * _b.matrix[1] ) + ( _a.y * _b.matrix[2] ) ;
		_result.y = ( _a.x * _b.matrix[3] ) + ( _a.y * _b.matrix[4] ) + ( _a.y * _b.matrix[5] ) ;
		_result.z = ( _a.x * _b.matrix[6] ) + ( _a.y * _b.matrix[7] ) + ( _a.y * _b.matrix[8] ) ;

		return _result ;
	}

	public static Matrix3 add( final Matrix3 _a, final Matrix3 _b, final Matrix3 _result )
	{
		copy( _a.matrix, _result.matrix ) ;
		_result.add( _b ) ;
		return _result ;
	}
	
	public static Matrix3 subtract( final Matrix3 _a, final Matrix3 _b, final Matrix3 _result )
	{
		copy( _a.matrix, _result.matrix ) ;
		_result.subtract( _b ) ;
		return _result ;
	}

	private static void copy( final float[] _from, final float[] _to )
	{
		System.arraycopy( _from, 0, _to, 0, _to.length ) ;
	}
}