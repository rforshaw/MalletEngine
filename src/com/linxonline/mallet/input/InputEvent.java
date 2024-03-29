package com.linxonline.mallet.input ;

public final class InputEvent
{
	public InputID id ;
	public InputType inputType ;
	public KeyCode keycode ;

	public int mouseX = 0 ;
	public int mouseY = 0 ;
	public boolean isActionKey = false ;
	public long when = 0L ;

	public InputEvent()
	{
		this( InputType.NONE, KeyCode.NONE, InputID.NONE ) ;
	}

	public InputEvent( final InputType _type, final int _x, final int _y, final long _when )
	{
		setID( InputID.NONE ) ;
		setInput( _type, _x, _y, _when ) ;
	}

	public InputEvent( final InputType _type, final KeyCode _keycode, final InputID _id )
	{
		id = _id ;
		inputType = _type ;
		keycode = _keycode ;
	}

	public InputEvent( final InputEvent _toCopy )
	{
		super() ;
		clone( _toCopy ) ;
	}

	public void setID( final InputID _id )
	{
		id = _id ;
	}

	public void setInput( final InputType _type, final KeyCode _keycode, final long _when )
	{
		inputType = _type ;
		keycode = _keycode ;
		when = _when ;
	}

	public void setInput( final InputType _type, final int _x, final int _y )
	{
		setInput( _type, _x, _y, when ) ;
	}

	public void setInput( final InputType _type, final int _x, final int _y, final long _when )
	{
		inputType = _type ;
		mouseX = _x ;
		mouseY = _y ;
		when = _when ;
	}

	public final InputID getID()
	{
		return id ;
	}

	public final InputType getInputType()
	{
		return inputType ;
	}

	public final KeyCode getKeyCode()
	{
		return keycode ;
	}

	public final char getKeyCharacter()
	{
		return keycode.character ;
	}
	
	public final boolean isActionKey()
	{
		return isActionKey ;
	}
	
	public final int getMouseX()
	{
		return mouseX ;
	}

	public final int getMouseY()
	{
		return mouseY ;
	}

	public long getWhen()
	{
		return when ;
	}

	public final void clone( final InputEvent _input )
	{
		inputType = _input.inputType ;
		mouseX = _input.mouseX ;
		mouseY = _input.mouseY ;
		keycode = _input.keycode ;
		isActionKey = _input.isActionKey ;
		when = _input.when ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( '[' ) ;
		buffer.append( inputType ) ;
		switch( inputType )
		{
			case KEYBOARD_RELEASED :
			case KEYBOARD_PRESSED  :
			{
				buffer.append( " KeyCode: " + keycode ) ;
				break ;
			}
			default :
			{
				buffer.append( " X: " + mouseX ) ;
				buffer.append( " Y: " + mouseY ) ;
				break ;
			}
		}
		buffer.append( ']' ) ;
		return buffer.toString() ;
	}

	public enum Action
	{
		PROPAGATE,			// Continue passing the InputEvent to the next IInputHandler
		CONSUME				// Do not propagate the InputEvent any further
	}
}
