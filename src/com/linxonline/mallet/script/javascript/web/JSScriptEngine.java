package com.linxonline.mallet.script.javascript ;

import com.linxonline.mallet.core.GameState ;

import com.linxonline.mallet.script.Script ;
import com.linxonline.mallet.script.IScriptEngine ;

public class JSScriptEngine implements IScriptEngine
{
	private final static String FALLBACK_SCRIPT = "() => { return { start: () => { }, update: ( _dt ) => { }, end: () => { } } } ;" ;

	public JSScriptEngine( final GameState _game )
	{
		
	}

	@Override
	public boolean init()
	{
		return true ;
	}

	@Override
	public void add( final Script _script )
	{
		
	}

	@Override
	public void remove( final Script _script )
	{
		
	}

	@Override
	public void update( final float _dt )
	{
		
	}

	@Override
	public void close()
	{
		
	}
}
