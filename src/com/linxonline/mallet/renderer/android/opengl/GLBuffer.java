package com.linxonline.mallet.renderer.android.opengl ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class GLBuffer
{
	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFF ;
	public final static int PRIMITIVE_EXPANSION = 1 ;

	public final static int VBO_VAR_BYTE_SIZE = 4 ;
	public final static int IBO_VAR_BYTE_SIZE = 2 ;

	private final byte[] abgrTemp = new byte[4] ;

	private final boolean ui ;

	public GLBuffer( final boolean _ui )
	{
		ui = _ui ;
	}

	public boolean isUI()
	{
		return ui ;
	}

	public void draw( final Matrix4 _projection ) {}

	public void shutdown() {}

	protected float getABGR( final MalletColour _colour )
	{
		abgrTemp[0] = _colour.colours[MalletColour.ALPHA] ;
		abgrTemp[1] = _colour.colours[MalletColour.BLUE] ;
		abgrTemp[2] = _colour.colours[MalletColour.GREEN] ;
		abgrTemp[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( abgrTemp, 0 ) ;
	}

	protected static void apply( final Matrix4 _mat4,
							   final Matrix4 _temp,
							   final Vector3 _position,
							   final Vector3 _offset,
							   final Vector3 _rotation,
							   final Vector3 _scale )
	{
		_mat4.setIdentity() ;
		_mat4.setTranslate( _position.x, _position.y, 0.0f ) ;

		_temp.setRotateX( _rotation.x ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setRotateY( _rotation.y ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setRotateZ( _rotation.z ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setScale( _scale.x, _scale.y, _scale.z ) ;
		_temp.setTranslate( _offset.x, _offset.y, _offset.z ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;
	}

	protected static VertexAttrib[] constructVertexAttrib( final Shape.Attribute[] _swivel, final GLProgram _program )
	{
		final VertexAttrib[] attributes = new VertexAttrib[_swivel.length] ;

		int offset = 0 ;
		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case VEC3  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, MGL.GL_FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case FLOAT :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 4, MGL.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case VEC2     :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 2, MGL.GL_FLOAT, false, offset ) ;
					offset += 2 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
			}
		}

		return attributes ;
	}

	protected static int calculateVertexSize( final Shape.Attribute[] _swivel )
	{
		int size = 0 ;
		for( int j = 0; j < _swivel.length; j++ )
		{
			switch( _swivel[j] )
			{
				case VEC3  : size += 3 ; break ;
				case FLOAT : size += 1 ; break ;
				case VEC2  : size += 2 ; break ;
			}
		}

		return size ;
	}

	protected static void enableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			//System.out.println( att.toString() ) ;
			MGL.glEnableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}

	protected static void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.glDisableVertexAttribArray( att.index ) ;
		}
	}

	protected static class VertexAttrib
	{
		public int index ;				// Specifies the index of the generic vertex attribute to be modified 
		public int size ;				// Specifies the number of components per generic vertex attribute
		public int type ;				// Specifies the data type ;
		public boolean normalised ;		// Specifies whether fixed-point data values should be normalized
		public int offset ;				// Specifies the offset for the first component

		public VertexAttrib( final int _index, final int _size, final int _type, final boolean _normalised, final int _offset )
		{
			index = _index ;
			size = _size ;
			type = _type ;
			normalised = _normalised ;
			offset = _offset ; 
		}

		public String toString()
		{
			final StringBuilder buffer = new StringBuilder() ;
			buffer.append( "Index: " ) ;
			buffer.append( index ) ;
			buffer.append( " Size: " ) ;
			buffer.append( size ) ;
			buffer.append( " Norm: " ) ;
			buffer.append( normalised ) ;
			buffer.append( " Offset: " ) ;
			buffer.append( offset ) ;

			return buffer.toString() ;
		}
	}
}
