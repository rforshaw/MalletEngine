package com.linxonline.mallet.script ;

public interface IScriptEngine
{
	public void add( final Script _script ) ;
	public void remove( final Script _script ) ;
	public void update( final double _dt ) ;

	public void close() ;
}
