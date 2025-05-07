package com.linxonline.mallet.event ;

public final class NullEventBlock implements IEventBlock
{
	private static final NullEventBlock BLOCK = new NullEventBlock() ;

	public NullEventBlock() {}

	public static IEventBlock create()
	{
		return BLOCK ;
	}

	public EventState getEventState()
	{
		return Event.getGlobalState() ;
	}

	@Override
	public <T> Event.IProcess<T> add( final String _type, final Event.IProcess<T> _proc )
	{
		return _proc ;
	}

	@Override
	public <T> Event.IProcess<T> add( final EventType _type, final Event.IProcess<T> _proc )
	{
		return _proc ;
	}

	@Override
	public void update() {}
}
