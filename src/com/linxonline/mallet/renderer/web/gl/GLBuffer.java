package com.linxonline.mallet.renderer.web.gl ;

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

import com.linxonline.mallet.util.caches.MemoryPool ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Logger ;

public class GLBuffer
{
	protected final static Matrix4 IDENTITY = new Matrix4() ;

	private final static MemoryPool<Texture> TEXTURES = new MemoryPool<Texture>( () -> new Texture() ) ;

	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFF ;
	public final static int PRIMITIVE_EXPANSION = 1 ;

	public final static int VBO_VAR_BYTE_SIZE = 4 ;
	public final static int IBO_VAR_BYTE_SIZE = 2 ;

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

	protected static boolean generateProgramUniforms( final GLProgram _glProgram, final Program _program, final List<IUniform> _toFill )
	{
		final List<JSONProgram.Uniform> uniforms = _glProgram.program.getUniforms() ;
		if( uniforms.isEmpty() )
		{
			return true ;
		}

		for( IUniform uniform : _toFill )
		{
			// We don't want to create more texture uniforms
			// than needed, use a cache.
			if( uniform.getType() == IUniform.Type.SAMPLER2D )
			{
				final Texture texture = ( Texture )uniform ;
				texture.reset() ;

				TEXTURES.reclaim( texture ) ;
			}
		}

		_toFill.clear() ;
		for( JSONProgram.Uniform tuple : uniforms )
		{
			final IUniform uniform = _program.getUniform( tuple.getName() ) ;
			if( uniform == null )
			{
				Logger.println( tuple.getName() + " not specified on program object.", Logger.Verbosity.MAJOR ) ;
				return false ;
			}
			
			switch( uniform.getType() )
			{
				case FLOAT64      :
				{
					Logger.println( "Build uniform type not implemented: " + uniform.getType(), Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				case UINT32       :
				case INT32        :
				case FLOAT32      :
				{
					_toFill.add( uniform ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final Texture texture = ( Texture )uniform ;
					if( texture == null )
					{
						Logger.println( "Requires texture: " + texture.toString(), Logger.Verbosity.MAJOR ) ;
					}

					final GLImage glTexture = GLRenderer.getTexture( texture ) ;
					if( glTexture == null )
					{
						return false ;
					}

					final Texture tex = TEXTURES.take() ;
					tex.set( glTexture, texture ) ;

					_toFill.add( tex ) ;
					break ;
				}
				case FONT         :
				{
					final Font font = ( Font )uniform ;
					final GLFont glFont = GLRenderer.getFont( font ) ;
					final GLImage texture = glFont.getTexture() ;

					final Texture tex = TEXTURES.take() ;
					tex.set( texture, font ) ;

					_toFill.add( tex ) ;
					break ;
				}
				case STRUCT       :
				{
					break ;
				}
				case ARRAY        :
				{
					break ;
				}
				case UNKNOWN      :
				default           : return false ;
			}
		}

		return true ;
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

	/**
		Load the uniforms expcted to be specified at the program level.
	*/
	protected boolean loadProgramUniforms( final GLProgram _program, final List<IUniform> _uniforms )
	{
		textureUnit = 0 ;

		final int size = _uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final IUniform uniform = _uniforms.get( i ) ;
			switch( uniform.getType() )
			{
				case FLOAT64      :
				{
					Logger.println( "Load uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				{
					final BoolUniform val = ( BoolUniform )uniform ;
					MGL.uniform1i( _program.inUniforms[i], val.getState() ? 1 : 0) ;
					break ;
				}
				case UINT32       :
				{
					final UIntUniform vec = ( UIntUniform )uniform ;
					final int num = vec.fill( 0, intTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Uint uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1ui( _program.inUniforms[i], intTemp[0] ) ; break ;
						case 2  : MGL.uniform2ui( _program.inUniforms[i], intTemp[0], intTemp[1] ) ; break ;
						case 3  : MGL.uniform3ui( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2] ) ; break ;
						case 4  : MGL.uniform4ui( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2], intTemp[3] ) ; break ;
					}
					break ;
				}
				case INT32        :
				{
					final IntUniform vec = ( IntUniform )uniform ;
					final int num = vec.fill( 0, intTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Int uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1i( _program.inUniforms[i], intTemp[0] ) ; break ;
						case 2  : MGL.uniform2i( _program.inUniforms[i], intTemp[0], intTemp[1] ) ; break ;
						case 3  : MGL.uniform3i( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2] ) ; break ;
						case 4  : MGL.uniform4i( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2], intTemp[3] ) ; break ;
					}
					break ;
				}
				case FLOAT32      :
				{
					final FloatUniform vec = ( FloatUniform )uniform ;
					final int num = vec.fill( 0, floatTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Float uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1f( _program.inUniforms[i], floatTemp[0] ) ; break ;
						case 2  : MGL.uniform2f( _program.inUniforms[i], floatTemp[0], floatTemp[1] ) ; break ;
						case 3  : MGL.uniform3f( _program.inUniforms[i], floatTemp[0], floatTemp[1], floatTemp[2] ) ; break ;
						case 4  : MGL.uniform4f( _program.inUniforms[i], floatTemp[0], floatTemp[1], floatTemp[2], floatTemp[3] ) ; break ;
						case 16 : MGL.uniformMatrix4fv( _program.inUniforms[i], true, floatTemp ) ; break ;
					}
					break ;
				}
				case SAMPLER2D    :
				case FONT         :
				{
					final Texture texture = ( Texture )uniform ;
					final GLImage image = texture.image ;

					MGL.activeTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.bindTexture( MGL.GL_TEXTURE_2D, image.textureIDs[0] ) ;
					MGL.uniform1i( _program.inUniforms[i], textureUnit ) ;

					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, texture.uWrap ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, texture.vWrap ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, texture.magFilter ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, texture.minFilter ) ;

					textureUnit += 1 ;
					break ;
				}
				case STRUCT       :
				{
					break ;
				}
				case ARRAY        :
				{
					break ;
				}
				case UNKNOWN      :
				default           : return false ;
			}
		}

