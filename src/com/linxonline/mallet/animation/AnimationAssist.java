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

	public static <T extends IAnimation> T add( final T _animation )
	{
		return assist.add( _animation ) ;
	}

	public static <T extends IAnimation> T remove( final T _animation )
	{
		return assist.remove( _animation ) ;
	}

	public interface Assist
	{
		public <T extends IAnimation> T add( final T _animation ) ;
		public <T extends IAnimation> T remove( final T _animation ) ;
	}
}
