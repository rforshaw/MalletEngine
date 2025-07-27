package com.linxonline.mallet.renderer.desktop.opengl ;

import com.linxonline.mallet.renderer.Stencil ;

public class GLStencil extends GLBuffer
{
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

	public boolean update( final Stencil _stencil )
	{
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
	}

	@Override
	public void shutdown() {}
}
