package com.linxonline.mallet.renderer ;

public sealed abstract class ABuffer implements IRequestUpdate permits
	DrawBuffer,
	GeometryBuffer,
	TextBuffer,
	Stencil,
	Depth,
	GroupBuffer,
	Storage
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;

	public abstract int getOrder() ;

	public int index()
	{
		return index ;
	}
}
