package com.linxonline.mallet.renderer ;

/**
	Program represent a handle to the backend 
	implementation of shaders.

	This will most like be a GLSL program or 
	at a later date SPIR-V.
*/
public interface Program
{
	public boolean removeUniform( final String _handler ) ;
	public boolean mapUniform( final String _handler, final Object _obj ) ;

	public boolean removeStorage( final String _handler ) ;
	public boolean mapStorage( final String _handler, final Storage _obj ) ;
}
