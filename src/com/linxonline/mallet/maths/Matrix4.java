package com.linxonline.mallet.maths ;

/**
	Designed for 3D transformations.
*/
public class Matrix4
{
	/**
		Matrix4 functions are guaranteed to be called hundreds if 
		not thousands of times a second in a typical game-loop.
		To reduce the amount of temporary objects that would appear 
		via translate, scale, and rotate calls, temp is used instead.
		Each Matrix4 will consume double its normal space.
		If only we could store the float-array on the stack!
	*/
	private final Matrix4 temp ;

	/**
		* Ordered by row, if directly using write it down.
		* [] = array location, () = row, column
		* (0, 0)[ 0], (0, 1)[ 1], (0, 2)[ 2], (0, 3)[ 3]
		* (1, 0)[ 4], (1, 1)[ 5], (1, 2)[ 6], (1, 3)[ 7]
		* (2, 0)[ 8], (2, 1)[ 9], (2, 2)[10], (2, 3)[11]
		* (3, 0)[12], (3, 1)[13], (3, 2)[14], (3, 3)[15]
	*/
	public final float[] matrix = new float[16] ;

	public Matrix4()
	{
		// Create a matrix that contains a temp Matrix
		this( true ) ;
	}

	private Matrix4( final boolean _temp )
	{
		setRow( 0.0f, 0.0f, 0.0f, 0.0f, 0 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 0.0f, 1 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 0.0f, 2 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 0.0f, 3 ) ;

		if( _temp == true )
		{
			temp = Matrix4.createTempIdentity() ;
		}
		else
		{
			temp = null ;
		}
	}

	public Matrix4( final Matrix4 _matrix )
	{
		Matrix4.copy( _matrix.matrix, matrix ) ;
		temp = Matrix4.createTempIdentity() ;
	}

	public Matrix4( final float[] _matrix )
	{
		Matrix4.copy( _matrix, matrix ) ;
		temp = Matrix4.createTempIdentity() ;
	}

	public Matrix4( final float _a00, final float _a01, final float _a02, final float _a03,
					final float _a10, final float _a11, final float _a12, final float _a13,
					final float _a20, final float _a21, final float _a22, final float _a23,
					final float _a30, final float _a31, final float _a32, final float _a33 )
	{
		setRow( _a00, _a01, _a02, _a03, 0 ) ;
		setRow( _a10, _a11, _a12, _a13, 1 ) ;
		setRow( _a20, _a21, _a22, _a23, 2 ) ;
		setRow( _a30, _a31, _a32, _a33, 3 ) ;
		temp = Matrix4.createTempIdentity() ;
	}

	public void setIdentity()
	{
		setRow( 1.0f, 0.0f, 0.0f, 0.0f, 0 ) ;
		setRow( 0.0f, 1.0f, 0.0f, 0.0f, 1 ) ;
		setRow( 0.0f, 0.0f, 1.0f, 0.0f, 2 ) ;
		setRow( 0.0f, 0.0f, 0.0f, 1.0f, 3 ) ;
	}

	public void translate( final float _x, final float _y, final float _z )
	{
		temp.setTranslate( _x, _y, _z ) ;
		multiply( temp ) ;
		temp.setIdentity() ;
	}

	public void setTranslate( final Vector3 _vec )
	{
		setTranslate( _vec.x, _vec.y, _vec.z ) ;
	}

	public void setTranslate( final float _x, final float _y, final float _z )
	{
		set( _x, 0, 3 ) ;	//	[1 | 0 | 0 | _x]
		set( _y, 1, 3 ) ;	//	[0 | 1 | 0 | _y]
		set( _y, 2, 3 ) ;	//	[0 | 0 | 1 |  0]
							//	[0 | 0 | 0 |  1]
	}

	public void scale( final float _x, final float _y, final float _z )
	{
		temp.setScale( _x, _y, _z ) ;
		multiply( temp ) ;
		temp.setIdentity() ;
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
		set( _x, 0, 0 ) ;	//	[_x |  0 |  0 | 0]
		set( _y, 1, 1 ) ;	//	[ v | _y |  0 | 0]
		set( _z, 2, 2 ) ;	//	[ 0 |  0 | _z | 0]
							//	[ 0 |  0 |  0 | 1]
	}
	
