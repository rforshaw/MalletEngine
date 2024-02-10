package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Stencil ;
import com.linxonline.mallet.renderer.ABuffer ;

import com.linxonline.mallet.maths.Matrix4 ;

public class GLStencil extends GLBuffer
{
	private final List<GLBuffer> buffers = new ArrayList<GLBuffer>() ;

	// Stencil function
	private int func = MGL.GL_ALWAYS ;
	private int ref = 0 ;
	private int mask = 1 ;

	// Stencil operation
	private int sFail ;
	private int zFail ;
	private int zPass ;

	private boolean enable ;
	private boolean clear ;

	private final boolean[] colourMask = new boolean[] { false, false, false, false } ;

	public GLStencil( final Stencil _stencil )
	{
		super( false ) ;
	}

	public boolean update( final Stencil _stencil, final AssetLookup<?, GLBuffer> _buffers )
	{
		buffers.clear() ;
		for( final ABuffer buffer : _stencil.getBuffers() )
		{
			switch( buffer.getBufferType() )
			{
				default                    : Logger.println( "Attempting to add incompatible buffer to Stencil.", Logger.Verbosity.NORMAL ) ; break ;
				case DRAW_INSTANCED_BUFFER :
				case DRAW_BUFFER           :
				case TEXT_BUFFER           :
				{
					final int index = buffer.index() ;
					final GLBuffer buff = _buffers.getRHS( index ) ;
					if( buff != null )
					{
						buffers.add( buff ) ;
					}
				}
			}
		}

		func = getOperation( _stencil.getOperation() ) ;
		ref = _stencil.getReference() ;
		mask = _stencil.getMask() ;

		sFail = getAction( _stencil.getStencilFail() ) ;
		zFail = getAction( _stencil.getDepthFail() ) ;
		zPass = getAction( _stencil.getDepthPass() ) ;

		enable = _stencil.isEnabled() ;
		clear = _stencil.shouldClear() ;

		System.arraycopy( _stencil.getColourMask(), 0, colourMask, 0, colourMask.length ) ;

		return true ;
	}

	@Override
	public void draw( final GLCamera _camera )
	{
		if( enable )
		{
			MGL.glEnable( MGL.GL_STENCIL_TEST ) ;
		}
		else
		{
			MGL.glDisable( MGL.GL_STENCIL_TEST ) ;
		}

		MGL.glColorMask( colourMask[0], colourMask[1], colourMask[2], colourMask[3] ) ;

		final int clearBits = ( clear == true ) ? MGL.GL_STENCIL_BUFFER_BIT : 0 ;
		MGL.glClear( clearBits ) ;

		MGL.glStencilOp( sFail, zFail, zPass ) ;
		MGL.glStencilFunc( func, ref, mask ) ;
		MGL.glStencilMask( mask ) ;

		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _camera ) ;
		}
	}

	@Override
	public void shutdown() {}
}
