package com.linxonline.mallet.entity ;

import java.util.HashMap ;
import java.util.ArrayList ;

import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.input.KeyInputListener ;

/**
	Provides a convient method to process particular logic based an a key value.
	Decouples the key from the logic.
**/
public class KeyInputComponent extends InputComponent
{
	private final HashMap<Character, Key> keys = new HashMap<Character, Key>() ;

	public KeyInputComponent()
	{
		super() ;
	}

	public void registerKey( final char _key, final KeyInputListener _listener )
	{
		final Character key = Character.valueOf( _key ) ;
		if( keys.containsKey( key ) == true )
		{
			keys.get( key ).add( _listener ) ;
		}
		else
		{
			keys.put( key, new Key( _listener ) ) ;
		}
	}

	public void unregisterKey( final char _key )
	{
		final Character key = Character.valueOf( _key ) ;
		if( keys.containsKey( key ) == true )
		{
			keys.remove( key ) ;
		}
	}

	@Override
	protected void processInputEvent( final InputEvent _input )
	{
		switch( _input.inputType )
		{
			case InputEvent.KEYBOARD_PRESSED :
			{
				final Character key = Character.valueOf( _input.key ) ;
				if( keys.containsKey( key ) == true )
				{
					keys.get( key ).callPressed() ;
				}
				break ;
			}
			case InputEvent.KEYBOARD_RELEASED :
			{
				final Character key = Character.valueOf( _input.key ) ;
				if( keys.containsKey( key ) == true )
				{
					keys.get( key ).callReleased() ;
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