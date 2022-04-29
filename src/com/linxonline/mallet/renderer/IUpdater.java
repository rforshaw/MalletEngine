package com.linxonline.mallet.renderer ;

import java.util.List ;

public interface IUpdater<B extends ABuffer>
{
	public void forceUpdate() ;

	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration ) ;
}
