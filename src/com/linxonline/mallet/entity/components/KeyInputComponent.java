package com.linxonline.mallet.entity.components ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.input.KeyInputListener ;
import com.linxonline.mallet.input.KeyCode ;

/**
	Provides a convient method to process particular logic based an a key value.
	Decouples the key from the logic.
**/
public class KeyInputComponent extends InputComponent
{
	private final Map<KeyCode, Key> keys = MalletMap.<KeyCode, Key>newMap() ;

	public KeyInputComponent()
	{
		super() ;
	}

	public KeyInputComponent( final String _name )
	{
		this( _name, "INPUTCOMPONENT" ) ;
	}
	
	public KeyInputComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}
	
	public void registerKey( final KeyCode _key, final KeyInputListener _listener )
	{
		final Key key = keys.get( _key ) ;
		if( key != null )
		{
			key.add( _listener ) ;
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
				final Key key = keys.get( _input.keycode ) ;
				if( key != null )
				{
					key.callPressed( _input ) ;
				}
				break ;
			}
			case KEYBOARD_RELEASED :
			{
				final Key key = keys.get( _input.keycode ) ;
				if( key != null )
				{
					key.callReleased( _input ) ;
				}
				break ;
			}
		}
	}

	private static class Key
	{
		public final List<KeyInputListener> listeners = MalletList.<KeyInputListener>newList() ;

		public Key( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void add( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void callPressed( final InputEvent _input )
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).pressed( _input ) ;
			}
		}

		public void callReleased( final InputEvent _input )
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).released( _input ) ;
			}
		}
	}
}
