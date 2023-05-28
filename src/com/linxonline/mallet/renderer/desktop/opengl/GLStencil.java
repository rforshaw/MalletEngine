package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Stencil ;
import com.linxonline.mallet.renderer.ABuffer ;
import com.linxonline.mallet.renderer.Operation ;
import com.linxonline.mallet.renderer.Action ;

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

		return true ;
	}

	private static int getOperation( final Operation _operation )
	{
		switch( _operation )
		{
			default                 : return MGL.GL_ALWAYS ;
			case ALWAYS             : return MGL.GL_ALWAYS ;
			case NEVER              : return MGL.GL_NEVER ;
			case LESS_THAN          : return MGL.GL_LESS ;
			case GREATER_THAN       : return MGL.GL_GREATER ;
			case LESS_THAN_EQUAL    : return MGL.GL_LEQUAL ;
			case GREATER_THAN_EQUAL : return MGL.GL_GEQUAL ;
			case EQUAL              : return MGL.GL_EQUAL ;
			case NOT_EQUAL          : return MGL.GL_NOTEQUAL ;
		}
	}

	private static int getAction( final Action _action )
	{
		switch( _action )
		{
			default        : return MGL.GL_KEEP ;
			case KEEP      : return MGL.GL_KEEP ;
			case ZERO      : return MGL.GL_ZERO ;
			case REPLACE   : return MGL.GL_REPLACE ;
			case INCREMENT : return MGL.GL_INCR ;
			case DECREMENT : return MGL.GL_DECR ;
			case INVERT    : return MGL.GL_INVERT ;
		}
	}

	@Override
	public void draw( final Matrix4 _projection )
	{
		if( enable )
		{
			MGL.glEnable( MGL.GL_STENCIL_TEST ) ;
		}
		else
		{
			MGL.glDisable( MGL.GL_STENCIL_TEST ) ;
		}

		final int clearBits = ( clear == true ) ? MGL.GL_STENCIL_BUFFER_BIT : 0 ;
		MGL.glClear( clearBits ) ;

		MGL.glStencilOp( sFail, zFail, zPass ) ;
		MGL.glStencilFunc( func, ref, mask ) ;
		MGL.glStencilMask( mask ) ;

		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _projection ) ;
		}
	}

	@Override
	public void shutdown() {}
}