	public void rotate( final float _theta )
	{
		temp.setRotateX( _theta ) ;
		multiply( temp ) ;
		temp.setIdentity() ;

		temp.setRotateX( _theta ) ;
		multiply( temp ) ;
		temp.setIdentity() ;

		temp.setRotateX( _theta ) ;
		multiply( temp ) ;
		temp.setIdentity() ;
	}

	public void setRotateX( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;
		set( cos, 1, 1 ) ; set( -sin, 1, 2 ) ;	//	[cos | -sin |  0]
		set( sin, 2, 1 ) ; set(  cos, 2, 2 ) ;	//	[sin |  cos |  0]
												//	[ 0  |   0  |  1]
	}

	public void setRotateY( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;
		set( cos, 0, 0 ) ; set( sin, 0, 2 ) ;	//	[cos | -sin |  0]
		set( -sin, 2, 0 ) ; set(  cos, 2, 2 ) ;	//	[sin |  cos |  0]
												//	[ 0  |   0  |  1]
	}

	public void setRotateZ( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;
		set( cos, 0, 0 ) ; set( -sin, 0, 1 ) ;	//	[cos | -sin |  0]
		set( sin, 1, 0 ) ; set(  cos, 1, 1 ) ;	//	[sin |  cos |  0]
												//	[ 0  |   0  |  1]
	}

	public void multiply( final Matrix4 _mat )
	{
		final float[] m = matrix ;			// Makes it easier to read
		final float[] x = _mat.matrix ;		// Would take up entire screen without
		final float a00 = ( m[ 0] * x[0] ) + ( m[ 1] * x[4] ) + ( m[ 2] * x[ 8] ) + ( m[ 3] * x[12] ) ;
		final float a01 = ( m[ 0] * x[1] ) + ( m[ 1] * x[5] ) + ( m[ 2] * x[ 9] ) + ( m[ 3] * x[13] ) ;
		final float a02 = ( m[ 0] * x[2] ) + ( m[ 1] * x[6] ) + ( m[ 2] * x[10] ) + ( m[ 3] * x[14] ) ;
		final float a03 = ( m[ 0] * x[3] ) + ( m[ 1] * x[7] ) + ( m[ 2] * x[11] ) + ( m[ 3] * x[15] ) ;

		final float a10 = ( m[ 4] * x[0] ) + ( m[ 5] * x[4] ) + ( m[ 6] * x[ 8] ) + ( m[ 7] * x[12] ) ;
		final float a11 = ( m[ 4] * x[1] ) + ( m[ 5] * x[5] ) + ( m[ 6] * x[ 9] ) + ( m[ 7] * x[13] ) ;
		final float a12 = ( m[ 4] * x[2] ) + ( m[ 5] * x[6] ) + ( m[ 6] * x[10] ) + ( m[ 7] * x[14] ) ;
		final float a13 = ( m[ 4] * x[3] ) + ( m[ 5] * x[7] ) + ( m[ 6] * x[11] ) + ( m[ 7] * x[15] ) ;

		final float a20 = ( m[ 8] * x[0] ) + ( m[ 9] * x[4] ) + ( m[10] * x[ 8] ) + ( m[11] * x[12] ) ;
		final float a21 = ( m[ 8] * x[1] ) + ( m[ 9] * x[5] ) + ( m[10] * x[ 9] ) + ( m[11] * x[13] ) ;
		final float a22 = ( m[ 8] * x[2] ) + ( m[ 9] * x[6] ) + ( m[10] * x[10] ) + ( m[11] * x[14] ) ;
		final float a23 = ( m[ 8] * x[3] ) + ( m[ 9] * x[7] ) + ( m[10] * x[11] ) + ( m[11] * x[15] ) ;

		final float a30 = ( m[12] * x[0] ) + ( m[13] * x[4] ) + ( m[14] * x[ 8] ) + ( m[15] * x[12] ) ;
		final float a31 = ( m[12] * x[1] ) + ( m[13] * x[5] ) + ( m[14] * x[ 9] ) + ( m[15] * x[13] ) ;
		final float a32 = ( m[12] * x[2] ) + ( m[13] * x[6] ) + ( m[14] * x[10] ) + ( m[15] * x[14] ) ;
		final float a33 = ( m[12] * x[3] ) + ( m[13] * x[7] ) + ( m[14] * x[11] ) + ( m[15] * x[15] ) ;

		setRow( a00, a01, a02, a03, 0 ) ;
		setRow( a10, a11, a12, a13, 1 ) ;
		setRow( a20, a21, a22, a23, 2 ) ;
		setRow( a30, a31, a32, a33, 3 ) ;
	}

