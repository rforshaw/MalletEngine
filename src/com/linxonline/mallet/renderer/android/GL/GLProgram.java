package com.linxonline.mallet.renderer.android.GL ;

import java.util.List ;
import java.util.HashMap ;

import android.opengl.GLES30 ;

import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.util.Utility ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.texture.* ;
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
	/**
		Load mapped uniforms to GPU
	*/
	public enum Uniform
	{
		BOOL( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		INT( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		UINT( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		FLOAT( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		DOUBLE( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		VEC2( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		VEC3( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		VEC4( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				return false ;
			}

			public Class getUniformClass()
			{
				return null ;
			}

			public void reset() {}
		} ),
		MAT4( new UniformDelegate()
		{
			public boolean load( final ProgramMap _data, final int _index )
			{
				final GLProgram program               = ( GLProgram )_data.getProgram() ;
				final Tuple<String, Uniform> uniform = program.uniforms.get( _index ) ;
				final int inUniform                   = program.inUniforms[_index] ;

				final Matrix4 m = ( Matrix4 )_data.get( uniform.getLeft() ) ;
				GLES30.glUniformMatrix4fv( inUniform, 1, true, m.matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;
				return true ;
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

			public boolean load( final ProgramMap _data, final int _index )
			{
				final GLProgram program               = ( GLProgram )_data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;
				final int inUniform                   = program.inUniforms[_index] ;

				final MalletTexture texture = ( MalletTexture )_data.get( uniform.getLeft() ) ;
				final Texture<GLImage> glTexture = GLRenderer.getTexture( texture.getPath() ) ;
				if( glTexture == null )
				{
					return false ;
				}

				GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;				//GLRenderer.handleError( "Activate Texture", _gl ) ;
				GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, glTexture.getImage().textureIDs[0] ) ;		//GLRenderer.handleError( "Bind Texture", _gl ) ;
				textureUnit += 1 ;
				return true ;
			}

			public Class getUniformClass()
			{
				return MalletTexture.class ;
			}

			public void reset()
			{
				textureUnit = 0 ;
			}
		}  ),
		FONT( new UniformDelegate()
		{
			private int textureUnit = 0 ;

			public boolean load( final ProgramMap _data, final int _index )
			{
				final GLProgram program               = ( GLProgram )_data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;
				final int inUniform                   = program.inUniforms[_index] ;

				final MalletFont font = ( MalletFont )_data.get( uniform.getLeft() ) ;
				final GLFontMap fm = ( GLFontMap )font.font.getFont() ;
				final Texture<GLImage> texture = fm.getTexture() ;
				if( texture == null )
				{
					return false ;
				}

				GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;						//GLRenderer.handleError( "Activate Texture", _gl ) ;
				GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, texture.getImage().textureIDs[0] ) ;		//GLRenderer.handleError( "Bind Texture", _gl ) ;
				textureUnit += 1 ;
				return true ;
			}

			public Class getUniformClass()
			{
				return MalletFont.class ;
			}

			public void reset()
			{
				textureUnit = 0 ;
			}
		}  ) ;

		private final UniformDelegate delegate ;

		private Uniform( final UniformDelegate _delegate )
		{
			delegate = _delegate ;
		}

		/**
			Load the information being mapped at index location.
		*/
		public boolean load( final ProgramMap _data, final int _index )
		{
			return delegate.load( _data, _index ) ;
		}

		public void reset()
		{
			delegate.reset() ;
		}

		public boolean isValid( final Object _obj )
		{
			return delegate.getUniformClass().isInstance( _obj ) ;
		}

		protected static void resetAll()
		{
			Uniform.BOOL.reset() ;
			Uniform.INT.reset() ;
			Uniform.UINT.reset() ;
			Uniform.FLOAT.reset() ;
			Uniform.DOUBLE.reset() ;
			Uniform.VEC2.reset() ;
			Uniform.VEC3.reset() ;
			Uniform.VEC4.reset() ;
			Uniform.MAT4.reset() ;
			Uniform.SAMPLER2D.reset() ;
			Uniform.FONT.reset() ;
		}

		private interface UniformDelegate
		{
			public boolean load( final ProgramMap _data, final int _index ) ;

			public Class getUniformClass() ;

			public void reset() ;
		}
	}

	public final String name ;				// Unique Name
	public final int[] id = new int[1] ;	// GL Program ID

	// Model View Projection Matrix, doesn't need to be defined in *.jgl,
	// however it must be defined in atleast vertex shader. 
	public int inMVPMatrix = -1 ;
	public final int[] inUniforms ;			// Additional uniforms defined in *.jgl and shaders  
	public final int[] inAttributes ;		// Vertex swivel order defined in *.jgl

	public final List<GLShader> shaders ;
	public final List<Tuple<String, Uniform>> uniforms ;
	public final List<String> swivel ;

	public GLProgram( final String _name,
					  final List<GLShader> _shaders,
					  final List<Tuple<String, Uniform>> _uniforms,
					  final List<String> _swivel )
	{
		name = _name ;

		shaders  = _shaders ;
		uniforms = _uniforms ;
		swivel   = _swivel ;

		inUniforms   = ( uniforms != null ) ? new int[uniforms.size()] : new int[0] ;
		inAttributes = ( swivel != null )   ? new int[swivel.size()]   : new int[0] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}
	}

	/**
		A GL Program will have information that it requires 
		before it can be used effectively. 
	*/
	public boolean loadUniforms( final ProgramMap _data )
	{
		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, Uniform> uniform = uniforms.get( i ) ;
			if( uniform.getRight().load( _data, i ) == false )
			{
				Uniform.resetAll() ;
				return false ;
			}
		}

		Uniform.resetAll() ;
		return true ;
	}

	/**
		Ensure that the Mallet Program maps correctly with 
		the GL Program it is apparently associated with.
	*/
	public boolean isValidMap( final HashMap<String, Object> _map )
	{
		final int size = uniforms.size() ;
		final int diff = _map.size() - size ;
		if( diff != 0 )
		{
			if( diff > 0 )
			{
				Logger.println( "Mallet Program contains more uniforms than OpenGL Program expects", Logger.Verbosity.MINOR ) ;
			}
			if( diff < 0 )
			{
				Logger.println( "Mallet Program contains less uniforms than OpenGL Program expects", Logger.Verbosity.MINOR ) ;
			}
			return false ;
		}

		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, Uniform> map = uniforms.get( i ) ;
			final String name = map.getLeft() ;

			final Object obj = _map.get( name ) ;
			if( obj == null )
			{
				Logger.println( "OpenGL Program does not contain Map", Logger.Verbosity.MINOR ) ;
				return false ;
			}

			final Uniform type = map.getRight() ;
			if( type.isValid( obj ) == false )
			{
				Logger.println( "OpenGL Program does not align to Mallet Program", Logger.Verbosity.MINOR ) ;
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
