package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.caches.Cacheable ;

/**
	Designed for 3D transformations.
*/
public class Matrix4 implements Cacheable
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
		set( _z, 2, 3 ) ;	//	[0 | 0 | 1 |  0]
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
		final float[] x = _mat.matrix ;
		Matrix4.multiplyStage1( this, x ) ;
		Matrix4.multiplyStage2( this, x ) ;
		Matrix4.multiplyStage3( this, x ) ;
		Matrix4.multiplyStage4( this, x ) ;
	}

	private static void multiplyStage1( final Matrix4 _m, final float[] _x )
	{
		final float[] m = _m.matrix ;				// Makes it easier to read
		final float a00 = FloatBuffer.multiply( m, 0, _x, 0 ) + FloatBuffer.multiply( m, 1, _x, 4 ) + FloatBuffer.multiply( m, 2, _x,  8 ) + FloatBuffer.multiply( m, 3, _x, 12 ) ;
		final float a01 = FloatBuffer.multiply( m, 0, _x, 1 ) + FloatBuffer.multiply( m, 1, _x, 5 ) + FloatBuffer.multiply( m, 2, _x,  9 ) + FloatBuffer.multiply( m, 3, _x, 13 ) ;
		final float a02 = FloatBuffer.multiply( m, 0, _x, 2 ) + FloatBuffer.multiply( m, 1, _x, 6 ) + FloatBuffer.multiply( m, 2, _x, 10 ) + FloatBuffer.multiply( m, 3, _x, 14 ) ;
		final float a03 = FloatBuffer.multiply( m, 0, _x, 3 ) + FloatBuffer.multiply( m, 1, _x, 7 ) + FloatBuffer.multiply( m, 2, _x, 11 ) + FloatBuffer.multiply( m, 3, _x, 15 ) ;
		_m.setRow( a00, a01, a02, a03, 0 ) ;
	}

	private static void multiplyStage2( final Matrix4 _m, final float[] _x )
	{
		final float[] m = _m.matrix ;				// Makes it easier to read
		final float a10 = FloatBuffer.multiply( m, 4, _x, 0 ) + FloatBuffer.multiply( m, 5, _x, 4 ) + FloatBuffer.multiply( m, 6, _x,  8 ) + FloatBuffer.multiply( m, 7, _x, 12 ) ;
		final float a11 = FloatBuffer.multiply( m, 4, _x, 1 ) + FloatBuffer.multiply( m, 5, _x, 5 ) + FloatBuffer.multiply( m, 6, _x,  9 ) + FloatBuffer.multiply( m, 7, _x, 13 ) ;
		final float a12 = FloatBuffer.multiply( m, 4, _x, 2 ) + FloatBuffer.multiply( m, 5, _x, 6 ) + FloatBuffer.multiply( m, 6, _x, 10 ) + FloatBuffer.multiply( m, 7, _x, 14 ) ;
		final float a13 = FloatBuffer.multiply( m, 4, _x, 3 ) + FloatBuffer.multiply( m, 5, _x, 7 ) + FloatBuffer.multiply( m, 6, _x, 11 ) + FloatBuffer.multiply( m, 7, _x, 15 ) ;
		_m.setRow( a10, a11, a12, a13, 1 ) ;
	}

	private static void multiplyStage3( final Matrix4 _m, final float[] _x )
	{
		final float[] m = _m.matrix ;				// Makes it easier to read
		final float a20 = FloatBuffer.multiply( m, 8, _x, 0 ) + FloatBuffer.multiply( m, 9, _x, 4 ) + FloatBuffer.multiply( m, 10, _x,  8 ) + FloatBuffer.multiply( m, 11, _x, 12 ) ;
		final float a21 = FloatBuffer.multiply( m, 8, _x, 1 ) + FloatBuffer.multiply( m, 9, _x, 5 ) + FloatBuffer.multiply( m, 10, _x,  9 ) + FloatBuffer.multiply( m, 11, _x, 13 ) ;
		final float a22 = FloatBuffer.multiply( m, 8, _x, 2 ) + FloatBuffer.multiply( m, 9, _x, 6 ) + FloatBuffer.multiply( m, 10, _x, 10 ) + FloatBuffer.multiply( m, 11, _x, 14 ) ;
		final float a23 = FloatBuffer.multiply( m, 8, _x, 3 ) + FloatBuffer.multiply( m, 9, _x, 7 ) + FloatBuffer.multiply( m, 10, _x, 11 ) + FloatBuffer.multiply( m, 11, _x, 15 ) ;
		_m.setRow( a20, a21, a22, a23, 2 ) ;
	}

	private static void multiplyStage4( final Matrix4 _m, final float[] _x )
	{
		final float[] m = _m.matrix ;				// Makes it easier to read
		final float a30 = FloatBuffer.multiply( m, 12, _x, 0 ) + FloatBuffer.multiply( m, 13, _x, 4 ) + FloatBuffer.multiply( m, 14, _x,  8 ) + FloatBuffer.multiply( m, 15, _x, 12 ) ;
		final float a31 = FloatBuffer.multiply( m, 12, _x, 1 ) + FloatBuffer.multiply( m, 13, _x, 5 ) + FloatBuffer.multiply( m, 14, _x,  9 ) + FloatBuffer.multiply( m, 15, _x, 13 ) ;
		final float a32 = FloatBuffer.multiply( m, 12, _x, 2 ) + FloatBuffer.multiply( m, 13, _x, 6 ) + FloatBuffer.multiply( m, 14, _x, 10 ) + FloatBuffer.multiply( m, 15, _x, 14 ) ;
		final float a33 = FloatBuffer.multiply( m, 12, _x, 3 ) + FloatBuffer.multiply( m, 13, _x, 7 ) + FloatBuffer.multiply( m, 14, _x, 11 ) + FloatBuffer.multiply( m, 15, _x, 15 ) ;
		_m.setRow( a30, a31, a32, a33, 3 ) ;
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
		FloatBuffer.swap( matrix, 1, 4 ) ;
		FloatBuffer.swap( matrix, 2, 8 ) ;
		FloatBuffer.swap( matrix, 3, 12 ) ;
		FloatBuffer.swap( matrix, 6, 9 ) ;
		FloatBuffer.swap( matrix, 7, 13 ) ;
		FloatBuffer.swap( matrix, 11, 14 ) ;
	}

	@Override
	public void reset()
	{
		setIdentity() ;
	}

	public String toString()
	{
		final String row1 = "[" + matrix[0] +  "|" + matrix[1] +  "|" + matrix[2]  +  "|" + matrix[3]  + "]\n" ;
		final String row2 = "[" + matrix[4] +  "|" + matrix[5] +  "|" + matrix[6]  +  "|" + matrix[7]  + "]\n" ;
		final String row3 = "[" + matrix[8] +  "|" + matrix[9] +  "|" + matrix[10]  + "|" + matrix[11] + "]\n" ;
		final String row4 = "[" + matrix[12] + "|" + matrix[13] + "|" + matrix[14]  + "|" + matrix[15] + "]" ;
		return row1 + row2 + row3 + row4 ;
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
		_result.setXYZ( FloatBuffer.multiply( m, 0, _a.x ) + FloatBuffer.multiply( m, 1, _a.y ) + FloatBuffer.multiply( m,  2, _a.z ) + FloatBuffer.multiply( m,  3, 1.0f ),
						FloatBuffer.multiply( m, 4, _a.x ) + FloatBuffer.multiply( m, 5, _a.y ) + FloatBuffer.multiply( m,  6, _a.z ) + FloatBuffer.multiply( m,  7, 1.0f ),
						FloatBuffer.multiply( m, 8, _a.x ) + FloatBuffer.multiply( m, 9, _a.y ) + FloatBuffer.multiply( m, 10, _a.z ) + FloatBuffer.multiply( m, 11, 1.0f ) ) ;

		final float w = FloatBuffer.multiply( m, 12, _a.x ) + FloatBuffer.multiply( m, 13, _a.y ) + FloatBuffer.multiply( m, 14, _a.z ) + FloatBuffer.multiply( m, 15, 1.0f ) ;
		_result.divide( w ) ;

		return _result ;
	}

	/**
		Stores transformation in original Vector3 - _a
	*/
	public static Vector3 multiply( final Vector3 _a, final Matrix4 _b )
	{
		final float[] m = _b.matrix ;
		final float w = FloatBuffer.multiply( m, 12, _a.x ) + FloatBuffer.multiply( m, 13, _a.y ) + FloatBuffer.multiply( m, 14, _a.z ) + FloatBuffer.multiply( m, 15, 1.0f ) ;
		_a.setXYZ( FloatBuffer.multiply( m, 0, _a.x ) + FloatBuffer.multiply( m, 1, _a.y ) + FloatBuffer.multiply( m,  2, _a.z ) + FloatBuffer.multiply( m,  3, 1.0f ),
				   FloatBuffer.multiply( m, 4, _a.x ) + FloatBuffer.multiply( m, 5, _a.y ) + FloatBuffer.multiply( m,  6, _a.z ) + FloatBuffer.multiply( m,  7, 1.0f ),
				   FloatBuffer.multiply( m, 8, _a.x ) + FloatBuffer.multiply( m, 9, _a.y ) + FloatBuffer.multiply( m, 10, _a.z ) + FloatBuffer.multiply( m, 11, 1.0f ) ) ;

		_a.divide( w ) ;
		return _a ;
	}

	private static void copy( final float[] _from, final float[] _to )
	{
		FloatBuffer.copy( _from, _to ) ;
	}
}
