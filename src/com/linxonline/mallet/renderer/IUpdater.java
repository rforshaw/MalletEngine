package com.linxonline.mallet.renderer ;

import java.util.List ;

public interface IUpdater<D extends IUpdate, B extends ABuffer>
{
	public void makeDirty() ;
	public boolean isDirty() ;

	public void addBuffers( final B ... _buffers ) ;
	public void removeBuffers( final B ... _buffers ) ;

	public void addDraws( final D ... _draws ) ;
	public void removeDraws( final D ... _draws ) ;

	public List<D> getDraws() ;
	public List<B> getBuffers() ;

	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration ) ;
}
