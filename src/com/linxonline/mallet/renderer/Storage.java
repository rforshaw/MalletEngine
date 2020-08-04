package com.linxonline.mallet.renderer ;

/**
	Handler for accessing and modifying storage objects.

	Storage objects can be created and attached to program 
	handles. A storage object can be used to store data 
	that can be later used by a shader.
*/
public interface Storage
{
	public void expand( final int _by ) ;
	public float[] getBuffer() ;
}
