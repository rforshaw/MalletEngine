package com.linxonline.mallet.animation ;

import com.linxonline.mallet.animation.AnimationSystem.IAnimation ;

public final class AnimationAssist
{
	private static Assist assist = null ;

	private AnimationAssist() {}

	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	public static IAnimation add( final IAnimation _animation )
	{
		return assist.add( _animation ) ;
	}

	public static IAnimation remove( final IAnimation _animation )
	{
		return assist.remove( _animation ) ;
	}

	public interface Assist
	{
		public IAnimation add( final IAnimation _animation ) ;
		public IAnimation remove( final IAnimation _animation ) ;
	}
}
