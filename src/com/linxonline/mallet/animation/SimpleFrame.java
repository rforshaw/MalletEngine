package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

public final class SimpleFrame implements Animation.Frame
{
	private final String path ; 				// Texture path
	private final Texture texture ;

	private final float u1 ;
	private final float v1 ;

	private final float u2 ;
	private final float v2 ;

	public SimpleFrame( final String _path,
						final float _u1, final float _v1,
						final float _u2, final float _v2 )
	{
		path = _path ;
		texture = new Texture( path ) ;
		u1 = _u1 ;
		v1 = _v1 ;
		u2 = _u2 ;
		v2 = _v2 ;
	}

	public Texture getTexture()
	{
		return texture ;
	}

	public String getPath()
	{
		return path ;
	}

	public Vector2 getMinUV( final Vector2 _populate )
	{
		_populate.setXY( u1, v1 ) ;
		return _populate ;
	}

	public Vector2 getMaxUV( final Vector2 _populate )
	{
		_populate.setXY( u2, v2 ) ;
		return _populate ;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder() ;

		builder.append( path ) ;
		builder.append( ' ' ) ;

		builder.append( u1 ) ;
		builder.append( ' ' ) ;

		builder.append( v1 ) ;
		builder.append( ' ' ) ;

		builder.append( u2 ) ;
		builder.append( ' ' ) ;

		builder.append( v2 ) ;
		builder.append( ' ' ) ;

		return builder.toString() ;
	}

	public final static class Generator implements AnimatorGenerator.FrameGenerator<SimpleFrame>
	{
		@Override
		public SimpleFrame create( final String _animPath, final List<JObject> _resources )
		{
			final JObject resource = _resources.get( 0 ) ;

			final String path = resource.optString( "texture", "" ) ;
			final float u1 = ( float )resource.optDouble( "u1", 0.0 ) ;
			final float v1 = ( float )resource.optDouble( "v1", 0.0 ) ;

			final float u2 = ( float )resource.optDouble( "u2", 0.0 ) ;
			final float v2 = ( float )resource.optDouble( "v2", 0.0 ) ;

			return new SimpleFrame( path, u1, v1, u2, v2 ) ;
		}
	}

	public final static class Listener implements AnimationBooklet.IListener<SimpleFrame>
	{
		private final World world ;
		private final Program program ;
		private Draw draw ;
		private DrawUpdater updater ;

		private final int order ;
		private final Vector2 min = new Vector2() ;
		private final Vector2 max = new Vector2() ;

		public Listener( final World _world,
						 final Program _program,
						 final Draw _draw,
						 final int _order )
		{
			world = _world ;
			program = _program ;
			draw = _draw ;
			order = _order ;
		}

		@Override
		public void init() {}

		@Override
		public void shutdown()
		{
			if( updater != null )
			{
				final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
				geometry.removeDraw( draw ) ;
				updater.forceUpdate() ;
			}
		}

		@Override
		public void animationChanged( final Animation<SimpleFrame> _previous, final Animation<SimpleFrame> _next ) {}

		@Override
		public void frameChanged( final SimpleFrame _previous, final SimpleFrame _next )
		{
			final String previousPath = ( _previous != null ) ? _previous.getPath(): "" ;
			final String nextPath = _next.getPath() ;
			if( previousPath.equals( nextPath ) == false )
			{
				applyTexture( _next.getTexture() ) ;
			}

			_next.getMinUV( min ) ;
			_next.getMaxUV( max ) ;

			Shape.updatePlaneUV( ( Shape )draw.getShape(), min, max ) ;
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

		private void applyTexture( final Texture _texture )
		{
			if( updater != null )
			{
				final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
				geometry.removeDraw( draw ) ;
				updater.forceUpdate() ;
			}

			// We only want to remap the programs texture 
			// if the sprite is not using a spritesheet.
			program.mapUniform( "inTex0", _texture ) ;

			final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;
			updater = pool.getOrCreate( world, program, draw.getShape(), false, order ) ;

			final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.addDraw( draw ) ;
		}
	}
}
