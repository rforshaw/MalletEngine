package com.linxonline.mallet.renderer ;

/**
	Camera handler.
*/
public interface Camera<T extends Camera>
{
	public String getID() ;

	public void setDrawInterface( DrawInterface<T> _draw ) ;

	public interface DrawInterface<T extends Camera>
	{
		public void draw( final T _data ) ;
	}
}