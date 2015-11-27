package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.nio.* ;

import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class GLGeometryUploader
{
	private final int[] indicies ;
	private final float[] verticies ;

	private final IntBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private final ArrayList<GLBuffer> buffers = new ArrayList<GLBuffer>() ;
	private final Vector3 temp = new Vector3() ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = new int[_indexSize] ;
		verticies = new float[_vboSize] ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _vboSize * 4 ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _indexSize * 4 ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;
	}

	public void draw( final GL3 _gl, final Matrix4 _modelViewProjection )
	{
		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _gl, _modelViewProjection ) ;
		}
	}

	public void upload( final GL3 _gl, final GLRenderer.GLRenderData _data, final Matrix4 _matrix )
	{
		final GLBuffer buffer = getBuffer( _data ) ;
		if( buffer != null )
		{
			buffer.upload( _gl, _data.getShape(), _matrix ) ;
		}
	}

	public void reset()
	{
		for( final GLBuffer buffer : buffers )
		{
			buffer.reset() ;
		}
	}

	protected void uploadIndex( final GL3 _gl, final GLGeometry _handler, final Shape _shape )
	{
		final int[] index = _shape.indicies ;
		final int indexOffset = _handler.amountVertexUsedBytes / _handler.vertexStrideBytes ;

		int j = 0 ;
		int offset = _handler.amountIndexUsedBytes ;
		for( int i = 0; i < index.length; i++ )
		{
			//System.out.println( "Index: " + index[i] + " With Offset: " + ( indexOffset + index[i] ) ) ;
			indicies[j++] = indexOffset + index[i] ;
			if( j >= indicies.length )
			{
				indexBuffer.put( indicies ) ;
				indexBuffer.position( 0 ) ;

				final int indexBufferLengthInBytes = indicies.length * 4 ;
				_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, offset, indexBufferLengthInBytes, indexBuffer ) ;
				//GLRenderer.handleError( "Index Buffer Sub Data: ", _gl ) ;

				j = 0 ;
				offset += indexBufferLengthInBytes ;
			}
		}

		if( j > 0 )
		{
			indexBuffer.put( indicies ) ;
			indexBuffer.position( 0 ) ;

			final int indexBufferLengthInBytes = j * 4 ;
			_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, offset, indexBufferLengthInBytes, indexBuffer ) ;
			//GLRenderer.handleError( "Index Buffer Sub Data: ", _gl ) ;
		}
	}

	protected void uploadVBO( final GL3 _gl, final GLGeometry _handler, final Shape _shape, final Matrix4 _matrix )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVertexSize() ;

		int inc = 0 ;
		int offset = _handler.amountVertexUsedBytes ;

		for( int i = 0; i < verticiesSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case NORMAL :
					case POINT  :
					{
						final Vector3 point = _shape.getPoint( i, j ) ;
						Matrix4.multiply( point, _matrix, temp ) ;
						verticies[inc++] = temp.x ;
						verticies[inc++] = temp.y ;
						verticies[inc++] = temp.z ;
						break ;
					}
					case COLOUR :
					{
						final MalletColour colour = _shape.getColour( i, j ) ;
						verticies[inc++] = getABGR( colour ) ;
						break ;
					}
					case UV     :
					{
						final Vector2 uv = _shape.getUV( i, j ) ;
						verticies[inc++] = uv.x ;
						verticies[inc++] = uv.y ;
						break ;
					}
				}
			}

			if( ( inc + vertexSize ) >= verticies.length )
			{
				vertexBuffer.put( verticies ) ;
				vertexBuffer.position( 0 ) ;

				final int vertexLengthInBytes = inc * 4 ;
				_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, offset, vertexLengthInBytes, vertexBuffer ) ;
				//GLRenderer.handleError( "Vertex Buffer Sub Data: ", _gl ) ;

				inc = 0 ;
				offset += vertexLengthInBytes ;
			}
		}
		
		if( inc > 0 )
		{
			vertexBuffer.put( verticies ) ;
			vertexBuffer.position( 0 ) ;

			final int vertexLengthInBytes = inc * 4 ;
			_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, offset, vertexLengthInBytes, vertexBuffer ) ;
			//GLRenderer.handleError( "Vertex Buffer Sub Data: ", _gl ) ;
		}
	}

	/**
		Find a GLBuffer that supports the texture, swivel, and layer
		of the datat passed in.
		If a GLBuffer doesn't exist create one.
	*/
	private GLBuffer getBuffer( final GLRenderer.GLRenderData _data )
	{
		for( final GLBuffer buffer : buffers )
		{
			if( buffer.isSupported( _data ) == true )
			{
				return buffer ;
			}
		}

		final GLBuffer buffer = new GLBuffer( _data, indicies.length * 4, verticies.length * 4 ) ;
		buffers.add( buffer ) ;
		return buffer ;
	}

	private static VertexAttrib[] constructVertexAttrib( final Shape.Swivel[] _swivel )
	{
		final VertexAttrib[] attributes = new VertexAttrib[_swivel.length] ;

		int offset = 0 ;
		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case POINT  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.VERTEX_ARRAY, 3, GL3.GL_FLOAT, false, offset ) ;
					offset += 3 * 4 ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.COLOUR_ARRAY, 4, GL3.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * 4 ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.TEXTURE_COORD_ARRAY0, 2, GL3.GL_FLOAT, false, offset ) ;
					offset += 2 * 4 ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.NORMAL_ARRAY, 3, GL3.GL_FLOAT, false, offset ) ;
					offset += 3 * 4 ;
					break ;
				}
			}
		}

		return attributes ;
	}

	private static int calculateVertexSize( final Shape.Swivel[] _swivel )
	{
		int size = 0 ;
		for( int j = 0; j < _swivel.length; j++ )
		{
			switch( _swivel[j] )
			{
				case POINT  : size += 3 ; break ;
				case COLOUR : size += 1 ; break ;
				case UV     : size += 2 ; break ;
				case NORMAL : size += 3 ; break ;
			}
		}

		return size ;
	}

	public static class VertexAttrib
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
	} ;

	/**
		Handles the geometry and index buffers for 
		a particular set of vertex attributes and style.
		GLBuffer will generate more GLGeometry buffers 
		when the existing buffers are full.
	*/
	public class GLBuffer implements GeometryInterface
	{
		private final Shape.Swivel[] shapeSwivel ;
		private final Shape.Style shapeStyle ;

		private final VertexAttrib[] attributes ;
		private final int style ;					// OpenGL GL_TRIANGLES, GL_LINES, 
		private final int indexLengthBytes ;
		private final int vertexLengthBytes ;
		private final int vertexStrideBytes ;		// Specifies the byte offset between verticies

		private final GLProgram program ;
		private final int textureID ;				// -1 represent no texture in use
		private final int layer ;

		private final ArrayList<GLGeometry> buffers = new ArrayList<GLGeometry>() ;

		public GLBuffer( final GLRenderer.GLRenderData _data,
						 final int _indexLengthBytes,
						 final int _vertexLengthBytes )
		{
			final Shape shape = _data.getShape() ;
			final Shape.Swivel[] swivel = shape.getSwivel() ;

			shapeSwivel = Arrays.copyOf( swivel, swivel.length ) ;
			attributes = constructVertexAttrib( shapeSwivel ) ;
			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = calculateVertexSize( shapeSwivel ) * 4 ;

			final Texture<GLImage> texture = _data.getTexture() ;
			textureID = ( texture != null ) ? texture.getImage().textureIDs[0] : -1 ;
			layer = _data.getLayer() ;
			program = _data.getProgram() ;

			shapeStyle = shape.getStyle() ;
			switch( shapeStyle )
			{
				case LINES      : style = GL2.GL_LINES ;      break ;
				case LINE_STRIP : style = GL2.GL_LINE_STRIP ; break ;
				case FILL       : style = GL2.GL_TRIANGLES ;  break ;
				default         : style = GL2.GL_LINES ;      break ;
			}

			buffers.add( new GLGeometry( style,
										 indexLengthBytes,
										 vertexLengthBytes,
										 vertexStrideBytes ) ) ;
		}

		public void draw( final GL3 _gl, final Matrix4 _modelViewProjection )
		{
			if( program == null )
			{
				System.out.println( "No program specified..." ) ;
				return ;
			}

			_gl.glUseProgram( program.id[0] ) ;		//GLRenderer.handleError( "Use Program", _gl ) ;

			final int inMVPMatrix = _gl.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;		//GLRenderer.handleError( "Get Matrix Handle", _gl ) ;
			_gl.glUniformMatrix4fv( inMVPMatrix, 1, true, _modelViewProjection.matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;

			if( textureID != -1 )
			{
				_gl.glActiveTexture( GL3.GL_TEXTURE0 + 0 ) ;		//GLRenderer.handleError( "Activate Texture", _gl ) ;
				_gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;	//GLRenderer.handleError( "Bind Texture", _gl ) ;
				_gl.glEnable( GL3.GL_BLEND ) ;						//GLRenderer.handleError( "Enable Blend", _gl ) ;
				_gl.glBlendFunc( GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA ) ;	//GLRenderer.handleError( "Set Blend Func", _gl ) ;
			}

			GLGeometryUploader.enableVertexAttributes( _gl, attributes ) ;
			for( final GLGeometry geometry : buffers )
			{
				_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Draw Bind Index: ", _gl ) ;
				_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Draw Bind Vertex: ", _gl ) ;

				GLGeometryUploader.prepareVertexAttributes( _gl, attributes, vertexStrideBytes ) ;
				//System.out.println( "Index: " + geometry.getUsedIndexBytes() + " Vertex: " + geometry.getUsedVertexBytes() ) ;
				_gl.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.GL_UNSIGNED_INT, 0 ) ;
				//GLRenderer.handleError( "Draw Elements: ", _gl ) ;
			}
			GLGeometryUploader.disableVertexAttributes( _gl, attributes ) ;

			_gl.glUseProgram( 0 ) ;			//GLRenderer.handleError( "Disable Program", _gl ) ;
			_gl.glDisable( GL3.GL_BLEND ) ;	//GLRenderer.handleError( "Disable Blend", _gl ) ;
		}

		public void upload( final GL3 _gl, final Shape _shape, final Matrix4 _matrix )
		{
			for( final GLGeometry geometry : buffers )
			{
				// Upload the data to the buffer 
				// if it returns succesfully then the data 
				// was uploaded.
				if( geometry.upload( _gl, _shape, _matrix ) == true )
				{
					return ;
				}
			}

			expand() ;
			upload( _gl, _shape, _matrix ) ;
		}

		public void reset()
		{
			for( final GLGeometry geometry : buffers )
			{
				geometry.reset() ;
			}
		}

		/**
			Determine whether or not this GLBuffer supports
			the requirements of the GLRenderData.
			GLBuffers will batch together similar content to 
			improve rendering performance.
			They will use layer, texture, shape swivel and style
			to determine if the buffer can support the data.
		*/
		public boolean isSupported( final GLRenderer.GLRenderData _data )
		{
			final Shape shape = _data.getShape() ;
			if( shapeStyle != shape.getStyle() )
			{
				return false ;
			}

			if( program != _data.getProgram() )
			{
				return false ;
			}

			if( textureID > 0 )
			{
				final Texture<GLImage> texture = _data.getTexture() ;
				if( texture == null )
				{
					return false ;
				}

				final GLImage image = texture.getImage() ;
				if( image.textureIDs[0] != textureID )
				{
					return false ;
				}
			}

			if( layer != _data.getLayer() )
			{
				return false ;
			}

			final Shape.Swivel[] sw = shape.getSwivel() ;
			if( shapeSwivel.length != sw.length )
			{
				return false ;
			}

			for( int i = 0; i < sw.length; i++ )
			{
				if( shapeSwivel[i] != sw[i] )
				{
					return false ;
				}
			}

			return true ;
		}

		private void expand()
		{
			buffers.add( new GLGeometry( style,
										 indexLengthBytes,
										 vertexLengthBytes,
										 vertexStrideBytes ) ) ;
		}

		@Override
		public void destroy()
		{
			for( final GLGeometry buffer : buffers )
			{
				buffer.destroy() ;
			}
		}
	}

	public class GLGeometry implements GeometryInterface
	{
		private final int style ;
		private final int indexLengthBytes ;
		private final int vertexLengthBytes ;
		private final int vertexStrideBytes ;		// Specifies the byte offset between verticies

		public final int[] indexID ;
		public final int[] vboID ;

		public int amountIndexUsedBytes  = 0 ;		// How much of buffer has been used
		public int amountVertexUsedBytes = 0 ;		// How much of buffer has been used

		public GLGeometry( final int _style,
						   final int _indexLengthBytes,
						   final int _vertexLengthBytes,
						   final int _vertexStrideBytes )
		{
			style = _style ;
			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = _vertexStrideBytes ;

			GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
			final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;

			indexID = GLModelManager.genIndexID( gl ) ;	//GLRenderer.handleError( "Gen Index: ", gl ) ;
			vboID = GLModelManager.genVBOID( gl ) ;		//GLRenderer.handleError( "Gen VBO: ", gl ) ;

			gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;	//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			gl.glBufferData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexLengthBytes, null, GL3.GL_DYNAMIC_DRAW ) ;	//GLRenderer.handleError( "Upload Data: ", gl ) ;

			gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vboID[0] ) ;		//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			gl.glBufferData( GL3.GL_ARRAY_BUFFER, vertexLengthBytes, null, GL3.GL_DYNAMIC_DRAW ) ;		//GLRenderer.handleError( "Upload Data: ", gl ) ;

			GLRenderer.getCanvas().getContext().release() ;
		}

		public boolean upload( final GL3 _gl, final Shape _shape, final Matrix4 _matrix )
		{
			final int availableIndex = indexLengthBytes - amountIndexUsedBytes ;
			final int shapeIndexBytes = _shape.getIndexSize() * 4 ;
			if( shapeIndexBytes > availableIndex )
			{
				//System.out.println( "Not enough Index space..." ) ;
				return false ;
			}

			final int availableVertex = vertexLengthBytes - amountVertexUsedBytes ;
			final int shapeVertexBytes = _shape.getVertexSize() * vertexStrideBytes ;
			if( shapeVertexBytes > availableVertex )
			{
				//System.out.println( "Not enough Vertex space..." ) ;
				return false ;
			}

			_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, getIndexID() ) ;		//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( _gl, this, _shape ) ;
			GLGeometryUploader.this.uploadVBO( _gl, this, _shape, _matrix ) ;

			amountIndexUsedBytes += shapeIndexBytes ;
			amountVertexUsedBytes += shapeVertexBytes ;
			return true ;
		}

		public int getIndexID()
		{
			return indexID[0] ;
		}

		public int getVBOID()
		{
			return vboID[0] ;
		}

		public int getIndexLength()
		{
			return amountIndexUsedBytes / 4 ;
		}

		public int getUsedIndexBytes()
		{
			return amountIndexUsedBytes ;
		}

		public int getUsedVertexBytes()
		{
			return amountVertexUsedBytes ;
		}

		public int getStyle()
		{
			return style ;
		}

		public int getStride()
		{
			return vertexStrideBytes ;
		}

		public void reset()
		{
			amountIndexUsedBytes  = 0 ;
			amountVertexUsedBytes = 0 ;
		}

		public void destroy()
		{
			reset() ;
			GLModelManager.unbind( this ) ;
		}
	}

	protected static void enableVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			_gl.glEnableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts, final int _stride )
	{
		for( VertexAttrib att : _atts )
		{
			_gl.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}
	
	protected static void disableVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			_gl.glDisableVertexAttribArray( att.index ) ;
		}
	}

	private static float getABGR( final MalletColour _colour )
	{
		final byte[] colour = new byte[4] ;
		colour[0] = _colour.colours[MalletColour.ALPHA] ;
		colour[1] = _colour.colours[MalletColour.BLUE] ;
		colour[2] = _colour.colours[MalletColour.GREEN] ;
		colour[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( colour, 0, 4 ) ;
	}
}