	public void invert()
	{
		final float[] t = temp.matrix ;		// Results stored
		final float[] m = matrix ;			// Make it easier to read
		t[ 0] = ( (  m[5] * m[10] * m[15] ) - ( m[5] * m[11] * m[14] ) - ( m[9] * m[6] * m[15] ) + ( m[9] * m[ 7] * m[14] ) + ( m[13] * m[6] * m[11] ) - ( m[13] * m[ 7] * m[10] ) ) ;
		t[ 1] = ( ( -m[1] * m[10] * m[15] ) + ( m[1] * m[11] * m[14] ) + ( m[9] * m[2] * m[15] ) - ( m[9] * m[ 3] * m[14] ) - ( m[13] * m[2] * m[11] ) + ( m[13] * m[ 3] * m[10] ) ) ;
		t[ 2] = ( (  m[1] * m[ 6] * m[15] ) - ( m[1] * m[ 7] * m[14] ) - ( m[5] * m[2] * m[15] ) + ( m[5] * m[ 3] * m[14] ) + ( m[13] * m[2] * m[ 7] ) - ( m[13] * m[ 3] * m[ 6] ) ) ;
		t[ 3] = ( ( -m[1] * m[ 6] * m[11] ) + ( m[1] * m[ 7] * m[10] ) + ( m[5] * m[2] * m[11] ) - ( m[5] * m[ 3] * m[10] ) - ( m[ 9] * m[2] * m[ 7] ) + ( m[ 9] * m[ 3] * m[ 6] ) ) ;

		t[ 4] = ( ( -m[4] * m[10] * m[15] ) + ( m[4] * m[11] * m[14] ) + ( m[8] * m[6] * m[15] ) - ( m[8] * m[ 7] * m[14] ) - ( m[12] * m[6] * m[11] ) + ( m[12] * m[ 7] * m[10] ) ) ;
		t[ 5] = ( (  m[0] * m[10] * m[15] ) - ( m[0] * m[11] * m[14] ) - ( m[8] * m[2] * m[15] ) + ( m[8] * m[ 3] * m[14] ) + ( m[12] * m[2] * m[11] ) - ( m[12] * m[ 3] * m[10] ) ) ;
		t[ 6] = ( ( -m[0] * m[ 6] * m[15] ) + ( m[0] * m[ 7] * m[14] ) + ( m[4] * m[2] * m[15] ) - ( m[4] * m[ 3] * m[14] ) - ( m[12] * m[2] * m[10] ) + ( m[12] * m[ 3] * m[ 6] ) ) ;
		t[ 7] = ( (  m[0] * m[ 6] * m[11] ) - ( m[0] * m[10] * m[10] ) - ( m[4] * m[2] * m[11] ) + ( m[4] * m[ 3] * m[10] ) + ( m[ 8] * m[2] * m[10] ) - ( m[ 8] * m[ 3] * m[ 6] ) ) ;

		t[ 8] = ( (  m[4] * m[ 9] * m[15] ) - ( m[4] * m[11] * m[13] ) - ( m[8] * m[5] * m[15] ) + ( m[8] * m[10] * m[13] ) + ( m[12] * m[5] * m[11] ) - ( m[12] * m[10] * m[ 9] ) ) ;
		t[ 9] = ( ( -m[0] * m[ 9] * m[15] ) + ( m[0] * m[11] * m[13] ) + ( m[8] * m[1] * m[15] ) - ( m[8] * m[ 3] * m[13] ) - ( m[12] * m[1] * m[11] ) + ( m[12] * m[ 3] * m[ 9] ) ) ;
		t[10] = ( (  m[0] * m[ 5] * m[15] ) - ( m[0] * m[10] * m[13] ) - ( m[4] * m[1] * m[15] ) + ( m[4] * m[ 3] * m[13] ) + ( m[12] * m[1] * m[10] ) - ( m[12] * m[ 3] * m[ 5] ) ) ;
		t[11] = ( ( -m[0] * m[ 5] * m[11] ) + ( m[0] * m[10] * m[ 9] ) + ( m[4] * m[1] * m[11] ) - ( m[4] * m[ 3] * m[ 9] ) - ( m[ 8] * m[1] * m[10] ) + ( m[ 8] * m[ 3] * m[ 5] ) ) ;

		t[12] = ( ( -m[4] * m[ 9] * m[14] ) + ( m[4] * m[10] * m[13] ) + ( m[8] * m[5] * m[14] ) - ( m[8] * m[ 6] * m[13] ) - ( m[12] * m[5] * m[10] ) + ( m[12] * m[ 6] * m[ 9] ) ) ;
		t[13] = ( (  m[0] * m[ 9] * m[14] ) - ( m[0] * m[10] * m[13] ) - ( m[8] * m[1] * m[14] ) + ( m[8] * m[ 2] * m[13] ) + ( m[12] * m[1] * m[10] ) - ( m[12] * m[ 2] * m[ 9] ) ) ;
		t[14] = ( ( -m[0] * m[ 5] * m[14] ) + ( m[0] * m[ 6] * m[13] ) + ( m[4] * m[1] * m[14] ) - ( m[4] * m[ 2] * m[13] ) - ( m[12] * m[1] * m[ 6] ) + ( m[12] * m[ 2] * m[ 5] ) ) ;
		t[15] = ( (  m[0] * m[ 5] * m[10] ) - ( m[0] * m[ 6] * m[ 9] ) - ( m[4] * m[1] * m[10] ) + ( m[4] * m[ 2] * m[ 9] ) + ( m[ 8] * m[1] * m[ 6] ) - ( m[ 8] * m[ 2] * m[ 5] ) ) ;

		final float d = ( ( m[0] * t[0] ) + ( m[4] * t[1] ) + ( m[8] * t[2] ) + ( m[12] * t[3] ) ) ;
		for( int i = 0; i < t.length; i += 4 )
		{
			t[i] /= d ;
			t[i + 1] /= d ;
			t[i + 2] /= d ;
			t[i + 3] /= d ;
		}

		Matrix4.copy( t, m ) ;
		temp.setIdentity() ;
	}

