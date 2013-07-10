package com.linxonline.mallet.input ;

public final class InputEvent
{
	// Keycodes
	public static final int SHIFT = 16 ;
	public static final int CTRL = 17 ;
	public static final int ALT = 18 ;
	public static final int ALTGROUP = 65406 ;
	public static final int BACKSPACE = 8 ;
	public static final int ENTER = 66 ;

	public static final int NONE = -1 ;

	// Input Types
	public static final int MOUSE1_PRESSED = 0 ;
	public static final int MOUSE1_RELEASED = 1 ;
	public static final int MOUSE2_PRESSED = 2 ;
	public static final int MOUSE2_RELEASED = 3 ;
	public static final int MOUSE_MOVED = 4 ;

	public static final int KEYBOARD_PRESSED = 5 ;
	public static final int KEYBOARD_RELEASED = 6 ;

	public static final int TOUCH_MOVE = 7 ;
	public static final int TOUCH_UP = 8 ;
	public static final int TOUCH_DOWN = 9 ;

	public static final int SCROLL_WHEEL = 10 ;
	
	// Class variables
	public int inputType = NONE ;
	public int mouseX = 0 ;
	public int mouseY = 0 ;
	public char key = ' ' ;
	public int keycode = NONE ;
	public boolean isActionKey = false ;

	public InputEvent() {}

	/**
		Adding Mouse Event
	**/
	public InputEvent( final int _type, final int _x, final int _y )
	{
		setInput( _type, _x, _y ) ;
	}

	/**
		Adding Mouse Event
	**/
	public InputEvent( final int _type, final float _x, final float _y )
	{
		setInput( _type, _x, _y ) ;
	}
	
	/**
		Adding Keyboard Event
	**/
	public InputEvent( final int _type, final char _key, final int _keycode )
	{
		setInput( _type, _key, _keycode ) ;
	}

	public void setInput( final int _type, final int _x, final int _y )
	{
		inputType = _type ;
		mouseX = _x ;
		mouseY = _y ;
	}

	public void setInput( final int _type, final float _x, final float _y )
	{
		inputType = _type ;
		mouseX = ( int )_x ;
		mouseY = ( int )_y ;
	}

	public void setInput( final int _type, final char _key, final int _keycode )
	{
		inputType = _type ;
		key = _key ;
		keycode = _keycode ;
	}
	
	public final int getInputType()
	{
		return inputType ;
	}

	public final int getKeyCode()
	{
		return keycode ;
	}
	
	public final char getKeyCharacter()
	{
		return key ;
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
		key = _input.key ;
		keycode = _input.keycode ;
		isActionKey = _input.isActionKey ;
	}
}