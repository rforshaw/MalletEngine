package com.linxonline.mallet.renderer ;

import java.util.List ;

/**
	Used by World, and GroupBuffer.

	Intended to be used by implementations that manage
	buffers such as DrawBuffer, DrawInstancedBuffer, GroupBuffer.
*/
public interface IManageBuffers extends IRequestUpdate
{
	public <T extends ABuffer> T addBuffer( final T _buffers ) ;
	public <T extends ABuffer> void removeBuffer( final T _buffers ) ;

	public List<ABuffer> getBuffers() ;
}
