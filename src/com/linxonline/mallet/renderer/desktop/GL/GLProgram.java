package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.ProgramData ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public class GLProgram extends Resource
{
	public enum DataType
	{
		BOOL( null ),
		INT( null ),
		UINT( null ),
		FLOAT( null ),
		DOUBLE( null ),
		VEC2( null ),
		VEC3( null ),
		VEC4( null ),
		MAT4( new UniformDelegate()
		{
			public void load( final GL3 _gl, final ProgramData _data, final int _index )
			{
				final GLProgram program               = ( GLProgram )_data.getProgram() ;
				final Tuple<String, DataType> uniform = program.uniforms.get( _index ) ;
				final int inUniform                   = program.inUniforms[_index] ;

				final Matrix4 m = ( Matrix4 )_data.get( uniform.getLeft() ) ;
				_gl.glUniformMatrix4fv( inUniform, 1, true, m.matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;
			}

			public Class getUniformClass()
			{
				return Matrix4.class ;
			}

			public void reset() {}
		} ),
		SAMPLER2D( new UniformDelegate()
		{
			private int textureUnit = 0 ;

			public void load( final GL3 _gl, final ProgramData _data, final int _index )
			{
				final GLProgram program               = ( GLProgram )_data.getProgram() ;
				final Tuple<String, DataType> uniform = program.uniforms.get( _index ) ;
				final int inUniform                   = program.inUniforms[_index] ;

				_gl.glUniform1i( inUniform, textureUnit ) ;
				_gl.glActiveTexture( GL3.GL_TEXTURE0 + textureUnit ) ;				//GLRenderer.handleError( "Activate Texture", _gl ) ;
				_gl.glBindTexture( GL.GL_TEXTURE_2D, program.textureID[textureUnit] ) ;		//GLRenderer.handleError( "Bind Texture", _gl ) ;
				textureUnit += 1 ;
			}

			public Class getUniformClass()
			{
				return MalletTexture.class ;
			}

			public void reset()
			{
				textureUnit = 0 ;
			}
		}  ) ;

		private final UniformDelegate delegate ;

		private DataType( final UniformDelegate _delegate )
		{
			delegate = _delegate ;
		}

		public void load( final GL3 _gl, final ProgramData _data, final int _index )
		{
			delegate.load( _gl, _data, _index ) ;
		}

		public void reset()
		{
			delegate.reset() ;
		}

		public boolean isValid( final Object _obj )
		{
			return delegate.getUniformClass().isInstance( _obj ) ;
		}

		private interface UniformDelegate
		{
			public void load( final GL3 _gl, final ProgramData _data, final int _index ) ;

			public Class getUniformClass() ;

			public void reset() ;
		}
	}

	public final String name ;
	public final int[] id = new int[1] ;

	public int inMVPMatrix = -1 ;			// Must always exist in shader
	public final int[] inUniforms ;
	public final int[] inAttributes ;

	public int[] textureID = null ;

	public final ArrayList<GLShader> shaders ;
	public final ArrayList<Tuple<String, DataType>> uniforms ;
	public final ArrayList<String> swivel ;

	public GLProgram( final String _name,
					  final ArrayList<GLShader> _shaders,
					  final ArrayList<Tuple<String, DataType>> _uniforms,
					  final ArrayList<String> _swivel )
	{
		name = _name ;

		shaders         = _shaders ;
		uniforms        = _uniforms ;
		swivel          = _swivel ;

		inUniforms        = ( uniforms != null )        ? new int[uniforms.size()]        : new int[0] ;
		inAttributes      = ( swivel != null )          ? new int[swivel.size()]          : new int[0] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}
	}

	public void load( final GL3 _gl, final ProgramData _data )
	{
		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, DataType> uniform = uniforms.get( i ) ;
			uniform.getRight().load( _gl, _data, i ) ;
		}

		/*DataType.BOOL.reset() ;
		DataType.INT.reset() ;
		DataType.UINT.reset() ;
		DataType.FLOAT.reset() ;
		DataType.DOUBLE.reset() ;
		DataType.VEC2.reset() ;
		DataType.VEC3.reset() ;
		DataType.VEC4.reset() ;
		DataType.MAT4.reset() ;*/
		DataType.SAMPLER2D.reset() ;
	}

	public boolean isValidMap( final HashMap<String, Object> _map )
	{
		final int size = uniforms.size() ;
		if( _map.size() != size )
		{
			Logger.println( "OpenGL Program does not map to Mallet Program", Logger.Verbosity.MINOR ) ;
			return false ;
		}

		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, DataType> map = uniforms.get( i ) ;
			final String name = map.getLeft() ;

			final Object obj = _map.get( name ) ;
			if( obj == null )
			{
				Logger.println( "OpenGL Program does not contain Map", Logger.Verbosity.MINOR ) ;
				return false ;
			}

			final DataType type = map.getRight() ;
			if( type.isValid( obj ) == false )
			{
				Logger.println( "OpenGL Program does not map to Mallet Program", Logger.Verbosity.MINOR ) ;
				return false ;
			}
		}

		return true ;
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}
