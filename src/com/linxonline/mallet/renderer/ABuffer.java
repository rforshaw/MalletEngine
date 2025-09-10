package com.linxonline.mallet.renderer ;

/**
	Abstract Buffer is a central class that allows us to retain
	and manage different buffer types as if they are
	the same.

	It's a class that defines the minimum amount required to map
	developer defined render state with it's internal render state.
*/
public sealed abstract class ABuffer implements IUpdateState, ICompatibleBuffer permits
	DrawBuffer,
	GeometryBuffer,
	TextBuffer,
	Stencil,
	Depth,
	GroupBuffer
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;
	private IMeta meta = IMeta.EMPTY_META ;

	@Override
	public IMeta setMeta( final IMeta _meta )
	{
		meta = ( _meta != null ) ? _meta : IMeta.EMPTY_META ;
		return meta ;
	}

	@Override
	public IMeta getMeta()
	{
		return meta ;
	}

	@Override
	public int index()
	{
		return index ;
	}
}
