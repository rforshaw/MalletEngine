package com.linxonline.mallet.event ;

/*===========================================*/
// AddEventInterface
// Used to ensure old objects can still pass
// Events to different new unknown Event Systems
/*===========================================*/
public interface AddEventInterface
{
	public void addEvent( final Event<?> _event ) ;
}