		return true ;
	}

	/**
		Load the uniforms expcted to be specified at the draw level.
		Draw object cannot override program level uniforms.
	*/
	protected boolean loadDrawUniforms( final GLProgram _program, final Draw _draw )
	{
		int textureUnitOffset = textureUnit ;
		
		final List<JSONProgram.Uniform> uniforms = _program.program.getDrawUniforms() ;
		final int size = uniforms.size() ;
		for( int i = 0; i < size; ++i )
		{
			final JSONProgram.Uniform tuple = uniforms.get( i ) ;

			final IUniform uniform = _draw.getUniform( tuple.getName() ) ;
			if( uniform == null )
			{
				Logger.println( tuple.getName() + " not specified on draw object.", Logger.Verbosity.MAJOR ) ;
				return false ;
			}

			switch( uniform.getType() )
			{
				case FLOAT64      :
				{
					Logger.println( "Load uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				{
					final BoolUniform val = ( BoolUniform )uniform ;
					MGL.uniform1i( _program.inDrawUniforms[i], val.getState() ? 1 : 0) ;
					break ;
				}
				case UINT32       :
				{
					final UIntUniform vec = ( UIntUniform )uniform ;
					final int num = vec.fill( 0, intTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Uint uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1ui( _program.inUniforms[i], intTemp[0] ) ; break ;
						case 2  : MGL.uniform2ui( _program.inUniforms[i], intTemp[0], intTemp[1] ) ; break ;
						case 3  : MGL.uniform3ui( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2] ) ; break ;
						case 4  : MGL.uniform4ui( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2], intTemp[3] ) ; break ;
					}
					break ;
				}
				case INT32        :
				{
					final IntUniform vec = ( IntUniform )uniform ;
					final int num = vec.fill( 0, intTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Int uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1i( _program.inUniforms[i], intTemp[0] ) ; break ;
						case 2  : MGL.uniform2i( _program.inUniforms[i], intTemp[0], intTemp[1] ) ; break ;
						case 3  : MGL.uniform3i( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2] ) ; break ;
						case 4  : MGL.uniform4i( _program.inUniforms[i], intTemp[0], intTemp[1], intTemp[2], intTemp[3] ) ; break ;
					}
					break ;
				}
				case FLOAT32      :
				{
					final FloatUniform vec = ( FloatUniform )uniform ;
					final int num = vec.fill( 0, floatTemp ) ;
					switch( num )
					{
						default :
						{
							Logger.println( "Float uniform - unsupported component count.", Logger.Verbosity.MAJOR ) ;
						}
						case 1  : MGL.uniform1f( _program.inUniforms[i], floatTemp[0] ) ; break ;
						case 2  : MGL.uniform2f( _program.inUniforms[i], floatTemp[0], floatTemp[1] ) ; break ;
						case 3  : MGL.uniform3f( _program.inUniforms[i], floatTemp[0], floatTemp[1], floatTemp[2] ) ; break ;
						case 4  : MGL.uniform4f( _program.inUniforms[i], floatTemp[0], floatTemp[1], floatTemp[2], floatTemp[3] ) ; break ;
						case 16 : MGL.uniformMatrix4fv( _program.inUniforms[i], true, floatTemp ) ; break ;
					}

					break ;
				}
				case SAMPLER2D    :
				{
					final Texture texture = ( Texture )uniform ;
					if( texture == null )
					{
						Logger.println( "Requires texture: " + texture.toString(), Logger.Verbosity.MAJOR ) ;
					}

					final GLImage glTexture = GLRenderer.getTexture( texture ) ;
					if( glTexture == null )
					{
						return false ;
					}

					MGL.activeTexture( MGL.GL_TEXTURE0 + textureUnitOffset ) ;
					MGL.bindTexture( MGL.GL_TEXTURE_2D, glTexture.textureIDs[0] ) ;
					MGL.uniform1i( _program.inDrawUniforms[i], textureUnitOffset ) ;

					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, GLImage.calculateWrap( texture.getUWrap() ) ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, GLImage.calculateWrap( texture.getVWrap() ) ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, GLImage.calculateMagFilter( texture.getMaxificationFilter() )  ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, GLImage.calculateMinFilter( texture.getMinificationFilter() ) ) ;

					textureUnitOffset += 1 ;
					break ;
				}
				case FONT         :
				{
					final Texture texture = ( Texture )uniform ;
					final GLImage image = texture.image ;

					MGL.activeTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.bindTexture( MGL.GL_TEXTURE_2D, image.textureIDs[0] ) ;
					MGL.uniform1i( _program.inUniforms[i], textureUnit ) ;

					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_LINEAR ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_LINEAR ) ;

					textureUnit += 1 ;
					break ;
				}
				case UNKNOWN      :
				default           : return false ;
			}
		}

		return true ;
	}

	public static void bindBuffers( final List<GLStorage> _storages )
	{
		/*final int size = _storages.size() ;
		for( int i = 0; i < size ; ++i )
		{
			final GLStorage storage = _storages.get( i ) ;
			MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, storage.id[0] ) ;
		}*/
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
			MGL.enableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.vertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}

	protected static void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.disableVertexAttribArray( att.index ) ;
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

	private static final class Texture implements IUniform
	{
		public GLImage image ;

		public int minFilter ;
		public int magFilter ;
		public int uWrap ;
		public int vWrap ;

		public Texture() {}

		public void set( final GLImage _image, final Texture _texture )
		{
			image = _image ;
			minFilter = GLImage.calculateMinFilter( _texture.getMinificationFilter() ) ;
			magFilter = GLImage.calculateMagFilter( _texture.getMaxificationFilter() ) ;

			uWrap = GLImage.calculateWrap( _texture.getUWrap() ) ;
			vWrap = GLImage.calculateWrap( _texture.getVWrap() ) ;
		}

		public void set( final GLImage _image, final Font _font )
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
