package com.linxonline.mallet.renderer ;

import java.util.List ;

public sealed interface IUpdater permits
	DrawUpdater,
	DrawInstancedUpdater,
	TextUpdater,
	StorageUpdater
{
	public void forceUpdate() ;

	public void update( final List<ABuffer> _updated, final float _coefficient ) ;
}
