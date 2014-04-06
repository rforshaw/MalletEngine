package com.linxonline.mallet.entity ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

/*==============================================================*/
// CameraInputComponent - used to move a 2D camera position		//
// using the WASD keys.											//
/*==============================================================*/

public class CameraInputComponent extends Component
										  implements InputHandler
{
	protected static final int speed = 400 ;
	protected boolean forward = false ;
	protected boolean backward = false ;
	protected boolean left = false ;
	protected boolean right = false ;

	public CameraInputComponent()
	{
		super( "CAMERAINPUT", "INPUTCOMPONENT" ) ;
	}
	
	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter ) {}

	@Override
	public void update( final float _dt )
	{
		final Vector3 pos = parent.getPosition() ;
		if( forward == true )
		{
			pos.y += speed * _dt ;
		}
		
		if( backward == true )
		{
			pos.y -= speed * _dt ;
		}
		
		if( left == true )
		{
			pos.x -= speed * _dt ;
		}
		
		if( right == true )
		{
			pos.x += speed * _dt ;
		}
	}
	
	public void passInputEvent( final InputEvent _event )
	{
		switch( _event.getInputType() )
		{
			case KEYBOARD_PRESSED  : pressed( _event ) ;  break ;
			case KEYBOARD_RELEASED : released( _event ) ; break ;
		}
	}

	private final void pressed( final InputEvent _event )
	{
		final char key = _event.getKeyCharacter() ;

		if( key == 'w' )
		{
			forward = true ;
		}

		if( key == 's' )
		{
			backward = true ;
		}

		if( key == 'a' )
		{
			left = true ;
		}

		if( key == 'd' )
		{
			right = true ;
		}
	}

	private final void released( final InputEvent _event )
	{
		final char key = _event.getKeyCharacter() ;

		if( key == 'w' )
		{
			forward = false ;
		}

		if( key == 's' )
		{
			backward = false ;
		}

		if( key == 'a' )
		{
			left = false ;
		}

		if( key == 'd' )
		{
			right = false ;
		}
	}
}