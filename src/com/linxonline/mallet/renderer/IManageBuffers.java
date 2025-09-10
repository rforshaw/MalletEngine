package com.linxonline.mallet.renderer ;

import java.util.List ;

/**
	Used by World, and GroupBuffer.

	World and Group objects can have certain buffers added to them
	if they are considered compatible via the IManageCompatible interface.

	You'll see this used mostly by the updater pools that will create
	their own drawbuffers and will add them to the passed in anchor.
*/
public sealed interface IManageBuffers permits
	World,
	GroupBuffer
{
	public <T extends IManageCompatible> T addBuffer( final T _buffers ) ;
	public <T extends IManageCompatible> void removeBuffer( final T _buffers ) ;

	public List<ICompatibleBuffer> getBuffers() ;

	public void requestUpdate() ;
}
