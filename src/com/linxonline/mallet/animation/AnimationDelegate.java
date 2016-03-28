package com.linxonline.mallet.animation ;

public interface AnimationDelegate
{
	public void addAnimation( final Anim _animation ) ;
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