package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.GroupBuffer ;
import com.linxonline.mallet.renderer.ABuffer ;

import com.linxonline.mallet.maths.Matrix4 ;

public class GLGroupBuffer extends GLBuffer
{
	private final List<GLBuffer> buffers = new ArrayList<GLBuffer>() ;

	public GLGroupBuffer( final GroupBuffer _stencil )
	{
		super( false ) ;
	}

	public boolean update( final GroupBuffer _group, final AssetLookup<?, GLBuffer> _buffers )
	{
		buffers.clear() ;
		for( final ABuffer buffer : _group.getBuffers() )
		{
			switch( buffer.getBufferType() )
			{
				default                    : Logger.println( "Attempting to add incompatible buffer to Group.", Logger.Verbosity.NORMAL ) ; break ;
				case DRAW_INSTANCED_BUFFER :
				case DRAW_BUFFER           :
				case TEXT_BUFFER           :
				case STENCIL               :
				case GROUP                 :
				case DEPTH                 :
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

		return true ;
	}

	@Override
	public void draw( final GLCamera _camera )
	{
		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _camera ) ;
		}
	}

	@Override
	public void shutdown() {}
}
