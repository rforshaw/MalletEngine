package com.linxonline.mallet.renderer ;

import java.util.List ;

public interface IUpdater<D extends IUpdate, B extends ABuffer>
{
	public void makeDirty() ;
	public boolean isDirty() ;

	public void addBuffers( final B ... _buffers ) ;
	public void removeBuffers( final B ... _buffers ) ;

	public void addDynamics( final D ... _draws ) ;
	public void removeDynamics( final D ... _draws ) ;

	public List<D> getDynamics() ;
	public List<B> getBuffers() ;

	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration ) ;
}
