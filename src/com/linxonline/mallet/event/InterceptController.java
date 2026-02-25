package com.linxonline.mallet.event ;

import com.linxonline.mallet.util.Tuple ;

public final class InterceptController implements IIntercept
{
	private final static IProcessor<?> PROCESSOR_FALLBACK = ( Object _obj ) ->
	{
		return true ;
	} ;

	private final EventType.Lookup<IProcessor<?>> processors ;

	public InterceptController()
	{
		processors = new EventType.Lookup<IProcessor<?>>( 1, PROCESSOR_FALLBACK ) ;
	}

	@SafeVarargs
	public InterceptController( final Tuple<String, IProcessor<?>> ... _processors )
	{
		processors = new EventType.Lookup<IProcessor<?>>( _processors.length, PROCESSOR_FALLBACK ) ;
		for( final Tuple<String, IProcessor<?>> processor : _processors )
		{
			processors.add( EventType.get( processor.getLeft() ), processor.getRight() ) ;
		}
	}

	public void addProcessor( final String _type, final IProcessor<?> _processor )
	{
		processors.add( EventType.get( _type ), _processor ) ;
	}

	@Override
	public boolean allow( final Event<?> _event )
	{
		final IProcessor proc = processors.get( _event.getEventType() ) ;
		return proc.process( _event.getVariable() ) ;
	}

	public static Tuple<String, IProcessor<?>> create( final String _name, final IProcessor<?> _processor )
	{
		return Tuple.<String, IProcessor<?>>build( _name, _processor ) ;
	}

	@FunctionalInterface
	public interface IProcessor<T>
	{
		public boolean process( final T _variable ) ;
	}
}
