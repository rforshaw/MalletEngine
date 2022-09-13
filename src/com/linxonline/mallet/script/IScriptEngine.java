package com.linxonline.mallet.script ;

import java.io.Closeable ;

public interface IScriptEngine extends Closeable
{
	public boolean init() ;

	public void add( final Script _script ) ;
	public void remove( final Script _script ) ;
	public void update( final float _dt ) ;
}
