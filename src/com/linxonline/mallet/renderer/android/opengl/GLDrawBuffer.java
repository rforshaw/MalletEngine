package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;

import com.linxonline.mallet.maths.Matrix4 ;

public class GLDrawBuffer extends GLBuffer
{
	private int order ;
	private VertexAttrib[] attributes = null ;

	private final ArrayList<GLGeometryBuffer> buffers = new ArrayList<GLGeometryBuffer>() ;
	private GLProgram glProgram ;
	private Program mapProgram = new Program() ;

	private AssetLookup<Storage, GLStorage> storages ;
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

		if( glProgram.remap( program, mapProgram ) == false )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
		}

		if( attributes == null )
		{
			// We only want to build the attributes once, 
			// we know a DrawBuffers program can't be fully 
			// replaced, they'd have to create a new GeometryBuffer 
			// to do that.
			attributes = constructVertexAttrib( _buffer.getSwivel(), glProgram ) ;
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

		storages = _storages ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	@Override
	public void draw( final Matrix4 _projection )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		MGL.glUseProgram( glProgram.id[0] ) ;

		final float[] matrix = _projection.matrix ;

		MGL.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;
		if( glProgram.loadUniforms( mapProgram ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		glProgram.bindBuffers( mapProgram, storages ) ;

		for( GLGeometryBuffer buffer : buffers )
		{
			buffer.draw( attributes, glProgram ) ;
		}
	}

	@Override
	public void shutdown() {}
}
