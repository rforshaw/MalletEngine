package com.linxonline.mallet.script ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.entity.Entity ;

public final class Script
{
	private final static Object FALLBACK = new Object() ; 

	private final String name ;
	private final String scriptPath ;

	private final List<Entity> entities = MalletList.<Entity>newList() ;
	private final List<Function> functions = MalletList.<Function>newList() ;

	public Script( final String _path )
	{
		this( _path, _path ) ;
	}

	public Script( final String _name, final String _path )
	{
		name = _name ;
		scriptPath = _path ;
	}

	public boolean addAll( final List<Entity> _entities )
	{
		return entities.addAll( _entities ) ;
	}

	public boolean add( final Entity _entity )
	{
		return entities.add( _entity ) ;
	}

	public boolean remove( final Entity _entity )
	{
		return entities.remove( _entity ) ;
	}

	// Specify a function name within the script.
	// The returned function will become executable as soon
	// as the script is added to the script-engine for processing.
	// Call invoke() to call into the script.
	public Function registerFunction( final String _name )
	{
		final Function function = new Function( _name ) ;
		functions.add( function ) ;
		return function ;
	}

	public String getName()
	{
		return name ;
	}

	public String getPath()
	{
		return scriptPath ;
	}

	public List<Entity> getEntities()
	{
		return entities ;
	}

	public List<Function> getFunctions()
	{
		return functions ;
	}

	public static class Function
	{
		private final String name ;
		private IFunction engineFunc ;

		public Function( final String _name )
		{
			name = _name ;
		}

		// Do not call directly, used by the scripting-engine.
		public void setEngineCall( final IFunction _function )
		{
			engineFunc = _function ;
		}

		public void invoke()
		{
			// This call should never fail, the engine will
			// provide a fallback function to call if the script
			// doesn't offer a function to call.
			engineFunc.invoke() ;
		}

		public String getName()
		{
			return name ;
		}
	}

	public interface IFunction
	{
		public void invoke() ;
	}
}
