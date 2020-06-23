package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.io.Resource ;
import com.linxonline.mallet.maths.Vector2 ;

public final class Sprite extends Resource
{
	public final int framerate ;
	public final List<Sprite.Frame> frames  ;

	public Sprite()
	{
		this( 30, 5 ) ;
	}

	public Sprite( final int _framerate, final int _frames )
	{
		framerate = _framerate ;
		frames = MalletList.<Sprite.Frame>newList( _frames ) ;
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
		public static final int UV_1_U = 0 ;
		public static final int UV_1_V = 1 ;

		public static final int UV_2_U = 2 ;
		public static final int UV_2_V = 3 ;

		public final MalletTexture path ;
		public final float[] uv = new float[4] ;

		public Frame( final String _frame, final float _u1, final float _v1, final float _u2, final float _v2 )
		{
			path = new MalletTexture( _frame ) ;
			uv[UV_1_U] = _u1 ;
			uv[UV_1_V] = _v1 ;

			uv[UV_2_U] = _u2 ;
			uv[UV_2_V] = _v2 ;
		}
	}
}
