package com.linxonline.mallet.audio.web ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.util.SourceCallback ;

public final class WebAudioSource implements ISource
{
	private SourceCallback callback = null ;

	public WebAudioSource( final WebSound _sound ) {}

	@Override
	public boolean play()
	{
		return false ;
	}

	@Override
	public boolean pause()
	{
		return false ;
	}

	@Override
	public boolean playLoop()
	{
		return false ;
	}

	@Override
	public boolean stop()
	{
		return false ;
	}

	@Override
	public boolean setPosition( final float _x, final float _y, final float _z )
	{
		return false ;
	}

	@Override
	public boolean setRelative( final boolean _relative )
	{
		return false ;
	}

	@Override
	public State getState()
	{
		return ISource.State.UNKNOWN ;
	}

	@Override
	public float getCurrentTime()
	{
		return 0.0f ;
	}

	@Override
	public float getDuration()
	{
		return 0.0f ;
	}

	@Override
	public void setVolume( final int _volume ) {}

	@Override
	public void setCallback( final SourceCallback _callback )
	{
		callback = _callback ;
	}

	@Override
	public SourceCallback getCallback()
	{
		return callback ;
	}

	@Override
	public void destroy() {}
}
