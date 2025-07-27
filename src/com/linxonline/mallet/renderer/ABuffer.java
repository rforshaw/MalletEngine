package com.linxonline.mallet.renderer ;

/**
	ABuffer is a central class that allows us to retain
	and manage different buffer types as if they are
	the same.
	It's bad to be honest. Certain classes for example
	GroupBuffer will allow you to add any buffer that
	extends ABuffer, but it only allows a subset, you are
	only informed of this at runtime, while it should really
	be a compile time operation.
*/
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
	private IMeta meta = IMeta.EMPTY_META ;

	public abstract int getOrder() ;

	public IMeta setMeta( final IMeta _meta )
	{
		meta = ( _meta != null ) ? _meta : IMeta.EMPTY_META ;
		return meta ;
	}

	public IMeta getMeta()
	{
		return meta ;
	}

	public int index()
	{
		return index ;
	}
}
