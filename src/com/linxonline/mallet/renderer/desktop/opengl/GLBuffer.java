package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.UIntUniform ;
import com.linxonline.mallet.renderer.IntUniform ;
import com.linxonline.mallet.renderer.FloatUniform ;
import com.linxonline.mallet.renderer.BoolUniform ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Operation ;
import com.linxonline.mallet.renderer.Action ;

import com.linxonline.mallet.util.caches.MemoryPool ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Logger ;

public class GLBuffer
{
	protected final static Matrix4 IDENTITY = new Matrix4() ;

	private final static MemoryPool<Texture> TEXTURES = new MemoryPool<Texture>( () -> new Texture() ) ;

	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFFFF ;
	public final static int PRIMITIVE_EXPANSION = 1 ;

	public final static int VBO_VAR_BYTE_SIZE = 4 ;
	public final static int IBO_VAR_BYTE_SIZE = 4 ;

	private final static float[] floatTemp = new float[16] ;
	private final static int[] intTemp = new int[16] ;

	private final byte[] abgrTemp = new byte[4] ;

	private final boolean ui ;

	private int textureUnit = 0 ;	// This needs to be reset, call loadProgramUniforms() first, then loadDrawUniforms().

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
		for( final String name : _glProgram.program.getBuffers() )
		{
			final Storage storage = _program.getStorage( name ) ;
			if( storage == null )
			{
				_toFill.add( null ) ;
				continue ;
			}

			final GLStorage glStorage = _lookup.getRHS( storage.index() ) ;
			_toFill.add( glStorage ) ;
		}
	}

	public static void bindBuffers( final List<GLStorage> _storages )
	{
		final int size = _storages.size() ;
		for( int i = 0; i < size ; ++i )
		{
			final GLStorage storage = _storages.get( i ) ;
			MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, storage.id[0] ) ;
		}
	}

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
		_mat4.setPosition( _position.x, _position.y, 0.0f ) ;

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
		_temp.setPosition( _offset.x, _offset.y, _offset.z ) ;
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

	private static final class Texture implements IUniform
	{
		public GLImage image ;

		public int minFilter ;
		public int magFilter ;
		public int uWrap ;
		public int vWrap ;

		public Texture() {}

		public void set( final GLImage _image, final MalletTexture _texture )
		{
			image = _image ;
			minFilter = GLImage.calculateMinFilter( _texture.getMinificationFilter() ) ;
			magFilter = GLImage.calculateMagFilter( _texture.getMaxificationFilter() ) ;

			uWrap = GLImage.calculateWrap( _texture.getUWrap() ) ;
			vWrap = GLImage.calculateWrap( _texture.getVWrap() ) ;
		}

		public void set( final GLImage _image, final MalletFont _font )
		{
			image = _image ;

			minFilter = MGL.GL_LINEAR ;
			magFilter = MGL.GL_LINEAR ;

			uWrap = MGL.GL_CLAMP_TO_EDGE ;
			vWrap = MGL.GL_REPEAT ;
		}

		@Override
		public IUniform.Type getType()
		{
			return IUniform.Type.SAMPLER2D ;
		}

		public void reset()
		{
			image = null ;
		}
	}
}