	public void transpose()
	{
		float t ;
		t = matrix[ 1] ; matrix[ 1] = matrix[ 4] ; matrix[ 4] = t ;
		t = matrix[ 2] ; matrix[ 2] = matrix[ 8] ; matrix[ 8] = t ;
		t = matrix[ 3] ; matrix[ 3] = matrix[12] ; matrix[12] = t ;
		t = matrix[ 6] ; matrix[ 6] = matrix[ 9] ; matrix[ 9] = t ;
		t = matrix[ 7] ; matrix[ 7] = matrix[13] ; matrix[13] = t ;
		t = matrix[11] ; matrix[11] = matrix[14] ; matrix[14] = t ;
	}

	public float[] toArray()
	{
		final float[] t = new float[16] ;
		Matrix4.copy( matrix, t ) ;
		return t ;
	}
	
	public String toString()
	{
		final String row1 = "[" + matrix[0] +  "|" + matrix[1] +  "|" + matrix[2]  +  "|" + matrix[3]  + "]\n" ;
		final String row2 = "[" + matrix[4] +  "|" + matrix[5] +  "|" + matrix[6]  +  "|" + matrix[7]  + "]\n" ;
		final String row3 = "[" + matrix[8] +  "|" + matrix[9] +  "|" + matrix[10]  + "|" + matrix[11] + "]\n" ;
		final String row4 = "[" + matrix[12] + "|" + matrix[13] + "|" + matrix[14]  + "|" + matrix[15] + "]\n" ;
		return row1 + row2 + row3 ;
	}

