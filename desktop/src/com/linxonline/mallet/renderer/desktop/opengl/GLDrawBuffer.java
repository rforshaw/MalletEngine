package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.IOcclude ;

import com.linxonline.mallet.maths.Matrix4 ;

public final class GLDrawBuffer extends GLBuffer
{
	private VertexAttrib[] attributes = null ;

	private final ArrayList<int[]> ranges = new ArrayList<int[]>() ;
	private final ArrayList<GLGeometryBuffer> buffers = new ArrayList<GLGeometryBuffer>() ;

	private GLProgram glProgram ;
	private int style = -1 ; // GL_TRIANGLES, LINES, etc...

	private final GLProgram.UniformState uniformState = new GLProgram.UniformState() ;
	private final List<GLProgram.ILoadUniform> uniforms = new ArrayList<GLProgram.ILoadUniform>() ;
	private final List<GLStorage> storages = new ArrayList<GLStorage>() ;

	private IOcclude occluder = DrawBuffer.OCCLUDER_FALLBACK ;
	private boolean stable = false ;

	public GLDrawBuffer( final DrawBuffer _buffer )
	{
		super( _buffer.isUI() ) ;
	}

	public boolean update( final DrawBuffer _buffer,
						   final AssetLookup<Program, GLProgram> _programs,
						   final AssetLookup<?, GLBuffer> _buffers,
						   final AssetLookup<Storage, GLStorage> _storages )
	{
		final Program program = _buffer.getProgram() ;

		glProgram = _programs.getRHS( program.index() ) ;
		if( glProgram == null )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
		}

		switch( program.getStyle() )
		{
			case LINES      : style = MGL.GL_LINES ;      break ;
			case LINE_STRIP : style = MGL.GL_LINE_STRIP ; break ;
			case FILL       : style = MGL.GL_TRIANGLES ;  break ;
			default         : style = MGL.GL_LINES ;      break ;
		}

		uniforms.clear() ;
		if( glProgram.buildProgramUniforms( program, uniforms ) == false )
		{
			stable = false ;
			return stable ;
		}

		if( glProgram.buildDrawUniforms( program, uniformState ) == false )
		{
			stable = false ;
			return stable ;
		}

		GLBuffer.generateStorages( glProgram, program, _storages, storages ) ;

		if( attributes == null )
		{
			// We only want to build the attributes once, 
			// we know a DrawBuffers program can't be fully 
			// replaced, they'd have to create a new GeometryBuffer 
			// to do that.
			attributes = constructVertexAttrib( program, glProgram ) ;
		}

		ranges.clear() ;
		final List<int[]> rRanges = _buffer.getRanges() ;
		final int rangeSize = rRanges.size() ;
		for( int i = 0; i < rangeSize; ++i )
		{
			final int[] range = rRanges.get( i ) ;
			final int[] clone = ( range != null ) ? range.clone() : null ; 
			ranges.add( clone ) ;
		}

		buffers.clear() ;
		final List<GeometryBuffer> gBuffers = _buffer.getBuffers() ;
		final int geomSize = gBuffers.size() ;
		for( int i = 0; i < geomSize; ++i )
		{
			final GeometryBuffer buffer = gBuffers.get( i ) ;
			final GLGeometryBuffer buff = ( GLGeometryBuffer )_buffers.getRHS( buffer.index() ) ;
			if( buff != null )
			{
				buffers.add( buff ) ;
			}
		}

		occluder = _buffer.getOccluder() ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	@Override
	public void draw( final GLCamera _camera )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		MGL.glUseProgram( glProgram.id[0] ) ;

		final Matrix4 view = ( isUI() ) ? IDENTITY : _camera.getView() ;
		final Matrix4 projection = ( isUI() ) ? _camera.getUIProjection() : _camera.getProjection() ;

		MGL.glUniformMatrix4fv( glProgram.inViewMatrix, 1, false, view.matrix, 0 ) ;
		MGL.glUniformMatrix4fv( glProgram.inProjectionMatrix, 1, false, projection.matrix, 0 ) ;
		MGL.glUniform2f( glProgram.inResolution, _camera.getWidth(), _camera.getHeight() ) ;

		{
			uniformState.reset() ;
			final int size = uniforms.size() ;
			for( int i = 0; i < size; ++i )
			{
				if( uniforms.get( i ).load( uniformState ) == false )
				{
					System.out.println( "Failed to load uniforms." ) ;
					return ;
				}
			}
		}

		GLDrawBuffer.bindBuffers( storages ) ;

		final Camera camera = _camera.getCamera() ;

		final int size = buffers.size() ;
		for( int i = 0; i < size; ++i )
		{
			final int[] range = ranges.get( i ) ;
			final GLGeometryBuffer buffer = buffers.get( i ) ;

			buffer.draw( range, style, attributes, glProgram, uniformState, camera, occluder ) ;
		}
	}

	@Override
	public void shutdown() {}
}
