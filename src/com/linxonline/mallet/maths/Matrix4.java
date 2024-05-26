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
		1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f
	} ;

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
		setIdentity() ;
	}

	public Matrix4( final Matrix4 _matrix )
	{
		Matrix4.copy( _matrix.matrix, matrix ) ;
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
	}

	public void set( final float _a00, final float _a01, final float _a02, final float _a03,
					 final float _a10, final float _a11, final float _a12, final float _a13,
					 final float _a20, final float _a21, final float _a22, final float _a23,
					 final float _a30, final float _a31, final float _a32, final float _a33 )
	{
		matrix[0] = _a00 ;
		matrix[4] = _a01 ;
		matrix[8] = _a02 ;
		matrix[12] = _a03 ;

		matrix[1] = _a10 ;
		matrix[5] = _a11 ;
		matrix[9] = _a12 ;
		matrix[13] = _a13 ;

		matrix[2] = _a20 ;
		matrix[6] = _a21 ;
		matrix[10] = _a22 ;
		matrix[14] = _a23 ;

		matrix[3] = _a30 ;
		matrix[7] = _a31 ;
		matrix[11] = _a32 ;
		matrix[15] = _a33 ;
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
		matrix[12] += _x ;
		matrix[13] += _y ;
		matrix[14] += _z ;
	}

	public void setPosition( final Vector3 _vec )
	{
		setPosition( _vec.x, _vec.y, _vec.z ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		matrix[12] = _x ;
		matrix[13] = _y ;
		matrix[14] = _z ;

		//set( _x, 0, 3 ) ;	//	[1 | 0 | 0 | _x]
		//set( _y, 1, 3 ) ;	//	[0 | 1 | 0 | _y]
		//set( _z, 2, 3 ) ;	//	[0 | 0 | 1 | _z]
							//	[0 | 0 | 0 |  1]
	}

	public void scale( final float _x, final float _y, final float _z )
	{
		matrix[0] *= _x ;
		matrix[4] *= _y ;
		matrix[8] *= _z ;

		matrix[1] *= _x ;
		matrix[5] *= _y ;
		matrix[9] *= _z ;
		
		matrix[2] *= _x ;
		matrix[6] *= _y ;
		matrix[10] *= _z ;

		matrix[3] *= _x ;
		matrix[7] *= _y ;
		matrix[11] *= _z ;
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

	public void setRotate( final float _x, final float _y, final float _z )
	{
		final float a = ( float )Math.cos( _x ) ;
		final float b = ( float )Math.sin( _x ) ;
		final float c = ( float )Math.cos( _y ) ;
		final float d = ( float )Math.sin( _y ) ;
		final float e = ( float )Math.cos( _z ) ;
		final float f = ( float )Math.sin( _z ) ;

		final float ae = a * e ;
		final float af = a * f ;
		final float be = b * e ;
		final float bf = b * f ;

		matrix[0] = c * e ;
		matrix[4] = - c * f ;
		matrix[8] = d ;

		matrix[1] = af + be * d ;
		matrix[5] = ae - bf * d ;
		matrix[9] = - b * c ;

		matrix[2] = bf - ae * d ;
		matrix[6] = be + af * d ;
		matrix[10] = a * c ;
	}

	public void setRotateX( final float _theta )
	{
		final float cos = ( float )Math.cos( _theta ) ;
		final float sin = ( float )Math.sin( _theta ) ;

		matrix[5] = cos ;
		matrix[9] = -sin ;

		matrix[6] = sin ;
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
		matrix[8] = sin ;

		matrix[2] = -sin ;
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
		matrix[4] = -sin ;

		matrix[1] = sin ;
		matrix[5] = cos ;

		//	[cos | -sin |  0]
		//	[sin |  cos |  0]
		//	[ 0  |   0  |  1]
	}

	public void applyTransformations( final Vector3 _position, final Vector3 _rotation, final Vector3 _scale )
	{
		final float c1 = ( float )Math.cos( _rotation.x / 2 ) ;
		final float c2 = ( float )Math.cos( _rotation.y / 2 ) ;
		final float c3 = ( float )Math.cos( _rotation.z / 2 ) ;

		final float s1 = ( float )Math.sin( _rotation.x / 2 ) ;
		final float s2 = ( float )Math.sin( _rotation.y / 2 ) ;
		final float s3 = ( float )Math.sin( _rotation.z / 2 ) ;

		final float x = s1 * c2 * c3 + c1 * s2 * s3 ;
		final float y = c1 * s2 * c3 - s1 * c2 * s3 ;
		final float z = c1 * c2 * s3 + s1 * s2 * c3 ;
		final float w = c1 * c2 * c3 - s1 * s2 * s3 ;

		final float x2 = x + x ;
		final float y2 = y + y ;
		final float z2 = z + z ;

		final float xx = x * x2 ;
		final float xy = x * y2 ;
		final float xz = x * z2 ;

		final float yy = y * y2 ;
		final float yz = y * z2 ;
		final float zz = z * z2 ;

		final float wx = w * x2 ;
		final float wy = w * y2 ;
		final float wz = w * z2 ;

		final float sx = _scale.x ;
		final float sy = _scale.y ;
		final float sz = _scale.z ;

		matrix[0] = ( 1.0f - ( yy + zz ) ) * sx ;
		matrix[1] = ( xy + wz ) * sx ;
		matrix[2] = ( xz - wy ) * sx ;
		matrix[3] = 0.0f ;

		matrix[4] = ( xy - wz ) * sy ;
		matrix[5] = ( 1.0f - ( xx + zz ) ) * sy ;
		matrix[6] = ( yz + wx ) * sy ;
		matrix[7] = 0.0f ;

		matrix[8] = ( xz + wy ) * sz ;
		matrix[9] = ( yz - wx ) * sz ;
		matrix[10] = ( 1 - ( xx + yy ) ) * sz ;
		matrix[11] = 0.0f ;

		matrix[12] = _position.x ;
		matrix[13] = _position.y ;
		matrix[14] = _position.z ;
		matrix[15] = 1.0f ;
	}

	public void multiply( final Matrix4 _mat )
	{
		final float[] m = this.matrix ;				// Makes it easier to read
		final float[] x = _mat.matrix ;

		final float a11 = m[0] ;
		final float a12 = m[4] ;
		final float a13 = m[8] ;
		final float a14 = m[12] ;

		final float a21 = m[1] ;
		final float a22 = m[5] ;
		final float a23 = m[9] ;
		final float a24 = m[13] ;

		final float a31 = m[2] ;
		final float a32 = m[6] ;
		final float a33 = m[10] ;
		final float a34 = m[14] ;

		final float a41 = m[3] ;
		final float a42 = m[7] ;
		final float a43 = m[11] ;
		final float a44 = m[15] ;

		final float b11 = x[0] ;
		final float b12 = x[4] ;
		final float b13 = x[8] ;
		final float b14 = x[12] ;

		final float b21 = x[1] ;
		final float b22 = x[5] ;
		final float b23 = x[9] ;
		final float b24 = x[13] ;

		final float b31 = x[2] ;
		final float b32 = x[6] ;
		final float b33 = x[10] ;
		final float b34 = x[14] ;

		final float b41 = x[3] ;
		final float b42 = x[7] ;
		final float b43 = x[11] ;
		final float b44 = x[15] ;

		m[0] = a11 * b11 + a12 * b21 + a13 * b31 + a14 * b41 ;
		m[4] = a11 * b12 + a12 * b22 + a13 * b32 + a14 * b42 ;
		m[8] = a11 * b13 + a12 * b23 + a13 * b33 + a14 * b43 ;
		m[12] = a11 * b14 + a12 * b24 + a13 * b34 + a14 * b44 ;

		m[1] = a21 * b11 + a22 * b21 + a23 * b31 + a24 * b41 ;
		m[5] = a21 * b12 + a22 * b22 + a23 * b32 + a24 * b42 ;
		m[9] = a21 * b13 + a22 * b23 + a23 * b33 + a24 * b43 ;
		m[13] = a21 * b14 + a22 * b24 + a23 * b34 + a24 * b44 ;

		m[2] = a31 * b11 + a32 * b21 + a33 * b31 + a34 * b41 ;
		m[6] = a31 * b12 + a32 * b22 + a33 * b32 + a34 * b42 ;
		m[10] = a31 * b13 + a32 * b23 + a33 * b33 + a34 * b43 ;
		m[14] = a31 * b14 + a32 * b24 + a33 * b34 + a34 * b44 ;

		m[3] = a41 * b11 + a42 * b21 + a43 * b31 + a44 * b41 ;
		m[7] = a41 * b12 + a42 * b22 + a43 * b32 + a44 * b42 ;
		m[11] = a41 * b13 + a42 * b23 + a43 * b33 + a44 * b43 ;
		m[15] = a41 * b14 + a42 * b24 + a43 * b34 + a44 * b44 ;
	}

	public void invert()
	{
		final float[] m = this.matrix ;

		final float n11 = m[0] ;
		final float n21 = m[1] ;
		final float n31 = m[2] ;
		final float n41 = m[3] ;

		final float n12 = m[4] ;
		final float n22 = m[5] ;
		final float n32 = m[6] ;
		final float n42 = m[7] ;

		final float n13 = m[8] ;
		final float n23 = m[9] ;
		final float n33 = m[10] ;
		final float n43 = m[11] ;

		final float n14 = m[12] ;
		final float n24 = m[13] ;
		final float n34 = m[14] ;
		final float n44 = m[15] ;

		final float t11 = n23 * n34 * n42 - n24 * n33 * n42 + n24 * n32 * n43 - n22 * n34 * n43 - n23 * n32 * n44 + n22 * n33 * n44 ;
		final float t12 = n14 * n33 * n42 - n13 * n34 * n42 - n14 * n32 * n43 + n12 * n34 * n43 + n13 * n32 * n44 - n12 * n33 * n44 ;
		final float t13 = n13 * n24 * n42 - n14 * n23 * n42 + n14 * n22 * n43 - n12 * n24 * n43 - n13 * n22 * n44 + n12 * n23 * n44 ;
		final float t14 = n14 * n23 * n32 - n13 * n24 * n32 - n14 * n22 * n33 + n12 * n24 * n33 + n13 * n22 * n34 - n12 * n23 * n34 ;

		final float det = n11 * t11 + n21 * t12 + n31 * t13 + n41 * t14;
		if( det == 0 )
		{
			setIdentity() ;
		}

		final float detInv = 1 / det ;

		m[0] = t11 * detInv ;
		m[1] = ( n24 * n33 * n41 - n23 * n34 * n41 - n24 * n31 * n43 + n21 * n34 * n43 + n23 * n31 * n44 - n21 * n33 * n44 ) * detInv ;
		m[2] = ( n22 * n34 * n41 - n24 * n32 * n41 + n24 * n31 * n42 - n21 * n34 * n42 - n22 * n31 * n44 + n21 * n32 * n44 ) * detInv ;
		m[3] = ( n23 * n32 * n41 - n22 * n33 * n41 - n23 * n31 * n42 + n21 * n33 * n42 + n22 * n31 * n43 - n21 * n32 * n43 ) * detInv ;

		m[4] = t12 * detInv ;
		m[5] = ( n13 * n34 * n41 - n14 * n33 * n41 + n14 * n31 * n43 - n11 * n34 * n43 - n13 * n31 * n44 + n11 * n33 * n44 ) * detInv ;
		m[6] = ( n14 * n32 * n41 - n12 * n34 * n41 - n14 * n31 * n42 + n11 * n34 * n42 + n12 * n31 * n44 - n11 * n32 * n44 ) * detInv ;
		m[7] = ( n12 * n33 * n41 - n13 * n32 * n41 + n13 * n31 * n42 - n11 * n33 * n42 - n12 * n31 * n43 + n11 * n32 * n43 ) * detInv ;

		m[8] = t13 * detInv ;
		m[9] = ( n14 * n23 * n41 - n13 * n24 * n41 - n14 * n21 * n43 + n11 * n24 * n43 + n13 * n21 * n44 - n11 * n23 * n44 ) * detInv ;
		m[10] = ( n12 * n24 * n41 - n14 * n22 * n41 + n14 * n21 * n42 - n11 * n24 * n42 - n12 * n21 * n44 + n11 * n22 * n44 ) * detInv ;
		m[11] = ( n13 * n22 * n41 - n12 * n23 * n41 - n13 * n21 * n42 + n11 * n23 * n42 + n12 * n21 * n43 - n11 * n22 * n43 ) * detInv ;

		m[12] = t14 * detInv ;
		m[13] = ( n13 * n24 * n31 - n14 * n23 * n31 + n14 * n21 * n33 - n11 * n24 * n33 - n13 * n21 * n34 + n11 * n23 * n34 ) * detInv ;
		m[14] = ( n14 * n22 * n31 - n12 * n24 * n31 - n14 * n21 * n32 + n11 * n24 * n32 + n12 * n21 * n34 - n11 * n22 * n34 ) * detInv ;
		m[15] = ( n12 * n23 * n31 - n13 * n22 * n31 + n13 * n21 * n32 - n11 * n23 * n32 - n12 * n21 * n33 + n11 * n22 * n33 ) * detInv ;

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
		final float x = _a.x ;
		final float y = _a.y ;
		final float z = _a.z ;

		final float w = 1 / ( m[3] * x + m[7] * y + m[11] * z + m[15] ) ;

		_result.x = ( m[0] * x + m[4] * y + m[8] * z + m[12] ) * w ;
		_result.y = ( m[1] * x + m[5] * y + m[9] * z + m[13] ) * w ;
		_result.z = ( m[2] * x + m[6] * y + m[10] * z + m[14] ) * w ;

		return _result ;
	}

	private static void copy( final float[] _from, final float[] _to )
	{
		FloatBuffer.copy( _from, _to ) ;
	}
}
