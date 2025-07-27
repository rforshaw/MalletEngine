package com.linxonline.mallet.renderer.desktop.opengl ;

import com.linxonline.mallet.renderer.Depth ;

public class GLDepth extends GLBuffer
{
	// Depth function
	private int func = MGL.GL_ALWAYS ;
	private boolean mask = true ;

	private boolean enable ;
	private boolean clear ;

	private final boolean[] colourMask = new boolean[] { false, false, false, false } ;

	public GLDepth( final Depth _depth )
	{
		super( false ) ;
	}

	public boolean update( final Depth _depth )
	{
		func = getOperation( _depth.getOperation() ) ;
		mask = _depth.getMask() ;

		enable = _depth.isEnabled() ;
		clear = _depth.shouldClear() ;

		System.arraycopy( _depth.getColourMask(), 0, colourMask, 0, colourMask.length ) ;

		return true ;
	}

	@Override
	public void draw( final GLCamera _camera )
	{
		if( enable )
		{
			MGL.glEnable( MGL.GL_DEPTH_TEST ) ;
		}
		else
		{
			MGL.glDisable( MGL.GL_DEPTH_TEST ) ;
		}

		MGL.glColorMask( colourMask[0], colourMask[1], colourMask[2], colourMask[3] ) ;

		final int clearBits = ( clear == true ) ? MGL.GL_DEPTH_BUFFER_BIT : 0 ;
		MGL.glClear( clearBits ) ;

		MGL.glDepthFunc( func ) ;
		MGL.glDepthMask( mask ) ;
	}

	@Override
	public void shutdown() {}
}
