package com.linxonline.mallet.entity.components ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.DrawUpdater ;
import com.linxonline.mallet.renderer.DrawInstancedUpdater ;
import com.linxonline.mallet.renderer.ABuffer ;
import com.linxonline.mallet.renderer.IUpdater ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;

public class RenderComponent extends Component
{
	public RenderComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public RenderComponent( final Entity _parent, final Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
		init() ;
	}

	/**
		Override when you wish to construct a set of 
		draw objects and add them directly to the 
		component without revealing them to others.
	*/
	public void init() {}

	public void shutdown() {}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		shutdown() ;
		super.readyToDestroy( _callback ) ;
	}
}