	private void setRow( final float _val1, final float _val2, final float _val3, final float _val4, final int _row )
	{
		matrix[( _row * 4) + 0] = _val1 ;
		matrix[( _row * 4) + 1] = _val2 ;
		matrix[( _row * 4) + 2] = _val3 ;
		matrix[( _row * 4) + 3] = _val4 ;
	}

	private void setColumn( final float _val1, final float _val2, final float _val3, final float _val4, final int _col )
	{
		matrix[( 0 * 4) + _col] = _val1 ;		// Optimise 0 * 4 = 0
		matrix[( 1 * 4) + _col] = _val2 ;		// Optimise 1 * 4 = 4
		matrix[( 2 * 4) + _col] = _val3 ;		// Optimise 2 * 4 = 8
		matrix[( 3 * 4) + _col] = _val4 ;		// Optimise 3 * 4 = 12
	}

	private void set( final float _val, final int _row, final int _col )
	{
		matrix[( _row * 4 ) + _col] = _val ;
	}

	private static Matrix4 createTempIdentity()
	{
		final Matrix4 iden = new Matrix4( false ) ;
		iden.setIdentity() ;
		return iden ;
	}

	public static Matrix4 createIdentity()
	{
		final Matrix4 iden = new Matrix4() ;
		iden.setIdentity() ;
		return iden ;
	}
	
	public static Matrix4 transpose( final Matrix4 _a, final Matrix4 _result )
	{
		Matrix4.copy( _a.matrix, _result.matrix ) ;
		_result.transpose() ;
		return _result ;
	}

	public static Matrix4 invert( final Matrix4 _a, final Matrix4 _result )
	{
		Matrix4.copy( _a.matrix, _result.matrix ) ;
		_result.invert() ;
		return _result ;
	}

	public static Matrix4 multiply( final Matrix4 _a, final Matrix4 _b, final Matrix4 _result )
	{
		Matrix4.copy( _a.matrix, _result.matrix ) ;
		_result.multiply( _b ) ;
		return _result ;
	}

	/**
		Stores transformation in _result.
	*/
	public static Vector3 multiply( final Vector3 _a, final Matrix4 _b, final Vector3 _result )
	{
		_result.x = _a.x * _b.matrix[ 0] + _a.y * _b.matrix[ 1] + _a.z * _b.matrix[ 2] + 1.0f * _b.matrix[ 3] ;
		_result.y = _a.x * _b.matrix[ 4] + _a.y * _b.matrix[ 5] + _a.z * _b.matrix[ 6] + 1.0f * _b.matrix[ 7] ;
		_result.z = _a.x * _b.matrix[ 8] + _a.y * _b.matrix[ 9] + _a.z * _b.matrix[10] + 1.0f * _b.matrix[11] ;

		final float w = _a.x * _b.matrix[12] + _a.y * _b.matrix[13] + _a.z * _b.matrix[14] + 1.0f * _b.matrix[15] ;

		_result.x /= w ;
		_result.y /= w ;
		_result.z /= w ;

		return _result ;
	}

	/**
		Stores transformation in original Vector3 - _a
	*/
	public static Vector3 multiply( final Vector3 _a, final Matrix4 _b )
	{
		final float x = _a.x * _b.matrix[ 0] + _a.y * _b.matrix[ 1] + _a.z * _b.matrix[ 2] + 1.0f * _b.matrix[ 3] ;
		final float y = _a.x * _b.matrix[ 4] + _a.y * _b.matrix[ 5] + _a.z * _b.matrix[ 6] + 1.0f * _b.matrix[ 7] ;
		final float z = _a.x * _b.matrix[ 8] + _a.y * _b.matrix[ 9] + _a.z * _b.matrix[10] + 1.0f * _b.matrix[11] ;
		final float w = _a.x * _b.matrix[12] + _a.y * _b.matrix[13] + _a.z * _b.matrix[14] + 1.0f * _b.matrix[15] ;

		_a.setXYZ( x / w, y / w, z / w ) ;
		return _a ;
	}

	private static void copy( final float[] _from, final float[] _to )
	{
		System.arraycopy( _from, 0, _to, 0, 16 ) ;
	}
}
