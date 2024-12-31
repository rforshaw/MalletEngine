package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;

import com.linxonline.mallet.maths.Matrix4 ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Texture ;
import com.linxonline.mallet.renderer.Colour ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Attribute ;
import com.linxonline.mallet.renderer.Operation ;
import com.linxonline.mallet.renderer.Action ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class GLBuffer
{
	protected final static Matrix4 IDENTITY = new Matrix4() ;

	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFFFF ;
	public final static int PRIMITIVE_EXPANSION = 1 ;

	public final static int VBO_VAR_BYTE_SIZE = 4 ;
	public final static int IBO_VAR_BYTE_SIZE = 4 ;

	private final static float[] floatTemp = new float[16] ;
	private final static int[] intTemp = new int[16] ;

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

	public void draw( GLCamera _camera ) {}

	public void shutdown() {}

	public static int getOperation( final Operation _operation )
	{
		switch( _operation )
		{
			default                 : return MGL.GL_ALWAYS ;
			case ALWAYS             : return MGL.GL_ALWAYS ;
			case NEVER              : return MGL.GL_NEVER ;
			case LESS_THAN          : return MGL.GL_LESS ;
			case GREATER_THAN       : return MGL.GL_GREATER ;
			case LESS_THAN_EQUAL    : return MGL.GL_LEQUAL ;
			case GREATER_THAN_EQUAL : return MGL.GL_GEQUAL ;
			case EQUAL              : return MGL.GL_EQUAL ;
			case NOT_EQUAL          : return MGL.GL_NOTEQUAL ;
		}
	}

	public static int getAction( final Action _action )
	{
		switch( _action )
		{
			default        : return MGL.GL_KEEP ;
			case KEEP      : return MGL.GL_KEEP ;
			case ZERO      : return MGL.GL_ZERO ;
			case REPLACE   : return MGL.GL_REPLACE ;
			case INCREMENT : return MGL.GL_INCR ;
			case DECREMENT : return MGL.GL_DECR ;
			case INVERT    : return MGL.GL_INVERT ;
		}
	}

	protected static void generateStorages( final GLProgram _glProgram, final Program _program, final AssetLookup<Storage, GLStorage> _lookup, final List<GLStorage> _toFill )
	{
		_toFill.clear() ;
		for( final GLProgram.SSBuffer ssb : _glProgram.inBuffers )
		{
			if( ssb == null )
			{
				continue ;
			}

			final String name = ssb.getName() ;
			final int binding = ssb.getBinding() ;

			final int size = _toFill.size() ;
			final int newSize = binding + 1 ;
			if( size < newSize )
			{
				for( int i = size; i < newSize; ++i )
				{
					_toFill.add( null ) ;
				}
			}

			final Storage storage = _program.getStorage( name ) ;
			if( storage == null )
			{
				System.out.println( "Failed to find storage for: " + name + " for " + _glProgram.getName() ) ;
				continue ;
			}

			final GLStorage glStorage = _lookup.getRHS( storage.index() ) ;
			_toFill.set( binding, glStorage ) ;
		}
	}

	public static void bindBuffers( final List<GLStorage> _storages )
	{
		final int size = _storages.size() ;
		for( int i = 0; i < size ; ++i )
		{
			// We store the binding point within the
			// array index (i).
			final GLStorage storage = _storages.get( i ) ;
			if( storage != null )
			{
				MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, storage.getID() ) ;
			}
		}
	}

	protected float getABGR( final Colour _colour )
	{
		abgrTemp[0] = _colour.colours[Colour.ALPHA] ;
		abgrTemp[1] = _colour.colours[Colour.BLUE] ;
		abgrTemp[2] = _colour.colours[Colour.GREEN] ;
		abgrTemp[3] = _colour.colours[Colour.RED] ;

		return ConvertBytes.toFloat( abgrTemp, 0 ) ;
	}

	/**
		There are three points of vertex attribute information.

		1) The attributes available from the GLProgram.
		2) The attributes required by the Program.
		3) The vertex-attributes available in the geometry.

		The geometry is not allowed to be added to a Drawbuffer unless
		it complies with the Program attributes.
		The geometry-attributes and Program-attributes must be a 1:1 mapping.

		TODO: It should be possible to allow geometry that provides more
		attributes than what the Program requires.

		The Program should provide all available attributes defined
		within GLProgram. The Program can define more attributes not
		defined by GLProgram, these will be ignored.

	*/
	protected static VertexAttrib[] constructVertexAttrib( final Program _program, final GLProgram _glProgram )
	{
		final Attribute[] swivel = _program.getAttributes() ;

		int active = 0 ;
		for( int i = 0; i < swivel.length; i++ )
		{
			// Figure out which attributes can be ignored.
			// A Program may use a shader that has fewer attributes
			// than the geometry we are using.
			active += ( swivel[i].ignore == false ) ? 1 : 0 ;
		}

		final VertexAttrib[] attributes = new VertexAttrib[active] ;

		int offset = 0 ;
		int increment = 0 ;
		for( int i = 0; i < swivel.length; i++ )
		{
			final GLProgram.Attribute glAttribute = _glProgram.getAttribute( swivel[i].name ) ;

			// The program may not require all of
			// our attributes, we'll still set them up but
			// specify the location as -1, this ensures
			// the correct vertex offsets are held.
			final int location = ( glAttribute != null ) ? glAttribute.getLocation() : -1 ;

			switch( swivel[i].type )
			{
				case VEC3  :
				{
					if( !swivel[i].ignore )
					{
						attributes[increment++] = new VertexAttrib( location, 3, MGL.GL_FLOAT, false, offset ) ;
					}

					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case FLOAT :
				{
					if( !swivel[i].ignore )
					{
						attributes[increment++] = new VertexAttrib( location, 4, MGL.GL_UNSIGNED_BYTE, true, offset ) ;
					}

					offset += 1 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case VEC2     :
				{
					if( !swivel[i].ignore )
					{
						attributes[increment++] = new VertexAttrib( location, 2, MGL.GL_FLOAT, false, offset ) ;
					}

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

	protected static final class VertexAttrib
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

		@Override
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
