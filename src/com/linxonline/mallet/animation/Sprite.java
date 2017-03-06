package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.maths.Vector2 ;

public final class Sprite extends Resource
{
	public final int framerate ;
	public final List<Sprite.Frame> frames = MalletList.<Sprite.Frame>newList()  ;

	public Sprite()
	{
		this( 30 ) ;
	}

	public Sprite( final int _framerate )
	{
		framerate = _framerate ;
	}

	public void addFrame( final Sprite.Frame _frame )
	{
		if( _frame != null )
		{
			frames.add( _frame ) ;
		}
	}

	public final int size()
	{
		return frames.size() ;
	}

	public final Sprite.Frame getFrame( final int _i )
	{
		return frames.get( _i ) ;
	}

	/**
		Return the audio buffer size in bytes.
	*/
	@Override
	public long getMemoryConsumption()
	{
		return 0L ;
	}

	@Override
	public String type()
	{
		return "SPRITE" ;
	}

	public static class Frame
	{
		public final MalletTexture path ;
		public final Vector2 uv1 = new Vector2() ;
		public final Vector2 uv2 = new Vector2() ;

		public Frame( final String _frame, final float _u1, final float _v1, final float _u2, final float _v2 )
		{
			path = new MalletTexture( _frame ) ;
			uv1.setXY( _u1, _v1 ) ;
			uv2.setXY( _u2, _v2 ) ;
		}
	}
}
