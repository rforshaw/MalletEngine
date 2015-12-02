package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Arrays ;
import java.nio.* ;

import android.opengl.GLES20 ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.DrawRequestType ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class GLGeometryUploader
{
	protected final static ObjectCache<Location> locationCache = new ObjectCache<Location>( Location.class ) ;

	private final short[] indicies ;
	private final float[] verticies ;

	private final ShortBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private final HashMap<GLRenderer.GLRenderData, GLBuffer> lookup = new HashMap<GLRenderer.GLRenderData, GLBuffer>() ;
	private final ArrayList<GLBuffer> buffers = new ArrayList<GLBuffer>() ;
	private final Vector3 temp = new Vector3() ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = new short[_indexSize] ;
		verticies = new float[_vboSize] ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _vboSize * 4 ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _indexSize * 2 ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asShortBuffer() ;
	}

	/**
		Draw the uploaded geometry.
	*/
	public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		//System.out.println( "Buffers: " + buffers.size() ) ;
		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _worldProjection, _uiProjection ) ;
		}
	}

	/**
		Upload geometry and have it prepared for drawing.
	*/
	public void upload( final GLRenderer.GLRenderData _data )
	{
		GLBuffer buffer = lookup.get( _data ) ;
		if( buffer != null )
		{
			if( buffer.isSupported( _data ) == true )
			{
				// If the buffer is still supported in the buffer 
				// it was previously loaded into then update it.
				buffer.upload( _data ) ;
				return ;
			}
			else
			{
				remove( _data ) ;
			}
		}

		buffer = getSupportedBuffer( _data ) ;
		lookup.put( _data, buffer ) ;
		buffer.upload( _data ) ;
	}

	public void remove( final GLRenderer.GLRenderData _data )
	{
		final GLBuffer buffer = lookup.remove( _data ) ;
		if( buffer != null )
		{
			buffer.remove( _data ) ;
		}
	}

	protected void uploadIndex( final Location _handler, final Shape _shape )
	{
		final GLGeometry geometry = _handler.getGeometry() ;

		final int[] index = _shape.indicies ;
		final int indexOffset = _handler.getVertexStart() / geometry.vertexStrideBytes ;

		for( int i = 0; i < index.length; i++ )
		{
			//System.out.println( "Index: " + index[i] + " With Offset: " + ( indexOffset + index[i] ) ) ;
			indicies[i] = ( short )( indexOffset + index[i] ) ;
		}

		indexBuffer.put( indicies ) ;
		indexBuffer.position( 0 ) ;

		GLES20.glBufferSubData( GLES20.GL_ELEMENT_ARRAY_BUFFER, _handler.getIndexStart(), _handler.getIndexLength(), indexBuffer ) ;
		//GLRenderer.handleError( "Index Buffer Sub Data: ", _gl ) ;
	}

	protected void uploadVBO( final Location _handler, final Shape _shape, final Matrix4 _matrix )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVertexSize() ;

		int inc = 0 ;
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
		}

		vertexBuffer.put( verticies ) ;
		vertexBuffer.position( 0 ) ;

		GLES20.glBufferSubData( GLES20.GL_ARRAY_BUFFER, _handler.getVertexStart(), _handler.getVertexLength(), vertexBuffer ) ;
		//GLRenderer.handleError( "Vertex Buffer Sub Data: ", _gl ) ;
	}

	/**
		Find a GLBuffer that supports the texture, swivel, and layer
		of the datat passed in.
		If a GLBuffer doesn't exist create one.
	*/
	private GLBuffer getSupportedBuffer( final GLRenderer.GLRenderData _data )
	{
		for( final GLBuffer buffer : buffers )
		{
			if( buffer.isSupported( _data ) == true )
			{
				return buffer ;
			}
		}

		// Use _data as initial parameters for this buffer.
		final GLBuffer buffer = new GLBuffer( _data, indicies.length * 2, verticies.length * 4 ) ;
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
					attributes[i] = new VertexAttrib( GLProgramManager.VERTEX_ARRAY, 3, GLES20.GL_FLOAT, false, offset ) ;
					offset += 3 * 4 ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.COLOUR_ARRAY, 4, GLES20.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * 4 ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.TEXTURE_COORD_ARRAY0, 2, GLES20.GL_FLOAT, false, offset ) ;
					offset += 2 * 4 ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.NORMAL_ARRAY, 3, GLES20.GL_FLOAT, false, offset ) ;
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

		private final GLProgram program ;			// What shader should be used
		private final int textureID ;				// -1 represent no texture in use
		private final int layer ;					// Defines the 2D layer the geometry resides on
		private final boolean ui ;					// Is the buffer used for UI or world space?
		private final boolean isText ;				// Is the buffer to be used for text?

		private GLProgram stencilProgram         = null ;	// Stencil is applied to all geometry located in buffers
		private Shape stencilShape               = null ;
		private VertexAttrib[] stencilAttributes = null ;
		private Matrix4 stencilMatrix            = null ;

		private Location stencilLocation = null ;

		private final HashMap<GLRenderer.GLRenderData, Location> locations = new HashMap<GLRenderer.GLRenderData, Location>() ;
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
			textureID                      = ( texture != null ) ? texture.getImage().textureIDs[0] : -1 ;
			layer                          = _data.getLayer() ;
			program                        = _data.getProgram() ;
			ui                             = _data.isUI() ;
			isText                         = _data.type == DrawRequestType.TEXT ;

			shapeStyle = shape.getStyle() ;
			switch( shapeStyle )
			{
				case LINES      : style = GLES20.GL_LINES ;      break ;
				case LINE_STRIP : style = GLES20.GL_LINE_STRIP ; break ;
				case FILL       : style = GLES20.GL_TRIANGLES ;  break ;
				default         : style = GLES20.GL_LINES ;      break ;
			}

			setupStencil( _data ) ;
		}

		public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
		{
			if( program == null )
			{
				System.out.println( "No program specified..." ) ;
				return ;
			}

			final float[] matrix = ( ui == false ) ? _worldProjection.matrix : _uiProjection.matrix ;
			if( stencilLocation != null )
			{
				drawStencil( matrix ) ;
			}

			GLES20.glUseProgram( program.id[0] ) ;		//GLRenderer.handleError( "Use Program", _gl ) ;

			final int inMVPMatrix = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;		//GLRenderer.handleError( "Get Matrix Handle", _gl ) ;
			GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;

			if( textureID != -1 )
			{
				GLES20.glActiveTexture( GLES20.GL_TEXTURE0 + 0 ) ;			//GLRenderer.handleError( "Activate Texture", _gl ) ;
				GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureID ) ;	//GLRenderer.handleError( "Bind Texture", _gl ) ;
				GLES20.glEnable( GLES20.GL_BLEND ) ;						//GLRenderer.handleError( "Enable Blend", _gl ) ;
				GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA ) ;	//GLRenderer.handleError( "Set Blend Func", _gl ) ;
			}

			GLGeometryUploader.enableVertexAttributes( attributes ) ;
			//System.out.println( "Geometry Buffers: " + buffers.size() ) ;
			for( final GLGeometry geometry : buffers )
			{
				//System.out.println( "Geometry Allocated: " + geometry.getLocationSize() ) ;
				GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;		//GLRenderer.handleError( "Draw Bind Index: ", _gl ) ;
				GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Draw Bind Vertex: ", _gl ) ;

				GLGeometryUploader.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
				GLES20.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GLES20.GL_UNSIGNED_SHORT, 0 ) ;
				//GLRenderer.handleError( "Draw Elements: ", _gl ) ;
			}
			GLGeometryUploader.disableVertexAttributes( attributes ) ;

			GLES20.glUseProgram( 0 ) ;						//GLRenderer.handleError( "Disable Program", _gl ) ;
			GLES20.glDisable( GLES20.GL_BLEND ) ;			//GLRenderer.handleError( "Disable Blend", _gl ) ;
			GLES20.glDisable( GLES20.GL_STENCIL_TEST ) ;	//GLRenderer.handleError( "Disable Stencil", _gl ) ;

			if( isText == true )
			{
				clear() ;
			}
			//System.out.println( "End Draw" ) ;
		}

		private void drawStencil( final float[] _projectionMatrix )
		{
			GLES20.glUseProgram( stencilProgram.id[0] ) ;
		
			final int inMVPMatrix = GLES20.glGetUniformLocation( stencilProgram.id[0], "inMVPMatrix" ) ;		//GLRenderer.handleError( "Get Matrix Handle", _gl ) ;
			GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, _projectionMatrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;

			// Don't render the element to the colour buffer
			GLES20.glColorMask( false, false, false, false ) ;
			GLES20.glEnable( GLES20.GL_STENCIL_TEST ) ;

			GLES20.glStencilMask( 0xFF ) ;
			GLES20.glClear( GLES20.GL_STENCIL_BUFFER_BIT ) ;

			GLES20.glStencilFunc( GLES20.GL_NEVER, 1, 0xFF ) ;
			GLES20.glStencilOp( GLES20.GL_REPLACE, GLES20.GL_KEEP, GLES20.GL_KEEP ) ;

			GLGeometryUploader.enableVertexAttributes( stencilAttributes ) ;

			final GLGeometry geometry = stencilLocation.getGeometry() ;
			GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;		//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;					//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( stencilLocation, stencilShape ) ;
			GLGeometryUploader.this.uploadVBO( stencilLocation, stencilShape, stencilMatrix ) ;

			GLGeometryUploader.prepareVertexAttributes( stencilAttributes, geometry.getStride() ) ;
			GLES20.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GLES20.GL_UNSIGNED_SHORT, 0 ) ;
			//GLRenderer.handleError( "Draw Elements: " ) ;
			GLGeometryUploader.disableVertexAttributes( stencilAttributes ) ;

			GLES20.glColorMask( true, true, true, true ) ;		// Re-enable colour buffer
			GLES20.glStencilFunc( GLES20.GL_EQUAL, 1, 1 ) ;
			// continue rendering scene...
		}

		public void upload( final GLRenderer.GLRenderData _data )
		{
			final Location location = findLocation( _data ) ;
			final GLGeometry geometry = location.getGeometry() ;
			final Shape shape = _data.getShape() ;

			GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;		//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( location, shape ) ;
			GLGeometryUploader.this.uploadVBO( location, shape, _data.getPositionMatrix() ) ;
		}

		public void remove( final GLRenderer.GLRenderData _data )
		{
			final Location location = locations.remove( _data ) ;
			if( location != null )
			{
				location.getGeometry().remove( location ) ;
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
			else if( ui != _data.isUI() )
			{
				return false ;
			}
			else if( stencilShape != _data.getClipShape() )
			{
				//System.out.println( stencilShape + " : " + _data.getClipShape() ) ;
				return false ;
			}
			else if( program != _data.getProgram() )
			{
				return false ;
			}
			else if( isText != ( _data.type == DrawRequestType.TEXT ) )
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

		private Location findLocation( final GLRenderer.GLRenderData _data )
		{
			if( isText == false )
			{
				// If _data has already been added we return the location 
				// in which it resides.
				final Location location = locations.get( _data ) ;
				if( location != null )
				{
					return location ;
				}
			}

			// If it hasn't been added find a space for it within 
			// an existing geometry buffer.
			for( final GLGeometry geometry : buffers )
			{
				final Location location = geometry.findLocation( _data.getShape() ) ;
				if( location != null )
				{
					locations.put( _data, location ) ;
					return location ;
				}
			}

			// If no space exists create a new geometry buffer 
			// and repeat the finding process.
			// Note: If the object is larger than the buffer, 
			// this will infinity loop.
			expand() ;
			return findLocation( _data ) ;
		}

		/**
			A buffer can support one stencil.
		*/
		private void setupStencil( final GLRenderer.GLRenderData _data )
		{
			stencilShape = _data.getClipShape() ;
			if( stencilShape != null )
			{
				final Shape.Swivel[] swivel = stencilShape.getSwivel() ;
				stencilAttributes = constructVertexAttrib( swivel ) ;

				final int vertexStrideBytes = calculateVertexSize( swivel ) * 4 ;
				final int vertexBytes = stencilShape.getVertexSize() * vertexStrideBytes ;
				final int indexBytes  = stencilShape.getIndexSize() * 2 ;

				final GLGeometry geometry = new GLGeometry( GLES20.GL_TRIANGLES, indexBytes, vertexBytes, vertexStrideBytes ) ;
				stencilLocation = geometry.findLocation( stencilShape ) ;
				stencilProgram = _data.getStencilProgram() ;
				stencilMatrix = _data.getClipMatrix() ;
			}
		}

		private void clear()
		{
			locations.clear() ;
			for( final GLGeometry geometry : buffers )
			{
				geometry.clear() ;
			}
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

		private final ArrayList<Location> allocated = new ArrayList<Location>() ;

		public GLGeometry( final int _style,
						   final int _indexLengthBytes,
						   final int _vertexLengthBytes,
						   final int _vertexStrideBytes )
		{
			style = _style ;
			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = _vertexStrideBytes ;

			indexID = GLModelManager.genIndexID() ;	//GLRenderer.handleError( "Gen Index: ", gl ) ;
			vboID = GLModelManager.genVBOID() ;		//GLRenderer.handleError( "Gen VBO: ", gl ) ;

			GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;	//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			GLES20.glBufferData( GLES20.GL_ELEMENT_ARRAY_BUFFER, indexLengthBytes, null, GLES20.GL_DYNAMIC_DRAW ) ;	//GLRenderer.handleError( "Upload Data: ", gl ) ;

			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboID[0] ) ;		//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, vertexLengthBytes, null, GLES20.GL_DYNAMIC_DRAW ) ;		//GLRenderer.handleError( "Upload Data: ", gl ) ;
		}

		public Location findLocation( final Shape _shape )
		{
			final int availableIndex = indexLengthBytes - amountIndexUsedBytes ;
			final int shapeIndexBytes = _shape.getIndexSize() * 2 ;
			if( shapeIndexBytes > availableIndex )
			{
				//System.out.println( "Not enough Index space..." ) ;
				return null ;
			}

			final int availableVertex = vertexLengthBytes - amountVertexUsedBytes ;
			final int shapeVertexBytes = _shape.getVertexSize() * vertexStrideBytes ;
			if( shapeVertexBytes > availableVertex )
			{
				//System.out.println( "Not enough Vertex space..." ) ;
				return null ;
			}

			if( allocated.isEmpty() )
			{
				//System.out.println( "Index Length: " + shapeIndexBytes + " Vertex Length: " + shapeVertexBytes ) ;
				return createLocation( 0, shapeIndexBytes, 0, shapeVertexBytes ) ;
			}

			boolean foundIndex = false ;
			boolean foundVertex = false ;
			int locationIndexStart = 0 ;
			int locationVertexStart = 0 ;
			final int size = allocated.size() ;

			//System.out.println( "Searching for location.. " ) ;
			for( int i = 0; i < size; i++ )
			{
				final Location next = allocated.get( i ) ;
				if( foundIndex == false )
				{
					final int indexLen = next.getIndexStart() - locationIndexStart ;
					if( indexLen < shapeIndexBytes )
					{
						locationIndexStart = next.getIndexEnd() ;
					}
					else
					{
						foundIndex = true ;
					}
				}

				if( foundVertex == false )
				{
					final int vertexLen = next.getVertexStart() - locationVertexStart ;
					if( vertexLen < shapeVertexBytes )
					{
						locationVertexStart = next.getVertexEnd() ;
					}
					else
					{
						foundVertex = true ;
					}
				}

				if( foundIndex == true && foundVertex == true )
				{
					//System.out.println( "Found location... " ) ;
					return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
				}
			}

			final int indexLen = indexLengthBytes - locationIndexStart ;
			final int vertexLen = vertexLengthBytes - locationVertexStart ;
			if( indexLen > shapeIndexBytes && vertexLen > shapeVertexBytes )
			{
				//System.out.println( "Found location at end... " ) ;
				return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
			}

			return null ;		// If no space is available
		}

		private Location createLocation( final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			amountIndexUsedBytes += _indexLength ;
			amountVertexUsedBytes += _vertexLength ;

			final Location location = locationCache.get() ;
			location.set( this, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
			allocated.add( location ) ;
			return location ; 
		}

		public void remove( final Location _location )
		{
			if( allocated.remove( _location ) == true )
			{
				amountIndexUsedBytes -= _location.getIndexLength() ;
				amountVertexUsedBytes -= _location.getVertexLength() ;
				locationCache.reclaim( _location ) ;
			}
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
			return amountIndexUsedBytes / 2 ;
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

		public int getLocationSize()
		{
			return allocated.size() ;
		}
		
		/**
			Returns true if the GLGeometry contains any 
			vertex data, false otherwise.
			Can be used to determine whether the buffer can 
			be destroyed.
		*/
		public boolean containsGeometry()
		{
			return amountVertexUsedBytes > 0 ;
		}

		@Override
		public void destroy()
		{
			GLModelManager.unbind( this ) ;
		}

		private void clear()
		{
			amountIndexUsedBytes = 0 ;
			amountVertexUsedBytes = 0 ;
			for( final Location location : allocated )
			{
				locationCache.reclaim( location ) ;
			}
			allocated.clear() ;
		}
	}

	public static class Location implements Cacheable
	{
		private GLGeometry geometry = null ;
		private int indexStart = 0 ;
		private int indexLength = 0 ;
		private int vertexStart = 0 ;
		private int vertexLength = 0 ;

		public Location() {}

		private Location( final GLGeometry _geometry, final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			set( _geometry, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
		}

		public void set( final GLGeometry _geometry, final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			geometry = _geometry ;
			indexStart = _indexStart ;
			indexLength = _indexLength ;
			vertexStart = _vertexStart ;
			vertexLength = _vertexLength ;
		}

		public int getIndexStart()
		{
			return indexStart ;
		}

		public int getIndexEnd()
		{
			return indexStart + indexLength ;
		}

		public int getIndexLength()
		{
			return indexLength ;
		}

		public int getVertexStart()
		{
			return vertexStart ;
		}

		public int getVertexEnd()
		{
			return vertexStart + vertexLength ;
		}

		public int getVertexLength()
		{
			return vertexLength ;
		}

		@Override
		public void reset()
		{
			set( null, 0, 0, 0, 0 ) ;
		}

		public GLGeometry getGeometry()
		{
			return geometry ;
		}

		public String toString()
		{
			return "Index: " + indexStart + " Index Length: " + indexLength + " Vertex: " + vertexStart + " Vertex Length: " + vertexLength ;
		}
	}
	
	protected static void enableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glEnableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}
	
	protected static void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glDisableVertexAttribArray( att.index ) ;
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