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
}
