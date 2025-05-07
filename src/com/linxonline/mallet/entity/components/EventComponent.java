package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.event.* ;

public class EventComponent extends Component
{
	protected final EventBlock block ;

	public EventComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public EventComponent( final Entity _parent, final Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
		block = createGlobalEventBlock() ;
	}

	public EventBlock createGlobalEventBlock( final Tuple<String, Event.IProcess<?>> ... _processors )
	{
		if( _processors == null )
		{
			return new EventBlock() ;
		}

		if( _processors.length == 0 )
		{
			return new EventBlock() ;
		}

		return new EventBlock( _processors ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		block.update() ;
	}

	public EventBlock getEventBlock()
	{
		return block ;
	}
}
