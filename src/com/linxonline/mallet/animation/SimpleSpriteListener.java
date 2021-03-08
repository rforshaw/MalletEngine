package com.linxonline.mallet.animation ;

import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.DrawUpdater ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletTexture ;

import com.linxonline.mallet.maths.Vector2 ;

public class SimpleSpriteListener implements SpriteAnimations.IListener
{
	private final World world ;
	private final Program program ;
	private Draw draw ;
	private DrawUpdater updater ;

	private final int order ;
	private final Vector2 min = new Vector2() ;
	private final Vector2 max = new Vector2() ;

	/**
		The program passed in is considered to be owned
		by the listener, and will be removed when the listener 
		is removed from the animation-system.
	*/
	public SimpleSpriteListener( final World _world,
								 final Program _program,
								 final Draw _draw,
								 final int _order )
	{
		world = _world ;
		program = ProgramAssist.add( _program ) ;
		draw = _draw ;
		order = _order ;
	}

	@Override
	public void init() {}

	@Override
	public void shutdown()
	{
		ProgramAssist.remove( program ) ;
		if( updater != null )
		{
			updater.removeDynamics( draw ) ;
		}
	}

	@Override
	public void spriteChanged( final MalletSprite _previous, final MalletSprite _next ) {}

	@Override
	public void frameChanged( final MalletSprite.Frame _previous, final MalletSprite.Frame _next )
	{
		final String previousPath = ( _previous != null ) ? _previous.getPath(): "" ;
		final String nextPath = _next.getPath() ;
		if( previousPath.equals( nextPath ) == false )
		{
			applyTexture( _next.getTexture() ) ;
		}

		_next.getMinUV( min ) ;
		_next.getMaxUV( max ) ;

		Shape.updatePlaneUV( draw.getShape(), min, max ) ;
		updater.forceUpdate() ;
	}

	public Draw getDraw()
	{
		return draw ;
	}

	public DrawUpdater getDrawUpdater()
	{
		return updater ;
	}

	private void applyTexture( final MalletTexture _texture )
	{
		if( updater != null )
		{
			updater.removeDynamics( draw ) ;
		}

		// We only want to remap the programs texture 
		// if the sprite is not using a spritesheet.
		program.mapUniform( "inTex0", _texture ) ;

		updater = DrawUpdater.getOrCreate( world, program, draw.getShape(), false, order ) ;
		updater.addDynamics( draw ) ;
	}
}
