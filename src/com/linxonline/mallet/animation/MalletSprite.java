package com.linxonline.mallet.animation ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.maths.Vector2 ;

public class MalletSprite
{
	private final Meta meta ;

	public MalletSprite( final String _path )
	{
		meta = SpriteAssist.create( _path ) ;
	}

	public Meta getMeta()
	{
		return meta ;
	}

	public Frame getFrame( final float _time )
	{
		return meta.getFrame( _time ) ;
	}

	public static class Meta
	{
		private final String path ;
		private final int framerate ;
		private final float frameDelta ;
		private final Frame[] frames ;

		public Meta( final String _path,
					 final int _framerate,
					 final Frame[] _frames )
		{
			path = _path ;
			framerate = _framerate ;
			frameDelta = 1.0f / framerate ;
			frames = _frames ;
		}

		public String getPath()
		{
			return path ;
		}

		public int getFramerate()
		{
			return framerate ;
		}

		public int getNumOfFrames()
		{
			return ( frames != null ) ? frames.length : 0 ;
		}

		public Frame getFrame( final float _time )
		{
			final int index = ( int )( _time / frameDelta ) % getNumOfFrames() ;
			return frames[index] ;
		}
	}

	public static class Frame
	{
		private final String path ; 				// Texture path
		private final MalletTexture texture ;
		private final float[] uv = new float[4] ;	// uv coordinates

		public Frame( final String _path,
					  final float _u1,
					  final float _v1,
					  final float _u2,
					  final float _v2 )
		{
			path = _path ;
			texture = new MalletTexture( path ) ;
			uv[0] = _u1 ;
			uv[1] = _v1 ;
			uv[2] = _u2 ;
			uv[3] = _v2 ;
		}

		public MalletTexture getTexture()
		{
			return texture ;
		}

		public String getPath()
		{
			return path ;
		}

		public Vector2 getMinUV( final Vector2 _populate )
		{
			_populate.setXY( uv[0], uv[1] ) ;
			return _populate ;
		}

		public Vector2 getMaxUV( final Vector2 _populate )
		{
			_populate.setXY( uv[2], uv[3] ) ;
			return _populate ;
		}
	}
}
