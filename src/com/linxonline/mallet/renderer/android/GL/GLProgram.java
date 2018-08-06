package com.linxonline.mallet.renderer.android.GL ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import android.opengl.GLES30 ;

import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.io.Resource ;
import com.linxonline.mallet.renderer.MalletFont ;
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
	/**
		Load mapped uniforms to GPU
	*/
	public enum Uniform
	{
		BOOL( new AUniform() ),
		INT( new AUniform() ),
		UINT( new AUniform() ),
		FLOAT( new AUniform() ),
		DOUBLE( new AUniform() ),
		VEC2( new AUniform() ),
		VEC3( new AUniform() ),
		VEC4( new AUniform() ),
		MAT4( new AUniform()
		{
			@Override
			public boolean build( final ProgramMap<GLProgram> _data, final int _index )
			{
				return true ;
			}

			@Override
			public boolean load( final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program              = _data.getProgram() ;
				final Tuple<String, Uniform> uniform = program.uniforms.get( _index ) ;
				final int inUniform                  = program.inUniforms[_index] ;

				final Matrix4 m = ( Matrix4 )_data.get( uniform.getLeft() ) ;
				GLES30.glUniformMatrix4fv( inUniform, 1, true, m.matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;
				return true ;
			}

			@Override
			public Class getUniformClass()
			{
				return Matrix4.class ;
			}
		} ),
		SAMPLER2D( new UniformDelegate()
		{
			private int textureUnit = 0 ;

			@Override
			public boolean build( final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final MalletTexture texture = ( MalletTexture )_data.get( uniform.getLeft() ) ;
				final GLImage glTexture = GLRenderer.getTexture( texture.getPath() ) ;
				if( glTexture == null )
				{
					return false ;
				}

				_data.set( uniform.getLeft(), glTexture ) ;
				return true ;
			}

			public boolean load( final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final GLImage texture = ( GLImage )_data.get( uniform.getLeft() ) ;

				GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;				//GLRenderer.handleError( "Activate Texture", _gl ) ;
				GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, texture.textureIDs[0] ) ;		//GLRenderer.handleError( "Bind Texture", _gl ) ;
				textureUnit += 1 ;
				return true ;
			}

			public void clean( final Set<String> _keys, final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final MalletTexture texture = ( MalletTexture )_data.get( uniform.getLeft() ) ;
				_keys.add( texture.getPath() ) ;
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

			public boolean build( final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final MalletFont font = ( MalletFont )_data.get( uniform.getLeft() ) ;
				final GLFont glFont = GLRenderer.getFont( font ) ;

				final GLImage texture = glFont.getTexture() ;
				if( texture == null )
				{
					return false ;
				}

				_data.set( uniform.getLeft(), texture ) ;
				return true ;
			}

			public boolean load( final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final GLImage texture = ( GLImage )_data.get( uniform.getLeft() ) ;

				GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;						//GLRenderer.handleError( "Activate Texture", _gl ) ;
				GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, texture.textureIDs[0] ) ;		//GLRenderer.handleError( "Bind Texture", _gl ) ;
				textureUnit += 1 ;
				return true ;
			}

			public void clean( final Set<String> _keys, final ProgramMap<GLProgram> _data, final int _index )
			{
				final GLProgram program               = _data.getProgram() ;
				final Tuple<String, Uniform> uniform  = program.uniforms.get( _index ) ;

				final MalletFont font = ( MalletFont )_data.get( uniform.getLeft() ) ;
				_keys.add( font.getID() ) ;
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

		public boolean build( final ProgramMap<GLProgram> _map, final int _index )
		{
			return delegate.build( _map, _index ) ;
		}

		/**
			Load the information being mapped at index location.
		*/
		public boolean load( final ProgramMap<GLProgram> _map, final int _index )
		{
			return delegate.load( _map, _index ) ;
		}

		public void clean( final Set<String> _keys, final ProgramMap<GLProgram> _map, final int _index )
		{
			delegate.clean( _keys, _map, _index ) ;
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

		private static class AUniform implements UniformDelegate
		{
			public boolean build( final ProgramMap<GLProgram> _data, final int _index ) { return false ; }

			public boolean load( final ProgramMap<GLProgram> _data, final int _index ) { return false ; }

			public void clean( final Set<String> _keys, final ProgramMap<GLProgram> _data, final int _index ) {}

			public Class getUniformClass() { return null ; }

			public void reset() {}
		}

		private interface UniformDelegate
		{
			public boolean build( final ProgramMap<GLProgram> _data, final int _index ) ;

			public boolean load( final ProgramMap<GLProgram> _data, final int _index ) ;

			public void clean( final Set<String> _keys, final ProgramMap<GLProgram> _data, final int _index ) ;

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
		The program-map defined by the user is not efficient 
		for loading uniforms as it specifies MalletTextures and 
		MalletFonts instead of GLImage and GLFont.

		Construct a program-map that references render specific 
		resources instead.

		If we fail to build an efficient map then return null.
	*/
	public ProgramMap<GLProgram> buildMap( final ProgramMap<GLProgram> _map )
	{
		final ProgramMap<GLProgram> map = new ProgramMap<GLProgram>( _map ) ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, Uniform> uniform = uniforms.get( i ) ;
			if( uniform.getRight().build( map, i ) == false )
			{
				return null ;
			}
		}

		map.setDirty( false ) ;
		return map ;
	}

	/**
		A GL Program will have information that it requires 
		before it can be used effectively. 
	*/
	public boolean loadUniforms( final ProgramMap<GLProgram> _data )
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
		Should only be used on a ProgramMap created by the user.
		Will crash if used with a ProgramMap built using buildMap().

		The program map contains the references to resources 
		that are potential managed by the renderer.

		Using the uniforms loop over the map and record 
		within _activeKeys the keys for those resources.
	*/
	public void getUsedResources( final Set<String> _activeKeys, final ProgramMap<GLProgram> _data )
	{
		_activeKeys.add( name ) ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, Uniform> uniform = uniforms.get( i ) ;
			uniform.getRight().clean( _activeKeys, _data, i ) ;
		}
	}

	/**
		Ensure that the Mallet Program maps correctly with 
		the GL Program it is apparently associated with.
	*/
	public boolean isValidMap( final Map<String, Object> _map )
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
	public long getMemoryConsumption()
	{
		return 0L ;
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}
