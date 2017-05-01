package com.linxonline.mallet.event ;

/*===========================================*/
// IAddEvent
// Used to ensure old objects can still pass
// Events to different new unknown Event Systems
/*===========================================*/
public interface IAddEvent
{
	public void addEvent( final Event<?> _event ) ;
}
