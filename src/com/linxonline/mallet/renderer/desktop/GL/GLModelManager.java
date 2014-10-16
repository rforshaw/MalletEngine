package com.linxonline.mallet.renderer.desktop.GL ;

import javax.media.opengl.* ;
import java.util.HashMap ;

import java.nio.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;

public class GLModelManager extends AbstractManager
{
	@Override
	protected Resource createResource( final String _file )
	{
		System.out.println( "GLModelManager - createResource() not supported." ) ;
		System.out.println( "Will be supported once models formats are added." ) ;
		return null ;
	}

	public static void bind( final GLGeometry _geometry )
	{
		final GL2 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL2() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return ;
		}

		bindVBO( gl, _geometry ) ;
		bindIndex( gl, _geometry ) ;
	}

	public static void unbind( final GLGeometry _geometry )
	{
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL2 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL2() ;
		if( gl != null )
		{
			final int[] id = new int[1] ;
			id[0] = _geometry.vboID ;
			gl.glDeleteBuffers( 1, id, 0 ) ;

			id[0] = _geometry.indexID ;
			gl.glDeleteBuffers( 1, id, 0 ) ;
		}
		GLRenderer.getCanvas().getContext().release() ;
	}

	public static int glGenBuffers( GL2 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenBuffers( 1, id, 0 ) ;
		return id[0] ;
	}

	public static void bindVBO( final GL2 _gl, final GLGeometry _geometry )
	{
		final int vboID = GLModelManager.glGenBuffers( _gl ) ;
		_gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vboID ) ;
		_geometry.vboID = vboID ;

		GLModelManager.updateVBO( _gl, _geometry ) ;
	}

	public static void updateVBO( final GL2 _gl, final GLGeometry _geometry )
	{
		final int vertexBufferLength = _geometry.vertex.length * 4 ; 

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( vertexBufferLength ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		final FloatBuffer vertexBuffer = vertexByteBuffer.asFloatBuffer() ;
		vertexBuffer.put( _geometry.vertex ) ;
		vertexBuffer.flip() ;

		_gl.glBufferData( GL2.GL_ARRAY_BUFFER, vertexBufferLength, vertexBuffer, GL2.GL_DYNAMIC_DRAW ) ;
	}

	public static void bindIndex( final GL2 _gl, final GLGeometry _geometry )
	{
		final int indexID = GLModelManager.glGenBuffers( _gl ) ;
		_gl.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
		_geometry.indexID = indexID ;

		final int indexBufferLength = _geometry.index.length * 4 ; 

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( indexBufferLength ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		final IntBuffer indexBuffer = indexByteBuffer.asIntBuffer() ;
		indexBuffer.put( _geometry.index ) ;
		indexBuffer.flip() ;

		_gl.glBufferData( GL2.GL_ELEMENT_ARRAY_BUFFER, indexBufferLength, indexBuffer, GL2.GL_STATIC_DRAW ) ;
	}
}