package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;

public class InterceptController implements IIntercept
{
	private final static IProcessor<Object> PROCESSOR_FALLBACK = ( Object _obj ) ->
	{
		return true ;
	} ;

	private final EventType.Lookup<IProcessor<?>> processors ;

	public InterceptController( final Tuple<String, IProcessor<?>> ... _processors )
	{
		processors = new EventType.Lookup<IProcessor<?>>( _processors.length, PROCESSOR_FALLBACK ) ;
		for( final Tuple<String, IProcessor<?>> processor : _processors )
		{
			final EventType type = EventType.get( processor.getLeft() ) ;
			processors.add( type, processor.getRight() ) ;
		}
	}

	public <T> void addProcessor( final String _type, final IProcessor<T> _processor )
	{
		final EventType type = EventType.get( _type ) ;
		processors.add( type, _processor ) ;
	}

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
