package com.linxonline.mallet.input ;

public final class InputEvent
{
	public InputType inputType ;
	public KeyCode keycode ;

	public int mouseX = 0 ;
	public int mouseY = 0 ;
	public boolean isActionKey = false ;

	public InputEvent()
	{
		inputType = InputType.NONE ;
		keycode = KeyCode.NONE ;
	}

	public InputEvent( final InputType _type, final int _x, final int _y )
	{
		inputType = _type ;
		mouseX = _x ;
		mouseY = _y ;
	}

	public InputEvent( final InputType _type, final KeyCode _keycode )
	{
		inputType = _type ;
		keycode = _keycode ;
	}

	public void setInput( final InputType _type, final int _x, final int _y )
	{
		inputType = _type ;
		mouseX = _x ;
		mouseY = _y ;
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

	public final void clone( final InputEvent _input )
	{
		inputType = _input.inputType ;
		mouseX = _input.mouseX ;
		mouseY = _input.mouseY ;
		keycode = _input.keycode ;
		isActionKey = _input.isActionKey ;
	}
}