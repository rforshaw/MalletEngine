package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.script.Script ;

public class ScriptComponent extends Component
{
	private final Script script ;

	public ScriptComponent( final String _scriptPath, final Entity _parent )
	{
		this( _scriptPath, _parent, Entity.AllowEvents.YES ) ;
	}

	public ScriptComponent( final String _scriptPath, final Entity _parent, Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
		script = new Script( _scriptPath, _scriptPath ) ;
		script.add( _parent ) ;
	}

	public Script getScript()
	{
		return script ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		_events.add( Event.<Script>create( "ADD_SCRIPT_EVENT", script ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( Event.<Script>create( "REMOVE_SCRIPT_EVENT", script )  ) ;
	}
}
