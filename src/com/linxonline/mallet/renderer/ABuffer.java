package com.linxonline.mallet.renderer ;

public abstract class ABuffer
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;

	public abstract BufferType getBufferType() ;

	public abstract int getOrder() ;

	public int index()
	{
		return index ;
	}
}
