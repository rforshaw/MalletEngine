package com.linxonline.mallet.renderer ;

public interface Draw<T extends Draw>
{
	public void setDrawInterface( DrawInterface<T> _draw ) ;

	public interface DrawInterface<T extends Draw>
	{
		public void draw( final T _data ) ;
	}
}