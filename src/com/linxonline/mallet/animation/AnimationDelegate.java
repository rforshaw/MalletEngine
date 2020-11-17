package com.linxonline.mallet.animation ;

import com.linxonline.mallet.renderer.World ;

public interface AnimationDelegate
{
	public void addAnimation( final Anim _animation, final World _world ) ;
	public void removeAnimation( final Anim _animation ) ;

	/**
		Inform the Animation System to accept requests
		from the delegate.
	*/
	public void start() ;

	/**
		Inform the Animation System to stop accepting requests 
		from delegate and remove all previous requests.
	*/
	public void shutdown() ;
}
