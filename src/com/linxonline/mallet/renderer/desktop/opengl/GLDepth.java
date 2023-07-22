package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Depth ;
import com.linxonline.mallet.renderer.ABuffer ;


import com.linxonline.mallet.maths.Matrix4 ;

public class GLDepth extends GLBuffer
{
	private final List<GLBuffer> buffers = new ArrayList<GLBuffer>() ;

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

	public boolean update( final Depth _depth, final AssetLookup<?, GLBuffer> _buffers )
	{
		buffers.clear() ;
		for( final ABuffer buffer : _depth.getBuffers() )
		{
			switch( buffer.getBufferType() )
			{
				default                    : Logger.println( "Attempting to add incompatible buffer to depth.", Logger.Verbosity.NORMAL ) ; break ;
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

		func = getOperation( _depth.getOperation() ) ;
		mask = _depth.getMask() ;

		enable = _depth.isEnabled() ;
		clear = _depth.shouldClear() ;

		System.arraycopy( _depth.getColourMask(), 0, colourMask, 0, colourMask.length ) ;

		return true ;
	}

	@Override
	public void draw( final Matrix4 _projection )
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

		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _projection ) ;
		}
	}

	@Override
	public void shutdown() {}
}
