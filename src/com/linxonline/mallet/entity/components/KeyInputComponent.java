package com.linxonline.mallet.entity.components ;

import java.util.HashMap ;
import java.util.ArrayList ;

import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.input.KeyInputListener ;
import com.linxonline.mallet.input.KeyCode ;

/**
	Provides a convient method to process particular logic based an a key value.
	Decouples the key from the logic.
**/
public class KeyInputComponent extends InputComponent
{
	private final HashMap<KeyCode, Key> keys = new HashMap<KeyCode, Key>() ;

	public KeyInputComponent()
	{
		super() ;
	}

	public KeyInputComponent( final String _name )
	{
		super( _name, "INPUTCOMPONENT" ) ;
	}
	
	public KeyInputComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}
	
	public void registerKey( final KeyCode _key, final KeyInputListener _listener )
	{
		if( keys.containsKey( _key ) == true )
		{
			keys.get( _key ).add( _listener ) ;
		}
		else
		{
			keys.put( _key, new Key( _listener ) ) ;
		}
	}

	public void unregisterKey( final KeyCode _key )
	{
		if( keys.containsKey( _key ) == true )
		{
			keys.remove( _key ) ;
		}
	}

	@Override
	protected void processInputEvent( final InputEvent _input )
	{
		switch( _input.inputType )
		{
			case KEYBOARD_PRESSED :
			{
				if( keys.containsKey( _input.keycode ) == true )
				{
					keys.get( _input.keycode ).callPressed() ;
				}
				break ;
			}
			case KEYBOARD_RELEASED :
			{
				if( keys.containsKey( _input.keycode ) == true )
				{
					keys.get( _input.keycode ).callReleased() ;
				}
				break ;
			}
		}
	}

	private class Key
	{
		public final ArrayList<KeyInputListener> listeners = new ArrayList<KeyInputListener>() ;

		public Key( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void add( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void callPressed()
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).pressed() ;
			}
		}

		public void callReleased()
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).released() ;
			}
		}
	}
}