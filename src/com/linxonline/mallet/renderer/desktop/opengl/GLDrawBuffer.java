package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.IOcclude ;

import com.linxonline.mallet.maths.Matrix4 ;

public final class GLDrawBuffer extends GLBuffer
{
	private VertexAttrib[] attributes = null ;

	private final ArrayList<GLGeometryBuffer> buffers = new ArrayList<GLGeometryBuffer>() ;

	private GLProgram glProgram ;
	private final List<IUniform> uniforms = new ArrayList<IUniform>() ;
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

		if( GLBuffer.generateProgramUniforms( glProgram, program, uniforms ) == false )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
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
			attributes = constructVertexAttrib( _buffer.getAttribute(), glProgram ) ;
		}

		buffers.clear() ;
		for( final GeometryBuffer buffer : _buffer.getBuffers() )
		{
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

		final Matrix4 projection = ( isUI() ) ? _camera.getUIProjection() : _camera.getWorldProjection() ;
		final float[] matrix = projection.matrix ;

		MGL.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;
		if( loadProgramUniforms( glProgram, uniforms ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		GLDrawBuffer.bindBuffers( storages ) ;

		final Camera camera = _camera.getCamera() ;
		for( GLGeometryBuffer buffer : buffers )
		{
			buffer.draw( attributes, glProgram, camera, occluder ) ;
		}
	}

	@Override
	public void shutdown() {}
}
