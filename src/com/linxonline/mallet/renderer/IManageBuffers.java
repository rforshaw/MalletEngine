package com.linxonline.mallet.renderer ;

import java.util.List ;

/**
	Used by World, Stencil, Depth, and GroupBuffer.

	Intended to be used by implementations that manage
	buffers such as DrawBuffer, DrawInstancedBuffer, GroupBuffer,
	Stencil, and Depth.
*/
public interface IManageBuffers extends IRequestUpdate
{
	public ABuffer[] addBuffers( final ABuffer ... _buffers ) ;
	public void removeBuffers( final ABuffer ... _buffers ) ;
	public List<ABuffer> getBuffers() ;
}
