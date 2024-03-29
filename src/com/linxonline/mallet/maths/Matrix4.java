package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.renderer.FloatUniform ;

/**
	Designed for 3D transformations.
*/
public final class Matrix4 extends FloatUniform
{
	private static final float[] IDENTITY = new float[]
	{
		1.0f,
		0.0f,
		0.0f,
		0.0f,

		0.0f,
		1.0f,
		0.0f,
		0.0f,

		0.0f,
		0.0f,
		1.0f,
		0.0f,

		0.0f,
		0.0f,
		0.0f,
		1.0f
	} ;

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
	public final float[] matrix = FloatBuffer.allocate( 16 ) ;

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

		temp = ( _temp == true ) ? Matrix4.createTempIdentity() : null ;
	}

	public Matrix4( final Matrix4 _matrix )
	{
		Matrix4.copy( _matrix.matrix, matrix ) ;
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

	public void set( final float _a00, final float _a01, final float _a02, final float _a03,
					 final float _a10, final float _a11, final float _a12, final float _a13,
					 final float _a20, final float _a21, final float _a22, final float _a23,
					 final float _a30, final float _a31, final float _a32, final float _a33 )
	{
		setRow( _a00, _a01, _a02, _a03, 0 ) ;
		setRow( _a10, _a11, _a12, _a13, 1 ) ;
		setRow( _a20, _a21, _a22, _a23, 2 ) ;
		setRow( _a30, _a31, _a32, _a33, 3 ) ;
	}

	public void set( final Matrix4 _from )
	{
		Matrix4.copy( _from.matrix, matrix ) ;
	}

	public void setIdentity()
	{
		System.arraycopy( IDENTITY, 0, matrix, 0, 16 ) ;
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
		matrix[3] = _x ;
		matrix[7] = _y ;
		matrix[11] = _z ;

		//set( _x, 0, 3 ) ;	//	[1 | 0 | 0 | _x]
		//set( _y, 1, 3 ) ;	//	[0 | 1 | 0 | _y]
		//set( _z, 2, 3 ) ;	//	[0 | 0 | 1 | _z]
							//	[0 | 0 | 0 |  1]
	}

	public void setX( final float _x )
	{
		matrix[3] = _x ;
	}

	public void setY( final float _y )
	{
		matrix[7] = _y ;
	}

	public void setZ( final float _z )
	{
		matrix[11] = _z ;
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
		matrix[0] = _x ;
		matrix[5] = _y ;
		matrix[10] = _z ;
	
		//set( _x, 0, 0 ) ;	//	[_x |  0 |  0 | 0]
		//set( _y, 1, 1 ) ;	//	[ v | _y |  0 | 0]
		//set( _z, 2, 2 ) ;	//	[ 0 |  0 | _z | 0]
							//	[ 0 |  0 |  0 | 1]
	}
	
	public void rotate( final float _theta )
	{
		rotate( _theta, 1.0f, 1.0f, 1.0f ) ;
	}

	public void rotate( final float _theta, final float _x, final float _y, final float _z )
	{
		temp.setRotateX( _theta * _x ) ;
		multiply( temp ) ;
		temp.setIdentity() ;

		temp.setRotateY( _theta * _y ) ;
		multiply( temp ) ;
		temp.setIdentity() ;

		temp.setRotateZ( _theta * _z ) ;
		multiply( temp ) ;
		temp.setIdentity() ;
	}

	public void setRotateX( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;

		matrix[5] = cos ;
		matrix[6] = -sin ;

		matrix[9] = sin ;
		matrix[10] = cos ;

		//	[cos | -sin |  0]
		//	[sin |  cos |  0]
		//	[ 0  |   0  |  1]
	}

	public void setRotateY( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;

		matrix[0] = cos ;
		matrix[2] = sin ;

		matrix[8] = -sin ;
		matrix[10] = cos ;

		//	[cos | -sin |  0]
		//	[sin |  cos |  0]
		//	[ 0  |   0  |  1]
	}

	public void setRotateZ( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;

		matrix[0] = cos ;
		matrix[1] = -sin ;

		matrix[4] = sin ;
		matrix[5] = cos ;

		//	[cos | -sin |  0]
		//	[sin |  cos |  0]
		//	[ 0  |   0  |  1]
	}

	public void multiply( final Matrix4 _mat )
	{
		final float[] m = this.matrix ;				// Makes it easier to read
		final float[] x = _mat.matrix ;
		
		{
			final float a00 = m[0] * x[0] + m[1] * x[4] + m[2] * x[ 8] + m[3] * x[12] ;
			final float a01 = m[0] * x[1] + m[1] * x[5] + m[2] * x[ 9] + m[3] * x[13] ;
			final float a02 = m[0] * x[2] + m[1] * x[6] + m[2] * x[10] + m[3] * x[14] ;
			final float a03 = m[0] * x[3] + m[1] * x[7] + m[2] * x[11] + m[3] * x[15] ;

			m[0] = a00 ;
			m[1] = a01 ;
			m[2] = a02 ;
			m[3] = a03 ;
		}

		{
			final float a10 = m[4] * x[0] + m[5] * x[4] + m[6] * x[ 8] + m[7] * x[12] ;
			final float a11 = m[4] * x[1] + m[5] * x[5] + m[6] * x[ 9] + m[7] * x[13] ;
			final float a12 = m[4] * x[2] + m[5] * x[6] + m[6] * x[10] + m[7] * x[14] ;
			final float a13 = m[4] * x[3] + m[5] * x[7] + m[6] * x[11] + m[7] * x[15] ;

			m[4] = a10 ;
			m[5] = a11 ;
			m[6] = a12 ;
			m[7] = a13 ;
		}

		{
			final float a20 = m[8] * x[0] + m[9] * x[4] + m[10] * x[ 8] + m[11] * x[12] ;
			final float a21 = m[8] * x[1] + m[9] * x[5] + m[10] * x[ 9] + m[11] * x[13] ;
			final float a22 = m[8] * x[2] + m[9] * x[6] + m[10] * x[10] + m[11] * x[14] ;
			final float a23 = m[8] * x[3] + m[9] * x[7] + m[10] * x[11] + m[11] * x[15] ;

			m[8]  = a20 ;
			m[9]  = a21 ;
			m[10] = a22 ;
			m[11] = a23 ;
		}

		{
			final float a30 = m[12] * x[0] + m[13] * x[4] + m[14] * x[ 8] + m[15] * x[12] ;
			final float a31 = m[12] * x[1] + m[13] * x[5] + m[14] * x[ 9] + m[15] * x[13] ;
			final float a32 = m[12] * x[2] + m[13] * x[6] + m[14] * x[10] + m[15] * x[14] ;
			final float a33 = m[12] * x[3] + m[13] * x[7] + m[14] * x[11] + m[15] * x[15] ;

			m[12] = a30 ;
			m[13] = a31 ;
			m[14] = a32 ;
			m[15] = a33 ;
		}
	}

	public void invert()
	{
		final float[] t = temp.matrix ;		// Results stored
		final float[] m = matrix ;			// Make it easier to read

		Matrix4.invertStage1( t, m ) ;
		Matrix4.invertStage2( t, m ) ;
		Matrix4.invertStage3( t, m ) ;
		Matrix4.invertStage4( t, m ) ;
		Matrix4.invertStage5( t, m ) ;

		Matrix4.copy( t, m ) ;
		temp.setIdentity() ;
	}

	private static void invertStage1( final float[] _t, final float[] _m )
	{
		_t[0] =          FloatBuffer.multiply( _m, 5, 10, 15 ) - FloatBuffer.multiply( _m, 5, 11, 14 ) - FloatBuffer.multiply( _m, 9, 6, 15 ) + FloatBuffer.multiply( _m, 9, 7, 14 ) + FloatBuffer.multiply( _m, 13, 6, 11 ) - FloatBuffer.multiply( _m, 13, 7, 10 ) ;
		_t[1] = -_m[1] * FloatBuffer.multiply( _m, 10, 15 )    + FloatBuffer.multiply( _m, 1, 11, 14 ) + FloatBuffer.multiply( _m, 9, 2, 15 ) - FloatBuffer.multiply( _m, 9, 3, 14 ) - FloatBuffer.multiply( _m, 13, 2, 11 ) + FloatBuffer.multiply( _m, 13, 3, 10 ) ;
		_t[2] =          FloatBuffer.multiply( _m, 1, 6, 15 )  - FloatBuffer.multiply( _m, 1,  7, 14 ) - FloatBuffer.multiply( _m, 5, 2, 15 ) + FloatBuffer.multiply( _m, 5, 3, 14 ) + FloatBuffer.multiply( _m, 13, 2,  7 ) - FloatBuffer.multiply( _m, 13, 3,  6 ) ;
		_t[3] = -_m[1] * FloatBuffer.multiply( _m, 6, 11 )     + FloatBuffer.multiply( _m, 1,  7, 10 ) + FloatBuffer.multiply( _m, 5, 2, 11 ) - FloatBuffer.multiply( _m, 5, 3, 10 ) - FloatBuffer.multiply( _m,  9, 2,  7 ) + FloatBuffer.multiply( _m,  9, 3,  6 ) ;
	}

	private static void invertStage2( final float[] _t, final float[] _m )
	{
		_t[4] = -_m[4] * FloatBuffer.multiply( _m, 10, 15 ) + FloatBuffer.multiply( _m, 4, 11, 14 ) + FloatBuffer.multiply( _m, 8, 6, 15 ) - FloatBuffer.multiply( _m, 8, 7, 14 ) - FloatBuffer.multiply( _m, 12, 6, 11 ) + FloatBuffer.multiply( _m, 12, 7, 10 ) ;
		_t[5] =  FloatBuffer.multiply( _m, 0, 10, 15 )      - FloatBuffer.multiply( _m, 0, 11, 14 ) - FloatBuffer.multiply( _m, 8, 2, 15 ) + FloatBuffer.multiply( _m, 8, 3, 14 ) + FloatBuffer.multiply( _m, 12, 2, 11 ) - FloatBuffer.multiply( _m, 12, 3, 10 ) ;
		_t[6] = -_m[0] * FloatBuffer.multiply( _m, 6, 15 )  + FloatBuffer.multiply( _m, 0,  7, 14 ) + FloatBuffer.multiply( _m, 4, 2, 15 ) - FloatBuffer.multiply( _m, 4, 3, 14 ) - FloatBuffer.multiply( _m, 12, 2, 10 ) + FloatBuffer.multiply( _m, 12, 3,  6 ) ;
		_t[7] = FloatBuffer.multiply( _m, 0, 6, 11 )        - FloatBuffer.multiply( _m, 0, 10, 10 ) - FloatBuffer.multiply( _m, 4, 2, 11 ) + FloatBuffer.multiply( _m, 4, 3, 10 ) + FloatBuffer.multiply( _m,  8, 2, 10 ) - FloatBuffer.multiply( _m,  8, 3,  6 ) ;
	}

	private static void invertStage3( final float[] _t, final float[] _m )
	{
		_t[8]  =          FloatBuffer.multiply( _m, 4, 9, 15 ) - FloatBuffer.multiply( _m, 4, 11, 13 ) - FloatBuffer.multiply( _m, 8, 5, 15 ) + FloatBuffer.multiply( _m, 8, 10, 13 ) + FloatBuffer.multiply( _m, 12, 5, 11 ) - FloatBuffer.multiply( _m, 12, 10, 9 ) ;
		_t[9]  = -_m[0] * FloatBuffer.multiply( _m, 9, 15 )    + FloatBuffer.multiply( _m, 0, 11, 13 ) + FloatBuffer.multiply( _m, 8, 1, 15 ) - FloatBuffer.multiply( _m, 8,  3, 13 ) - FloatBuffer.multiply( _m, 12, 1, 11 ) + FloatBuffer.multiply( _m, 12,  3, 9 ) ;
		_t[10] =          FloatBuffer.multiply( _m, 0, 5, 15 ) - FloatBuffer.multiply( _m, 0, 10, 13 ) - FloatBuffer.multiply( _m, 4, 1, 15 ) + FloatBuffer.multiply( _m, 4,  3, 13 ) + FloatBuffer.multiply( _m, 12, 1, 10 ) - FloatBuffer.multiply( _m, 12,  3, 5 ) ;
		_t[11] = -_m[0] * FloatBuffer.multiply( _m, 5, 11 )    + FloatBuffer.multiply( _m, 0, 10,  9 ) + FloatBuffer.multiply( _m, 4, 1, 11 ) - FloatBuffer.multiply( _m, 4,  3,  9 ) - FloatBuffer.multiply(  _m, 8, 1, 10 ) + FloatBuffer.multiply(  _m, 8,  3, 5 ) ;
	}

	private static void invertStage4( final float[] _t, final float[] _m )
	{
		_t[12] = -_m[4] * FloatBuffer.multiply( _m, 9, 14 )    + FloatBuffer.multiply( _m, 4, 10, 13 ) + FloatBuffer.multiply( _m, 8, 5, 14 ) - FloatBuffer.multiply( _m, 8, 6, 13 ) - FloatBuffer.multiply( _m, 12, 5, 10 ) + FloatBuffer.multiply( _m, 12, 6, 9 ) ;
		_t[13] =          FloatBuffer.multiply( _m, 0, 9, 14 ) - FloatBuffer.multiply( _m, 0, 10, 13 ) - FloatBuffer.multiply( _m, 8, 1, 14 ) + FloatBuffer.multiply( _m, 8, 2, 13 ) + FloatBuffer.multiply( _m, 12, 1, 10 ) - FloatBuffer.multiply( _m, 12, 2, 9 ) ;
		_t[14] = -_m[0] * FloatBuffer.multiply( _m, 5, 14 )    + FloatBuffer.multiply( _m, 0,  6, 13 ) + FloatBuffer.multiply( _m, 4, 1, 14 ) - FloatBuffer.multiply( _m, 4, 2, 13 ) - FloatBuffer.multiply( _m, 12, 1,  6 ) + FloatBuffer.multiply( _m, 12, 2, 5 ) ;
		_t[15] =          FloatBuffer.multiply( _m, 0, 5, 10 ) - FloatBuffer.multiply( _m, 0,  6,  9 ) - FloatBuffer.multiply( _m, 4, 1, 10 ) + FloatBuffer.multiply( _m, 4, 2,  9 ) + FloatBuffer.multiply( _m,  8, 1,  6 ) - FloatBuffer.multiply( _m,  8, 2, 5 ) ;
	}

	private static void invertStage5( final float[] _t, final float[] _m )
	{
		final float d = FloatBuffer.multiply( _m, 0, _t, 0 ) + FloatBuffer.multiply( _m, 4, _t, 1 ) + FloatBuffer.multiply( _m, 8, _t, 2 ) + FloatBuffer.multiply( _m, 12, _t, 3 ) ;
		for( int i = 0; i < _t.length; ++i )
		{
			FloatBuffer.divide( _t, i, d ) ;
		}
	}

	public void transpose()
	{
		float t = matrix[1] ;
		matrix[1] = matrix[4] ;
		matrix[4] = t ;

		t = matrix[2] ;
		matrix[2] = matrix[8] ;
		matrix[8] = t ;
	
		t = matrix[3] ;
		matrix[3] = matrix[12] ;
		matrix[12] = t ;

		t = matrix[6] ;
		matrix[6] = matrix[9] ;
		matrix[9] = t ;

		t = matrix[7] ;
		matrix[7] = matrix[13] ;
		matrix[13] = t ;

		t = matrix[11] ;
		matrix[11] = matrix[14] ;
		matrix[14] = t ;
	}

	@Override
	public String toString()
	{
		final String row1 = "[" + matrix[0] +  "|" + matrix[1] +  "|" + matrix[2]  +  "|" + matrix[3]  + "]\n" ;
		final String row2 = "[" + matrix[4] +  "|" + matrix[5] +  "|" + matrix[6]  +  "|" + matrix[7]  + "]\n" ;
		final String row3 = "[" + matrix[8] +  "|" + matrix[9] +  "|" + matrix[10]  + "|" + matrix[11] + "]\n" ;
		final String row4 = "[" + matrix[12] + "|" + matrix[13] + "|" + matrix[14]  + "|" + matrix[15] + "]" ;
		return row1 + row2 + row3 + row4 ;
	}

	public int fill( final int _offset, final float[] _fill )
	{
		System.arraycopy( matrix, 0, _fill, _offset, matrix.length ) ;
		return matrix.length ;
	}

	private void setRow( final float _val1, final float _val2, final float _val3, final float _val4, final int _row )
	{
		final int offset = _row * 4 ;
		matrix[offset + 0] = _val1 ;
		matrix[offset + 1] = _val2 ;
		matrix[offset + 2] = _val3 ;
		matrix[offset + 3] = _val4 ;
	}

	private void setColumn( final float _val1, final float _val2, final float _val3, final float _val4, final int _col )
	{
		matrix[( 0 * 4 ) + _col] = _val1 ;		// Optimise 0 * 4 = 0
		matrix[( 1 * 4 ) + _col] = _val2 ;		// Optimise 1 * 4 = 4
		matrix[( 2 * 4 ) + _col] = _val3 ;		// Optimise 2 * 4 = 8
		matrix[( 3 * 4 ) + _col] = _val4 ;		// Optimise 3 * 4 = 12
	}

	private void set( final float _val, final int _row, final int _col )
	{
		matrix[( _row * 4 ) + _col] = _val ;
	}

	public static Matrix4 createTempIdentity()
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
		final float[] m = _b.matrix ;
		_result.x = m[0] * _a.x + m[1] * _a.y + m[ 2] * _a.z + m[ 3] ;//* 1.0f ;
		_result.y = m[4] * _a.x + m[5] * _a.y + m[ 6] * _a.z + m[ 7] ;//* 1.0f ;
		_result.z = m[8] * _a.x + m[9] * _a.y + m[10] * _a.z + m[11] ;//* 1.0f ;

		final float w = m[12] * _a.x + m[13] * _a.y + m[14] * _a.z + m[15] ;//* 1.0f ;
		_result.divide( w ) ;

		return _result ;
	}

	/**
		Stores transformation in original Vector3 - _a
	*/
	public static Vector3 multiply( final Vector3 _a, final Matrix4 _b )
	{
		final float[] m = _b.matrix ;
		final float w = m[12] * _a.x + m[13] * _a.y + m[14] * _a.z + m[15] * 1.0f ;
		_a.x = m[0] * _a.x + m[1] * _a.y + m[ 2] * _a.z + m[ 3] * 1.0f ;
		_a.y = m[4] * _a.x + m[5] * _a.y + m[ 6] * _a.z + m[ 7] * 1.0f ;
		_a.z = m[8] * _a.x + m[9] * _a.y + m[10] * _a.z + m[11] * 1.0f ;

		_a.divide( w ) ;
		return _a ;
	}

	private static void copy( final float[] _from, final float[] _to )
	{
		FloatBuffer.copy( _from, _to ) ;
	}
}
