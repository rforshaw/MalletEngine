package com.linxonline.mallet.animation ;

public class Animation<T extends Animation.Frame>
{
	private final String path ;
	private final int framerate ;
	private final float frameDelta ;

	private final int[] frameOrder ;
	private final T[] frames ;

	public Animation( final String _path, final int _framerate, final int[] _frameOrder, final T[] _frames )
	{
		path = _path ;
		framerate = _framerate ;
		frameDelta = 1.0f / framerate ;

		frameOrder = _frameOrder ;
		frames = _frames ;
	}

	public String getPath()
	{
		return path ;
	}

	public T getFrame( final float _time )
	{
		final int index = ( int )( _time / frameDelta ) % frameOrder.length ;
		return frames[frameOrder[index]] ;
	}

	public interface Frame {}
}
