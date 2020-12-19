package com.linxonline.mallet.renderer.web.gl ;

import java.util.Iterator ;
import java.util.List ;
import java.util.Arrays ;
import java.nio.* ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLProgram ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;
import org.teavm.jso.typedarrays.Uint8Array ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.ProgramMap ;
import com.linxonline.mallet.renderer.font.Glyph ;

import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.util.OrderedInsert ;
import com.linxonline.mallet.util.ISort ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class GLGeometryUploader
{
	protected final ObjectCache<Location> locationCache = new ObjectCache<Location>( Location.class ) ;

	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFFFF ;
	private final static int PRIMITIVE_EXPANSION = 0/*1*/ ;			// Added to the end of a shapes index array

	private final static int VBO_VAR_BYTE_SIZE = 4 ;
	private final static int IBO_VAR_BYTE_SIZE = 2 ;
	private WebGLProgram programID = null ;

	private final Int16Array indicies ;
	private final Float32Array verticies ;

	private final List<IBuffer> buffers = MalletList.<IBuffer>newList() ;							// Available GLBuffers

	private final MalletColour shapeColour = new MalletColour() ;
	private final Vector2 uv = new Vector2() ;
	private final Vector3 point = new Vector3() ;
	private final Vector3 temp = new Vector3() ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = Int16Array.create( _indexSize ) ;
		verticies = Float32Array.create( _vboSize ) ;
	}

	/**
		Draw the uploaded geometry.
	*/
	public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		final int size = buffers.size() ;
		for( int i = 0; i < size; i++ )
		{
			final IBuffer buffer = buffers.get( i ) ;
			buffer.draw( _worldProjection, _uiProjection ) ;
		}
	}

	/**
		Upload geometry and have it prepared for drawing.
	*/
	public void upload( final GLDrawData _data )
	{
		IBuffer buffer = _data.getBuffer() ;
		if( buffer != null )
		{
			if( buffer.isSupported( _data ) == true )
			{
				// If the data is still supported in the buffer 
				// it was previously loaded into then update it.
				buffer.upload( _data ) ;
				return ;
			}
			else
			{
				remove( _data ) ;
			}
		}

		// If the draw data no longer fits with the previously 
		// used buffer find or create a new buffer that matches.
		buffer = getSupportedBuffer( _data ) ;
		_data.setBuffer( buffer ) ;

		buffer.upload( _data ) ;
	}

	/**
		Force all draw objects that are currently uploaded 
		to be flagged for redrawing.
	*/
	public void forceUpdate()
	{
		final int size = buffers.size() ;
		for( int i = 0; i < size; i++ )
		{
			buffers.get( i ).forceUpdate() ;
		}
	}
	
	/**
		Find the GLBuffer/GLGeometry that the GLRenderData resides in.
		Remove it from the buffers, and pack the index buffer.
	*/
	public void remove( final GLDrawData _data )
	{
		IBuffer buffer = _data.getBuffer() ;
		if( buffer != null )
		{
			buffer.remove( _data ) ;
			_data.setBuffer( null ) ;
			_data.setLocation( null ) ;

			// When a program is removed completely from the 
			// buffer we need to reset the dirty flag to ensure 
			// it is added to the correct buffer - else the 
			// ProgramMap check is never kicked off and the 
			// data is added to the wrong buffer.
			final ProgramMap<GLProgram> program = ( ProgramMap<GLProgram> )_data.getProgram() ;
			program.dirty() ;
		}
	}

	/**
		Destroy all buffers allocated in OpenGL.
	*/
	public void shutdown()
	{
		for( final IBuffer buffer : buffers )
		{
			buffer.destroy() ;
		}
		buffers.clear() ;
	}

	/**
		Remove any GLBuffers that do not contain any geometry.
	*/
	public void clean()
	{
		final Iterator<IBuffer> i = buffers.iterator() ;
		while( i.hasNext() == true )
		{
			final IBuffer buffer = i.next() ;
			buffer.clean() ;
			if( buffer.containsGeometry() == false )
			{
				buffer.destroy() ;
				i.remove() ;
			}
		}
	}

	protected void uploadIndex( final Location _handler, final Shape _shape )
	{
		final GLGeometry geometry = _handler.getGeometry() ;

		final int indexOffset = _handler.getVertexStart() / geometry.vertexStrideBytes ;

		final int indiciesLength = indicies.getLength() ;

		int increment = 0 ;
		int indexStartBytes = _handler.getIndexStart() ;

		final int size = _shape.getIndicesSize ;
		for( int i = 0; i < size; i++ )
		{
			indicies.set( increment++, ( short )( indexOffset + _shape.getIndex( i ) ) ) ;

			if( increment >= indiciesLength )
			{
				// The memory required for the Location may be 
				// greater than the buffer - upload now. 
				MGL.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, indexStartBytes, indicies ) ;

				indexStartBytes += indiciesLength * IBO_VAR_BYTE_SIZE ;
				increment = 0 ;
			}
		}

		//indicies.set( increment++, ( short )PRIMITIVE_RESTART_INDEX ) ;
		// Repeat the last index to create a dead triangle.
		//indicies.set( increment++, ( short )indexLast ) ;

		if( increment > 0 )
		{
			final Int16Array tmp = Int16Array.create( indicies.getBuffer(), 0, increment ) ;
			MGL.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, indexStartBytes, tmp ) ;
			//GLRenderer.handleError( "Index Buffer Sub Data: " ) ;
		}
	}

	protected void uploadVBO( final Location _handler, final Shape _shape, final Matrix4 _matrix )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVerticesSize() ;

		final Uint8Array byteVersion = Uint8Array.create( verticies.getBuffer() ) ;

		int increment = 0 ;
		int vertexStartBytes = _handler.getVertexStart() ;

		for( int i = 0; i < verticiesSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case NORMAL :
					case POINT  :
					{
						_shape.getVector3( i, j, point ) ;
						Matrix4.multiply( point, _matrix, temp ) ;
						verticies.set( increment++, temp.x ) ;
						verticies.set( increment++, temp.y ) ;
						verticies.set( increment++, temp.z ) ;
						break ;
					}
					case COLOUR :
					{
						//verticies.set( increment++, _shape.getFloat( i, j ) ) ;
						_shape.getColour( i, j, shapeColour ) ;
						setColour( increment++, shapeColour, byteVersion ) ;
						break ;
					}
					case UV     :
					{
						_shape.getVector2( i, j, uv ) ;
						verticies.set( increment++, uv.x ) ;
						verticies.set( increment++, uv.y ) ;
						break ;
					}
				}
			}

			final int size = verticies.getLength() ;
			if( ( increment + vertexSize ) >= size )
			{
				// The memory required for the Location may be 
				// greater than the buffer - upload now.
				MGL.bufferSubData( GL3.ARRAY_BUFFER, vertexStartBytes, verticies ) ;
				//GLRenderer.handleError( "Vertex Buffer Sub Data: " ) ;

				vertexStartBytes += size * VBO_VAR_BYTE_SIZE ;
				increment = 0 ;
			}
		}

		if( increment > 0 )
		{
			final Float32Array tmp = Float32Array.create( verticies.getBuffer(), 0, increment ) ;
			MGL.bufferSubData( GL3.ARRAY_BUFFER, vertexStartBytes, tmp ) ;
			//GLRenderer.handleError( "Vertex Buffer Sub Data: " ) ;
		}
	}

	/**
		Find a GLBuffer that supports the texture, swivel, and layer
		of the datat passed in.
		If a GLBuffer doesn't exist create one.
	*/
	private IBuffer getSupportedBuffer( final GLDrawData _data )
	{
		if( buffers.isEmpty() == false )
		{
			final int size = buffers.size() ;
			for( int i = 0; i < size; i++ )
			{
				final IBuffer buffer = buffers.get( i ) ;
				if( buffer.isSupported( _data ) == true )
				{
					return buffer ;
				}
			}
		}

		IBuffer buffer = null ;
		switch( _data.getMode() )
		{
			case BASIC   :
			{
				buffer = new GLGeometryBuffer( _data, indicies.getLength() * IBO_VAR_BYTE_SIZE, verticies.getLength() * VBO_VAR_BYTE_SIZE ) ;
				break ;
			}
			case TEXT    :
			{
				buffer = new GLTextBuffer( _data, indicies.getLength() * IBO_VAR_BYTE_SIZE, verticies.getLength() * VBO_VAR_BYTE_SIZE ) ;
				break ;
			}
			case STENCIL :
			{
				buffer = new GLStencilBuffer( _data, indicies.getLength() * IBO_VAR_BYTE_SIZE, verticies.getLength() * VBO_VAR_BYTE_SIZE ) ;
				final IBuffer endBuffer = new GLEndBuffer( buffer, _data.getEndOrder() )
				{
					public void end()
					{
						//System.out.println( "Disable Stencil: " + sortValue() ) ;
						MGL.disable( GL3.STENCIL_TEST ) ;		//GLRenderer.handleError( "Disable Stencil", _gl ) ;
					}
				} ;

				OrderedInsert.insert( endBuffer, buffers ) ;
				break ;
			}
			case DEPTH   : break ;
		}

		OrderedInsert.insert( buffer, buffers ) ;
		return buffer ;
	}

	private static VertexAttrib[] constructVertexAttrib( final Shape.Swivel[] _swivel, final GLProgram _program )
	{
		final VertexAttrib[] attributes = new VertexAttrib[_swivel.length] ;

		int offset = 0 ;
		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case POINT  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, GL3.FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 4, GL3.UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 2, GL3.FLOAT, false, offset ) ;
					offset += 2 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, GL3.FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
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

	public interface IBuffer extends ISort
	{
		public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection ) ;

		public void upload( final GLDrawData _data ) ;
		public void remove( final GLDrawData _data ) ;
		public void forceUpdate() ;

		public boolean containsGeometry() ;

		public boolean isSupported( final GLDrawData _data ) ;

		public void clean() ;
		public void destroy() ;

	}

	/**
		Handles the geometry and index buffers for 
		a particular set of vertex attributes and style.
		GLBuffer will generate more GLGeometry buffers 
		when the existing buffers are full.
	*/
	public abstract class GLBuffer implements IBuffer
	{
		protected final GLDrawData.Mode mode ;
		protected Shape.Swivel[] shapeSwivel ;
		protected Shape.Style shapeStyle ;

		protected VertexAttrib[] attributes ;
		protected int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES, 
		protected int indexLengthBytes  = -1 ;
		protected int vertexLengthBytes = -1 ;
		protected int vertexStrideBytes = -1 ;			// Specifies the byte offset between verticies

		// Used to determine if a GLDrawData's ProgramMap 
		// should be added to this buffer.
		protected ProgramMap<GLProgram> baseProgram ;
		// Removes the need for looking up the textures 
		// referenced by MalletTexture and MalletFont.
		protected ProgramMap<GLProgram> program ;

		protected final int layer ;								// Defines the 2D layer the geometry resides on
		protected final boolean ui ;							// Is the buffer used for UI or world space?

		protected final List<GLGeometry> buffers = MalletList.<GLGeometry>newList() ;

		public GLBuffer( final GLDrawData _data,
						 final int _indexLengthBytes,
						 final int _vertexLengthBytes )
		{
			this( _data.getMode(),
				  ( ProgramMap<GLProgram> )_data.getProgram(),
				  _data.isUI(),
				  _data.getDrawShape(),
				  _data.getOrder(),
				  _indexLengthBytes,
				  _vertexLengthBytes ) ;
		}

		public GLBuffer( final GLDrawData.Mode _mode,
						 final ProgramMap<GLProgram> _program,
						 final boolean _ui,
						 final Shape _shape,
						 final int _layer,
						 final int _indexLengthBytes,
						 final int _vertexLengthBytes )
		{
			mode = _mode ;
			baseProgram = new ProgramMap<GLProgram>( _program ) ;

			layer = _layer ;
			ui = _ui ;

			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;

			if( _shape != null )
			{
				initShape( _shape ) ;
			}
		}

		/**
			Implement how the buffer should be expanded 
			and what potential restrictions there are.
		*/
		protected abstract void expand( final int _indexLengthBytes, final int _vertexLengthBytes ) ;

		protected void initShape( final Shape _shape )
		{
			final Shape.Swivel[] swivel = _shape.getSwivel() ;
			shapeSwivel = Arrays.copyOf( swivel, swivel.length ) ;
			attributes = constructVertexAttrib( shapeSwivel, baseProgram.getProgram() ) ;

			vertexStrideBytes = calculateVertexSize( shapeSwivel ) * VBO_VAR_BYTE_SIZE ;

			shapeStyle = _shape.getStyle() ;
			switch( shapeStyle )
			{
				case LINES      : style = GL3.LINES ;      break ;
				case LINE_STRIP : style = GL3.LINE_STRIP ; break ;
				case FILL       : style = GL3.TRIANGLES ;  break ;
				default         : style = GL3.LINES ;      break ;
			}
		}

		@Override
		public void forceUpdate()
		{
			final int size = buffers.size() ;
			for( int i = 0; i < size; i++ )
			{
				buffers.get( i ).forceUpdate() ;
			}
		}

		@Override
		public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
		{
			//System.out.println( "Draw Buffer: " + sortValue() ) ;
			if( baseProgram == null )
			{
				System.out.println( "No program specified..." ) ;
				return ;
			}

			final GLProgram glProgram = baseProgram.getProgram() ;
			if( glProgram == null )
			{
				System.out.println( "No OpenGL program specified..." ) ;
				return ;
			}

			if( program == null )
			{
				program = glProgram.buildMap( baseProgram ) ;
				if( program == null )
				{
					return ;
				}
			}

			//if( glProgram.id[0] != programID )
			{
				// Only call glUseProgram if the last program used 
				// doesn't match what we want.
				// Ordering draws by program could improve rendering performance.
				programID = glProgram.id[0] ;
				MGL.useProgram( programID ) ;										//GLRenderer.handleError( "Use Program", _gl ) ;
			}

			final float[] matrix = ( ui == false ) ? _worldProjection.matrix : _uiProjection.matrix ;
			MGL.uniformMatrix4fv( glProgram.inMVPMatrix, false, matrix ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;
			if( glProgram.loadUniforms( program ) == false )
			{
				// We failed to load all uniforms required for 
				// this buffer.
				return ;
			}

			GLGeometryUploader.enableVertexAttributes( attributes ) ;
			for( final GLGeometry geometry : buffers )
			{
				MGL.bindBuffer( GL3.ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Draw Bind Index: ", _gl ) ;
				MGL.bindBuffer( GL3.ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Draw Bind Vertex: ", _gl ) ;

				GLGeometryUploader.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
				MGL.drawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.UNSIGNED_SHORT, 0 ) ;
				//GLRenderer.handleError( "Draw Elements: ", GL3 ) ;
			}
			GLGeometryUploader.disableVertexAttributes( attributes ) ;
		}

		@Override
		public void remove( final GLDrawData _data )
		{
			final Location location = _data.getLocation() ;
			if( location != null )
			{
				location.getGeometry().remove( location ) ;
				_data.setLocation( null ) ;
			}
		}

		@Override
		public boolean containsGeometry()
		{
			return buffers.isEmpty() == false ;
		}

		/**
			Determine whether or not this GLBuffer supports
			the requirements of the GLRenderData.

			GLBuffers will batch together similar content to 
			improve rendering performance.

			They will use layer, texture, shape swivel and style
			to determine if the buffer can support the data.
		*/
		public boolean isSupported( final GLDrawData _data )
		{
			if( mode != _data.getMode() )
			{
				return false ;
			}

			if( layer != _data.getOrder() )
			{
				return false ;
			}

			if( ui != _data.isUI() )
			{
				return false ;
			}

			if( isProgram( ( ProgramMap<GLProgram> )_data.getProgram() ) == false )
			{
				return false ;
			}

			if( isShape( _data.getDrawShape() ) == false )
			{
				return false ;
			}

			return true ;
		}

		protected boolean isShape( final Shape _shape )
		{
			if( shapeStyle != _shape.getStyle() )
			{
				return false ;
			}

			final Shape.Swivel[] sw = _shape.getSwivel() ;
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

		protected boolean isProgram( final ProgramMap<GLProgram> _program )
		{
			// Checking to see if the program matches up with 
			// the program used by the buffer is expensive.
			// We only check to see if the program is valid if it's 
			// flagged as dirty.
			// As only modified/new programs will be flagged as dirty.
			if( _program.isDirty() == true )
			{
				final boolean valid = baseProgram.equals( _program ) ;
				// The program should only be flagged as not dirty 
				// once a valid buffer has been found.
				_program.setDirty( valid ? false : true ) ;
				return valid ;
			}

			return true ;
		}

		/**
			Blank out geometry stored by this buffer.
			GLGeometry objects initialised still exist
			but are reset.
		*/
		private void clear()
		{
			for( final GLGeometry geometry : buffers )
			{
				geometry.clear() ;
			}
		}

		public void clean()
		{
			final Iterator<GLGeometry> i = buffers.iterator() ;
			while( i.hasNext() == true )
			{
				final GLGeometry geometry = i.next() ;
				if( geometry.containsGeometry() == false )
				{
					geometry.destroy() ;
					i.remove() ;
				}
			}
		}

		@Override
		public int sortValue()
		{
			return layer ;
		}

		/**
			Clear allocations and unbind the GLGeometry 
			objects in OpenMGL.
			Nothing should be left, purge it all.
		*/
		public void destroy()
		{
			shapeSwivel = null ;
			shapeStyle  = null ;
			attributes  = null ;
			baseProgram = null ;
			program     = null ;

			for( final GLGeometry geometry : buffers )
			{
				geometry.clear() ;
				geometry.destroy() ;
			}
			buffers.clear() ;
		}
	}

	/**
		Used by other buffers to disable an operation that 
		can span multiple other buffers.
		For example Stencil Buffers - a stencil can be applied 
		that affects the next buffers to be drawn, however, 
		not all buffers are to be affected by the stencil 
		operation.
	*/
	public abstract class GLEndBuffer implements IBuffer
	{
		private final IBuffer parent ;
		private final int layer ;

		public GLEndBuffer( IBuffer _parent, final int _layer )
		{
			parent = _parent ;
			layer = _layer ;
		}

		@Override
		public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
		{
			end() ;
		}

		public abstract void end() ;

		@Override
		public boolean isSupported( final GLDrawData _data )
		{
			return false ;
		}

		/**
			This Buffer should be cleaned up when the 
			parent buffer is being cleaned up.
		*/
		@Override
		public boolean containsGeometry()
		{
			return parent.containsGeometry() ;
		}

		@Override
		public int sortValue()
		{
			return layer ;
		}

		@Override
		public void upload( final GLDrawData _data ) {}
		
		@Override
		public void remove( final GLDrawData _data ) {}

		@Override
		public void forceUpdate() {}

		@Override
		public void clean() {}

		@Override
		public void destroy() {}
	}

	public class GLGeometryBuffer extends GLBuffer
	{
		public GLGeometryBuffer( final GLDrawData _data,
								 final int _indexLengthBytes,
								 final int _vertexLengthBytes )
		{
			super( _data, _indexLengthBytes, _vertexLengthBytes ) ;
		}

		@Override
		public void upload( final GLDrawData _data )
		{
			final Location location = findLocationGeometry( _data ) ;
			final GLGeometry geometry = location.getGeometry() ;
			final Shape shape = _data.getDrawShape() ;

			MGL.bindBuffer( GL3.ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;		//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			MGL.bindBuffer( GL3.ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( location, shape ) ;
			GLGeometryUploader.this.uploadVBO( location, shape, _data.getDrawMatrix() ) ;
		}

		private Location findLocationGeometry( final GLDrawData _data )
		{
			// If _data has already been added we return the location 
			// in which it resides.
			{
				final Location location = _data.getLocation() ;
				if( location != null )
				{
					return location ;
				}
			}

			// If it hasn't been added find a space for it within 
			// an existing geometry buffer.
			final int size = buffers.size() ;
			for( int i = 0; i < size; i++ )
			{
				final GLGeometry geometry = buffers.get( i ) ;
				final Location location = geometry.findLocation( _data ) ;
				if( location != null )
				{
					location.setData( _data ) ;
					_data.setLocation( location ) ;
					return location ;
				}
			}

			// If no space exists create a new geometry buffer 
			// and repeat the finding process.
			// Increase the buffer size if the geometry is too large.
			final Shape shape = _data.getDrawShape() ;
			final int shapeIndexBytes = ( shape.getIndicesSize + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
			final int indexBytes = ( indexLengthBytes > shapeIndexBytes ) ? indexLengthBytes : shapeIndexBytes ;

			final int shapeVertexBytes = shape.getVerticesSize() * vertexStrideBytes ;
			final int vertexBytes =  ( vertexLengthBytes > shapeVertexBytes ) ? vertexLengthBytes : shapeVertexBytes ;

			expand( indexBytes, vertexBytes ) ;
			return findLocationGeometry( _data ) ;
		}

		@Override
		protected void expand( final int _indexLengthBytes, final int _vertexLengthBytes )
		{
			buffers.add( new GLGeometry( style, _indexLengthBytes, _vertexLengthBytes, vertexStrideBytes )
			{
				@Override
				protected Location findLocation( final GLDrawData _data )
				{
					final Shape shape = _data.getDrawShape() ;
					return findLocation( shape.getIndicesSize, shape.getVerticesSize() ) ;
				}
			} ) ;
		}
	}

	public class GLStencilBuffer extends GLGeometryBuffer
	{
		public GLStencilBuffer( final GLDrawData _data,
								final int _indexLengthBytes,
								final int _vertexLengthBytes )
		{
			super( _data, _indexLengthBytes, _vertexLengthBytes ) ;
		}

		@Override
		public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
		{
			//System.out.println( "Enable Stencil: " + sortValue() ) ;
			MGL.enable( GL3.STENCIL_TEST ) ;

			MGL.stencilFunc( GL3.ALWAYS, 1, 0xFF ) ;
			MGL.stencilOp( GL3.KEEP, GL3.KEEP, GL3.REPLACE ) ;
			MGL.stencilMask( 0xFF ) ;

			MGL.colorMask( false, false, false, false ) ;
			MGL.depthMask( false ) ;

			MGL.clear( GL3.STENCIL_BUFFER_BIT ) ;

			super.draw( _worldProjection, _uiProjection ) ;		// Render geometry to stencil buffer

			MGL.stencilFunc( GL3.EQUAL, 1, 0xFF ) ;
			MGL.stencilMask( 0x00 ) ;

			MGL.colorMask( true, true, true, true ) ;		// Re-enable colour buffer
			MGL.depthMask( true ) ;

			// buffers called after this will now be 
			// affected by the stencil buffer.
		}
	}

	public class GLTextBuffer extends GLBuffer
	{
		private final MalletFont font ;
		private final MalletFont.Metrics metrics ;

		private GLFont glFont = null ;

		public GLTextBuffer( final GLDrawData _data,
							 final int _indexLengthBytes,
							 final int _vertexLengthBytes )
		{
			super( _data, _indexLengthBytes, _vertexLengthBytes ) ;

			font = baseProgram.get( "inTex0", MalletFont.class ) ;
			metrics = font.getMetrics() ;
		}

		@Override
		protected boolean isShape( final Shape _shape )
		{
			return ( _shape == null ) ? true : false ;
		}

		@Override
		public void upload( final GLDrawData _data )
		{
			//System.out.println( "Upload!" ) ;
			if( glFont == null )
			{
				glFont = GLRenderer.getFont( font ) ;
				initShape( glFont.getShapeWithChar( '\0' ) ) ;
			}

			final Location location = findLocationText( _data ) ;
			uploadText( location, _data ) ;
		}

		/**
			Determine whether or not the Location is large 
			enough to contain the text specified by the draw object.
			A developer can change the text on a whim, so the location 
			must be updated to take this into account.
		*/
		private boolean isCorrectSize( final GLDrawData _data, final Location _location )
		{
			final int length = _data.getTextEnd() - _data.getTextStart() ;
			final int shapeIndexBytes = ( ( length * 6 ) + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;

			//System.out.println( "Size: " + shapeIndexBytes + " " + _location.getIndexLength() ) ;
			return shapeIndexBytes == _location.getIndexLength() ;
		}

		private void uploadText( final Location _location, final GLDrawData _data )
		{
			final Uint8Array byteVersion = Uint8Array.create( verticies.getBuffer() ) ;

			final Matrix4 positionMatrix = _data.getDrawMatrix() ;
			final MalletColour colour = _data.getColour() ;

			final GLGeometry geometry = _location.getGeometry() ;
			MGL.bindBuffer( GL3.ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			MGL.bindBuffer( GL3.ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			final int initialIndexOffset = _location.getVertexStart() / geometry.vertexStrideBytes ;

			int indexInc = 0 ;
			int vertexInc = 0 ;

			int indexStartBytes = _location.getIndexStart() ;
			int vertexStartBytes = _location.getVertexStart() ;

			final StringBuilder text = _data.getText() ;
			final int start = _data.getTextStart() ;
			final int end = _data.getTextEnd() ;

			final int indiciesLength = indicies.getLength() ;
			final int verticiesLength = verticies.getLength() ;

			final int length = end - start ;
			for( int i = 0; i < length; i++ )
			{
				final char c = text.charAt( start + i ) ;

				final Glyph glyph = metrics.getGlyphWithChar( c ) ;
				final Shape shape = glFont.getShapeWithChar( c ) ;
				if( shape == null )
				{
					//System.out.println( "Missing: " + c + " Glyph: " + glyph ) ;
					// If a shape does not exist then the GLFont 
					// needs to be recreated as the MalletFont has a 
					// new glyph that is not yet represented.
					// We need to update all text as the texture co-ordinates 
					// used by the previously uploaded text will now be wrong.
					GLGeometryUploader.this.forceUpdate() ;
					continue ;
				}

				final Shape.Swivel[] swivel = shape.getSwivel() ;
				final int vertexSize = calculateVertexSize( swivel ) ;
				final int verticiesSize = shape.getVerticesSize() ;

				final int indexOffset = initialIndexOffset + ( i * 4 ) ;

				final int size = shape.getIndicesSize ; 
				for( int j = 0; j < size; j++ )
				{
					indicies.set( indexInc++, ( short )( indexOffset + shape.getIndex( j ) ) ) ;
					if( indexInc >= indiciesLength )
					{
						MGL.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, indexStartBytes, indicies ) ;
						//GLRenderer.handleError( "Index Buffer Sub Data: " ) ;

						indexStartBytes += indiciesLength * IBO_VAR_BYTE_SIZE ;
						indexInc = 0 ;
					}
				}

				for( int j = 0; j < verticiesSize; j++ )
				{
					for( int k = 0; k < swivel.length; k++ )
					{
						switch( swivel[k] )
						{
							case NORMAL :
							case POINT  :
							{
								shape.getVector3( j, k, point ) ;
								Matrix4.multiply( point, positionMatrix, temp ) ;
								verticies.set( vertexInc++, temp.x ) ;
								verticies.set( vertexInc++, temp.y ) ;
								verticies.set( vertexInc++, temp.z ) ;
								break ;
							}
							case COLOUR :
							{
								// GLDrawData colour overrides Shapes colour.
								final MalletColour col = ( colour != null ) ? colour : shape.getColour( j, k ) ;
								setColour( vertexInc++, col, byteVersion ) ;
								break ;
							}
							case UV     :
							{
								shape.getVector2( j, k, uv ) ;
								verticies.set( vertexInc++, uv.x ) ;
								verticies.set( vertexInc++, uv.y ) ;
								break ;
							}
						}
					}

					if( ( vertexInc + vertexSize ) >= verticiesLength )
					{
						MGL.bufferSubData( GL3.ARRAY_BUFFER, vertexStartBytes, verticies ) ;

						vertexStartBytes += size * VBO_VAR_BYTE_SIZE ;
						vertexInc = 0 ;
					}
				}

				positionMatrix.translate( glyph.getWidth(), 0.0f, 0.0f ) ;
			}

			//indicies.set( indexInc++, ( short )PRIMITIVE_RESTART_INDEX ) ;

			if( indexInc > 0 )
			{
				final Int16Array tmp = Int16Array.create( indicies.getBuffer(), 0, indexInc ) ;
				MGL.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, indexStartBytes, tmp ) ;
			}

			if( vertexInc > 0 )
			{
				final Float32Array tmp = Float32Array.create( verticies.getBuffer(), 0, vertexInc ) ;
				MGL.bufferSubData( GL3.ARRAY_BUFFER, vertexStartBytes, tmp ) ;
			}
		}

		private Location findLocationText( final GLDrawData _data )
		{
			// If _data has already been added we return the location 
			// in which it resides.
			{
				final Location location = _data.getLocation() ;
				if( location != null )
				{
					// Ensure the location previously assigned is still good
					// for our purposes.
					if( isCorrectSize( _data, location ) == true )
					{
						return location ;
					}

					super.remove( _data ) ;
				}
			}

			// If it hasn't been added find a space for it within 
			// an existing geometry buffer.
			final int size = buffers.size() ;
			for( int i = 0; i < size; i++ )
			{
				final GLGeometry geometry = buffers.get( i ) ;
				final Location location = geometry.findLocation( _data ) ;
				if( location != null )
				{
					location.setData( _data ) ;
					_data.setLocation( location ) ;
					return location ;
				}
			}

			final int length = _data.getTextEnd() - _data.getTextStart() ;

			// If no space exists create a new geometry buffer 
			// and repeat the finding process.
			// Increase the buffer size if the geometry is too large.
			final Shape shape = glFont.getShapeWithChar( '\0' ) ;
			final int shapeIndexBytes = ( ( shape.getIndicesSize + PRIMITIVE_EXPANSION ) * length ) * IBO_VAR_BYTE_SIZE ;
			final int indexBytes = ( indexLengthBytes > shapeIndexBytes ) ? indexLengthBytes : shapeIndexBytes ;

			final int shapeVertexBytes = shape.getVerticesSize() * vertexStrideBytes * length ;
			final int vertexBytes =  ( vertexLengthBytes > shapeVertexBytes ) ? vertexLengthBytes : shapeVertexBytes ;

			expand( indexBytes, vertexBytes ) ;
			return findLocationText( _data ) ;
		}

		@Override
		protected void expand( final int _indexLengthBytes, final int _vertexLengthBytes )
		{
			buffers.add( new GLGeometry( style, _indexLengthBytes, _vertexLengthBytes, vertexStrideBytes )
			{
				@Override
				protected Location findLocation( final GLDrawData _data )
				{
					final int length = _data.getTextEnd() - _data.getTextStart() ;
					return findLocation( ( 6 * length ), ( 4 * length ) ) ;
				}
			} ) ;
		}

		@Override
		public void destroy()
		{
			super.destroy() ;
			glFont = null ;
		}
	}

	public abstract class GLGeometry
	{
		private final int style ;
		private final int indexLengthBytes ;
		private final int vertexLengthBytes ;
		private final int vertexStrideBytes ;		// Specifies the byte offset between verticies

		public final WebGLBuffer[] indexID ;
		public final WebGLBuffer[] vboID ;

		public int amountIndexUsedBytes  = 0 ;		// How much of buffer has been used
		public int amountVertexUsedBytes = 0 ;		// How much of buffer has been used

		private final List<Location> allocated = MalletList.<Location>newList() ;
		private final List<Location.Range> indexRanges = MalletList.<Location.Range>newList() ;
		private final List<Location.Range> vertexRanges = MalletList.<Location.Range>newList() ;

		public GLGeometry( final int _style,
						   final int _indexLengthBytes,
						   final int _vertexLengthBytes,
						   final int _vertexStrideBytes )
		{
			style = _style ;
			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = _vertexStrideBytes ;

			System.out.println( "Creating buffer: " + indexLengthBytes + " " + vertexLengthBytes ) ;
			
			indexID = GLModelManager.genIndexID() ;	//GLRenderer.handleError( "Gen Index: ", gl ) ;
			vboID = GLModelManager.genVBOID() ;		//GLRenderer.handleError( "Gen VBO: ", gl ) ;

			MGL.bindBuffer( GL3.ELEMENT_ARRAY_BUFFER, indexID[0] ) ;	//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			MGL.bufferData( GL3.ELEMENT_ARRAY_BUFFER, indexLengthBytes, GL3.DYNAMIC_DRAW ) ;	//GLRenderer.handleError( "Upload Data: ", gl ) ;

			MGL.bindBuffer( GL3.ARRAY_BUFFER, vboID[0] ) ;		//GLRenderer.handleError( "Bind Buffer: ", gl ) ;
			MGL.bufferData( GL3.ARRAY_BUFFER, vertexLengthBytes, GL3.DYNAMIC_DRAW ) ;		//GLRenderer.handleError( "Upload Data: ", gl ) ;
		}

		/**
			Using the data stored in _data calculate index and 
			vertex size that needs to be found.
			Call findLocation( int _indexSize, int _vertexSize )
			once you've calculated your space requirements.
		*/
		protected abstract Location findLocation( final GLDrawData _data ) ;

		protected Location findLocation( final int _indexSize, final int _vertexSize )
		{
			final int availableIndex = indexLengthBytes - amountIndexUsedBytes ;
			final int shapeIndexBytes = ( _indexSize + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
			if( shapeIndexBytes > availableIndex )
			{
				return null ;
			}

			final int availableVertex = vertexLengthBytes - amountVertexUsedBytes ;
			final int shapeVertexBytes = _vertexSize * vertexStrideBytes ;
			if( shapeVertexBytes > availableVertex )
			{
				return null ;
			}

			if( allocated.isEmpty() )
			{
				return createLocation( 0, shapeIndexBytes, 0, shapeVertexBytes ) ;
			}

			boolean foundIndex = false ;
			boolean foundVertex = false ;
			int locationIndexStart = 0 ;
			int locationVertexStart = 0 ;

			if( indexRanges.isEmpty() == false )
			{
				final int size = indexRanges.size() ;
				for( int i = 0; i < size; i++ )
				{
					final Location.Range nextIndex = indexRanges.get( i ) ;
					final int indexLen = nextIndex.getStart() - locationIndexStart ;
					if( indexLen < shapeIndexBytes )
					{
						locationIndexStart = nextIndex.getEnd() ;
					}
					else
					{
						foundIndex = true ;
						break ;
					}
				}
			}

			if( vertexRanges.isEmpty() == false )
			{
				final int size = vertexRanges.size() ;
				for( int i = 0; i < size; i++ )
				{
					final Location.Range nextVertex = vertexRanges.get( i ) ;
					final int vertexLen = nextVertex.getStart() - locationVertexStart ;
					if( vertexLen < shapeVertexBytes )
					{
						locationVertexStart = nextVertex.getEnd() ;
					}
					else
					{
						foundVertex = true ;
						break ;
					}
				}
			}

			if( foundIndex == true && foundVertex == true )
			{
				return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
			}

			final int indexLen = indexLengthBytes - locationIndexStart ;
			final int vertexLen = vertexLengthBytes - locationVertexStart ;
			if( indexLen > shapeIndexBytes && vertexLen > shapeVertexBytes )
			{
				return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
			}

			return null ;		// If no space is available
		}

		private Location createLocation( final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			//System.out.println( "Index Start: " + ( _indexStart / IBO_VAR_BYTE_SIZE ) + " Length: " + ( _indexLength / IBO_VAR_BYTE_SIZE )+ " Vertex Start: " + ( _vertexStart / VBO_VAR_BYTE_SIZE ) + " Length: " + ( _vertexLength / VBO_VAR_BYTE_SIZE ) ) ;
			amountIndexUsedBytes += _indexLength ;
			amountVertexUsedBytes += _vertexLength ;

			final Location location = locationCache.get() ;
			location.set( this, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
			allocated.add( location ) ;

			updateRanges() ;
			return location ; 
		}

		public void remove( final Location _location )
		{
			if( allocated.remove( _location ) == true )
			{
				amountIndexUsedBytes -= _location.getIndexLength() ;
				amountVertexUsedBytes -= _location.getVertexLength() ;
				locationCache.reclaim( _location ) ;

				updateRanges() ;
				packGeometryData() ;
			}
		}

		public void forceUpdate()
		{
			final int size = allocated.size() ;
			for( int i = 0; i < size; i++ )
			{
				DrawAssist.forceUpdate( allocated.get( i ).getData() ) ;
			}
		}

		public WebGLBuffer getIndexID()
		{
			return indexID[0] ;
		}

		public WebGLBuffer getVBOID()
		{
			return vboID[0] ;
		}

		public int getIndexLength()
		{
			return amountIndexUsedBytes / IBO_VAR_BYTE_SIZE ;
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

		public void destroy()
		{
			GLModelManager.unbind( this ) ;
		}

		/**
			Update the index and vertex range arrays.
			Ranges must be in order so findLocation 
			can find a correct free space.
		*/
		private void updateRanges()
		{
			indexRanges.clear() ;
			orderedIndexes( allocated, indexRanges ) ;

			vertexRanges.clear() ;
			orderedVertexes( allocated, vertexRanges ) ;
		}

		private List<Location.Range> orderedIndexes( final List<Location> _locations, final List<Location.Range> _ordered )
		{
			_ordered.clear() ;
			if( _locations.isEmpty() == true )
			{
				return _ordered ;
			}

			_ordered.add( _locations.get( 0 ).index ) ;
			final int size = _locations.size() ;

			for( int i = 1; i < size; i++ )
			{
				OrderedInsert.insert( _locations.get( i ).index, _ordered ) ;
			}

			return _ordered ;
		}

		private List<Location.Range> orderedVertexes( final List<Location> _locations, final List<Location.Range> _ordered )
		{
			_ordered.clear() ;
			if( _locations.isEmpty() == true )
			{
				return _ordered ;
			}

			_ordered.add( _locations.get( 0 ).vertex ) ;
			final int size = _locations.size() ;

			for( int i = 1; i < size; i++ )
			{
				OrderedInsert.insert( _locations.get( i ).vertex, _ordered ) ;
			}

			return _ordered ;
		}

		private void packGeometryData()
		{
			int start = 0 ;
			final int size = indexRanges.size() ;
			for( int i = 0; i < size; i++ )
			{
				final Location.Range range = indexRanges.get( i ) ;
				final int indexLength = range.getLength() ;
				if( start != range.getStart() )
				{
					final Location location = range.getParent() ;
					location.set( this, start, indexLength, location.getVertexStart(), location.getVertexLength() ) ;

					// The data being shifted may be in a state of flux, 
					// there is no guarantee that it will still 
					// reside within this buffer.
					// We'll flag the data to be updated during its
					// render cycle.
					DrawAssist.forceUpdate( location.getData() ) ;
				}

				start += indexLength ;
			}
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
		private GLDrawData data ;
		private GLGeometry geometry = null ;
		
		private Range index = new Range() ;
		private Range vertex = new Range() ;

		public Location() {}

		private Location( final GLGeometry _geometry, final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			set( _geometry, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
		}

		public void set( final GLGeometry _geometry,
						 final int _indexStart,
						 final int _indexLength,
						 final int _vertexStart,
						 final int _vertexLength )
		{
			geometry = _geometry ;
			index.set( _indexStart, _indexLength ) ;
			vertex.set( _vertexStart, _vertexLength ) ;
		}

		public void setData( final GLDrawData _data )
		{
			data = _data ;
		}

		public int getIndexStart()
		{
			return index.getStart() ;
		}

		public int getIndexEnd()
		{
			return index.getEnd() ;
		}

		public int getIndexLength()
		{
			return index.getLength() ;
		}

		public int getVertexStart()
		{
			return vertex.getStart() ;
		}

		public int getVertexEnd()
		{
			return vertex.getEnd() ;
		}

		public int getVertexLength()
		{
			return vertex.getLength() ;
		}

		@Override
		public void reset()
		{
			set( null, 0, 0, 0, 0 ) ;
			setData( null ) ;
		}

		public GLDrawData getData()
		{
			return data ;
		}

		public GLGeometry getGeometry()
		{
			return geometry ;
		}

		public String toString()
		{
			return "Index: " + index.toString() + " Vertex: " + vertex.toString() ;
		}

		private class Range implements ISort
		{
			private int start = 0 ;
			private int length = 0 ;

			public Range() {}

			public void set( final int _start, final int _length )
			{
				start = _start ;
				length = _length ;
			}

			public int sortValue()
			{
				return start ;
			}

			public Location getParent()
			{
				return Location.this ;
			}

			public int getStart()
			{
				return start ;
			}

			public int getEnd()
			{
				return start + length ;
			}

			public int getLength()
			{
				return length ;
			}

			public String toString()
			{
				return "Start: " + start + " Length: " + length ;
			}
		}
	}

	protected static void enableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( final VertexAttrib att : _atts )
		{
			MGL.enableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( final VertexAttrib att : _atts )
		{
			MGL.vertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}

	protected static void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( final VertexAttrib att : _atts )
		{
			MGL.disableVertexAttribArray( att.index ) ;
		}
	}

	private static float getABGR( final MalletColour _colour )
	{
		final byte[] colour = new byte[4] ;
		colour[0] = _colour.colours[MalletColour.RED] ;
		colour[1] = _colour.colours[MalletColour.GREEN] ;
		colour[2] = _colour.colours[MalletColour.BLUE] ;
		colour[3] = _colour.colours[MalletColour.ALPHA] ;

		//System.out.println( _colour ) ;
		return ConvertBytes.toFloat( colour, 0, 4 ) ;
	}
	
	private static void setColour( final int _fIndex, final MalletColour _colour, final Uint8Array _byteStream )
	{
		int byteIndex = _fIndex * 4 ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.RED] ) ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.GREEN] ) ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.BLUE] ) ;
		_byteStream.set( byteIndex, ( short )_colour.colours[MalletColour.ALPHA] ) ;
	}
}
