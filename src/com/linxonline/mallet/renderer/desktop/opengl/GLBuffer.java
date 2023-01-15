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
import com.linxonline.mallet.renderer.BoolUniform ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Logger ;

public class GLBuffer
{
	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFFFF ;
	public final static int PRIMITIVE_EXPANSION = 1 ;

	public final static int VBO_VAR_BYTE_SIZE = 4 ;
	public final static int IBO_VAR_BYTE_SIZE = 4 ;

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

	protected static boolean generateUniforms( final GLProgram _glProgram, final Program _program, final List<IUniform> _toFill )
	{
		_toFill.clear() ;
		for( JSONProgram.UniformMap tuple : _glProgram.program.getUniforms() )
		{
			final IUniform uniform = _program.getUniform( tuple.getRight() ) ;
			switch( uniform.getType() )
			{
				case INT32        :
				case UINT32       :
				case FLOAT32      :
				case FLOAT64      :
				case FLOAT32_VEC2 :
				case FLOAT32_VEC3 :
				case FLOAT32_VEC4 :
				{
					Logger.println( "Build uniform type not implemented: " + uniform.getType(), Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				case FLOAT32_MAT4 :
				{
					_toFill.add( uniform ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final MalletTexture texture = ( MalletTexture )uniform ;
					if( texture == null )
					{
						Logger.println( "Requires texture: " + texture.toString(), Logger.Verbosity.MAJOR ) ;
					}

					final GLImage glTexture = GLRenderer.getTexture( texture ) ;
					if( glTexture == null )
					{
						return false ;
					}

					_toFill.add( new Texture( glTexture, texture ) ) ;
					break ;
				}
				case FONT         :
				{
					final MalletFont font = ( MalletFont )uniform ;
					final GLFont glFont = GLRenderer.getFont( font ) ;
					final GLImage texture = glFont.getTexture() ;

					_toFill.add( new Texture( texture, font ) ) ;
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

	protected static boolean loadUniforms( final GLProgram _program, final List<IUniform> _uniforms )
	{
		int textureUnit = 0 ;

		final int size = _uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final IUniform uniform = _uniforms.get( i ) ;
			switch( uniform.getType() )
			{
				case INT32        :
				case UINT32       :
				case FLOAT32      :
				case FLOAT64      :
				case FLOAT32_VEC2 :
				case FLOAT32_VEC3 :
				case FLOAT32_VEC4 :
				{
					Logger.println( "Load uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				{
					final BoolUniform val = ( BoolUniform )uniform ;
					MGL.glUniform1i( _program.inUniforms[i], val.getState() ? 1 : 0) ;
					break ;
				}
				case FLOAT32_MAT4 :
				{
					final Matrix4 m = ( Matrix4 )uniform ;
					final float[] matrix = m.matrix ;

					MGL.glUniformMatrix4fv( _program.inUniforms[i], 1, true, matrix, 0 ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final Texture texture = ( Texture )uniform ;
					final GLImage image = texture.image ;

					MGL.glActiveTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, image.textureIDs[0] ) ;
					MGL.glUniform1i( _program.inUniforms[i], textureUnit ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, texture.uWrap ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, texture.vWrap ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, texture.magFilter ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, texture.minFilter ) ;

					textureUnit += 1 ;
					break ;
				}
				case FONT         :
				{
					final Texture texture = ( Texture )uniform ;
					final GLImage image = texture.image ;

					MGL.glActiveTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, image.textureIDs[0] ) ;
					MGL.glUniform1i( _program.inUniforms[i], textureUnit ) ;

					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_LINEAR ) ;
					MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_LINEAR ) ;

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
		final int size = _storages.size() ;
		for( int i = 0; i < size ; ++i )
		{
			final GLStorage storage = _storages.get( i ) ;
			//System.out.println( name + " BindBase: " + i + " StorageID: " + glStorage.id[0] ) ;
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

	private static class Texture implements IUniform
	{
		private final IUniform.Type type ;
		public final GLImage image ;

		public final int minFilter ;
		public final int magFilter ;
		public final int uWrap ;
		public final int vWrap ;

		public Texture( final GLImage _image, final MalletTexture _texture )
		{
			image = _image ;
			minFilter = calculateMinFilter( _texture.getMinificationFilter() ) ;
			magFilter = calculateMagFilter( _texture.getMaxificationFilter() ) ;

			uWrap = calculateWrap( _texture.getUWrap() ) ;
			vWrap = calculateWrap( _texture.getVWrap() ) ;

			type = IUniform.Type.SAMPLER2D ;
		}

		public Texture( final GLImage _image, final MalletFont _font )
		{
			image = _image ;

			minFilter = -1 ;
			magFilter = -1 ;

			uWrap = -1 ;
			vWrap = -1 ;

			type = IUniform.Type.FONT ;
		}

		private int calculateMagFilter( MalletTexture.Filter _filter )
		{
			switch( _filter )
			{
				default          : return MGL.GL_LINEAR ;
				case LINEAR      : return MGL.GL_LINEAR ;
				case NEAREST     : return MGL.GL_NEAREST ;
			}
		}

		private int calculateMinFilter( MalletTexture.Filter _filter )
		{
			switch( _filter )
			{
				default          : return MGL.GL_LINEAR ;
				case MIP_LINEAR  : return MGL.GL_LINEAR_MIPMAP_LINEAR ;
				case MIP_NEAREST : return MGL.GL_NEAREST_MIPMAP_NEAREST ;
				case LINEAR      : return MGL.GL_LINEAR ;
				case NEAREST     : return MGL.GL_NEAREST ;
			}
		}

		private int calculateWrap( MalletTexture.Wrap _wrap )
		{
			switch( _wrap )
			{
				default         :
				case REPEAT     : return MGL.GL_REPEAT ;
				case CLAMP_EDGE : return MGL.GL_CLAMP_TO_EDGE ;
			}
		}

		@Override
		public IUniform.Type getType()
		{
			return type ;
		}
	}
}
