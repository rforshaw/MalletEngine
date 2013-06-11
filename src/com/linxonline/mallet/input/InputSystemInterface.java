package com.linxonline.mallet.input ;

/*==============================================================*/
// InputSystemInterface is used to implement unique InputSystems//
// for platforms that do not support Javas default input 		//
// listeners.													//
/*==============================================================*/

public interface InputSystemInterface
{
	public void addInputHandler( final InputHandler _handler ) ;
	public void removeInputHandler( final InputHandler _handler ) ;

	public void update() ;
	
	public void clearHandlers() ;
	public void clearInputs() ;
}